package com.jarvis.assistant.agent

import com.jarvis.assistant.data.AgentPlan
import com.jarvis.assistant.data.AgentStep
import com.jarvis.assistant.data.StepStatus
import org.json.JSONArray
import org.json.JSONObject

/**
 * Mission engine for JARVIS.
 * It creates a small, ordered plan and asks the configured model to work through
 * each step. It never executes generated shell commands or code on the device.
 */
class AgentCore(
    private val apiKey: String,
    private val baseUrl: String,
    private val model: String,
    private val conversationContext: List<Pair<String, String>> = emptyList()
) {
    fun plan(goal: String): AgentPlan {
        require(apiKey.isNotBlank()) { "أدخل مفتاح API أولاً." }
        require(baseUrl.isNotBlank()) { "أدخل رابط API." }
        require(model.isNotBlank()) { "أدخل اسم النموذج." }

        val prompt = """
            أنت JARVIS، مساعد ذكي يحوّل هدف المستخدم إلى خطة تنفيذ عملية.
            أعد JSON صالحاً فقط دون Markdown وبالشكل التالي بالضبط:
            {"steps":[{"title":"عنوان قصير","tool":"analysis|writing|code|design|research","detail":"ما الذي ستفعله في هذه الخطوة"}]}
            أنشئ من 3 إلى 6 خطوات مرتبة، محددة وقابلة للتنفيذ. لا تدّعِ أنك نفذت شيئاً بعد.
            هدف المستخدم: $goal
        """.trimIndent()

        val raw = LlmClient.chat(
            apiKey, baseUrl, model,
            buildList {
                add("system" to "أنت مخطط مهام دقيق. أخرج JSON صالحاً فقط. استخدم سياق المحادثة السابقة عند صلته بالهدف.")
                addAll(conversationContext.takeLast(12))
                add("user" to prompt)
            }
        )
        val jsonText = extractJson(raw)
        val root = JSONObject(jsonText)
        val array = root.optJSONArray("steps") ?: JSONArray()
        val steps = mutableListOf<AgentStep>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val title = item.optString("title").trim()
            if (title.isNotEmpty()) {
                steps += AgentStep(
                    title = title.take(100),
                    tool = item.optString("tool", "analysis").ifBlank { "analysis" }.take(40),
                    detail = item.optString("detail").take(500)
                )
            }
        }
        if (steps.isEmpty()) throw IllegalStateException("النموذج لم يُرجع خطة مفهومة. جرّب مرة أخرى.")
        return AgentPlan(goal = goal, steps = steps.take(8).toMutableList())
    }

    suspend fun execute(plan: AgentPlan, onStep: suspend (AgentStep, Int) -> Unit) {
        val history = mutableListOf<Pair<String, String>>()
        history += "system" to """
            أنت JARVIS، مساعد تنفيذي. أنجز خطوة واحدة في كل مرة بشكل عملي ومفصل.
            إذا كانت الخطوة تتطلب ملفات أو كوداً، أخرج المحتوى كاملاً مع أسماء الملفات.
            لا تزعم تنفيذ إجراءات على الهاتف أو الإنترنت أو إنشاء ملفات فعلية؛ أنت تنتج إرشادات ومحتوى داخل المحادثة فقط.
            اذكر الافتراضات والقيود بوضوح، ولا تطلب مفاتيح سرية أو كلمات مرور.
        """.trimIndent()
        history += conversationContext.takeLast(12)
        history += "user" to "الهدف الكامل: ${plan.goal}\nابدأ بتنفيذ الخطوات بالترتيب."

        plan.steps.forEachIndexed { index, step ->
            plan.steps[index] = step.copy(status = StepStatus.RUNNING)
            onStep(plan.steps[index], index)
            try {
                val task = """
                    الخطوة ${index + 1} من ${plan.steps.size}: ${step.title}
                    نوع العمل: ${step.tool}
                    التفاصيل: ${step.detail}
                    نفّذ هذه الخطوة الآن. اجعل الناتج واضحاً وقابلاً للاستخدام.
                """.trimIndent()
                history += "user" to task
                val result = LlmClient.chat(apiKey, baseUrl, model, history)
                history += "assistant" to result
                plan.steps[index] = step.copy(detail = result.take(12000), status = StepStatus.DONE)
                onStep(plan.steps[index], index)
            } catch (error: Exception) {
                plan.steps[index] = step.copy(
                    detail = "فشل تنفيذ الخطوة: ${error.message ?: "خطأ غير معروف"}".take(1000),
                    status = StepStatus.FAILED
                )
                onStep(plan.steps[index], index)
                throw error
            }
        }
    }

    private fun extractJson(text: String): String {
        val cleaned = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        if (start < 0 || end <= start) throw IllegalStateException("رد التخطيط ليس JSON صالحاً.")
        return cleaned.substring(start, end + 1)
    }
}

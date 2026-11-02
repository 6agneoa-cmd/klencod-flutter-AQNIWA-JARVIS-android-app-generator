package com.jarvis.assistant.agent

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Client for any OpenAI-compatible API (OpenRouter / OpenAI / Ollama / local). */
object LlmClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    /** Non-streaming chat completion. */
    fun chat(
        apiKey: String,
        baseUrl: String,
        model: String,
        messages: List<Pair<String, String>>
    ): String {
        val arr = JSONArray()
        messages.forEach { (r, c) ->
            arr.put(JSONObject().put("role", r).put("content", c))
        }
        val body = JSONObject()
            .put("model", model)
            .put("messages", arr)
            .put("temperature", 0.7)
            .toString()
            .toRequestBody(JSON)

        val url = baseUrl.trimEnd('/') + "/chat/completions"
        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw RuntimeException("HTTP ${resp.code}: ${resp.body?.string()?.take(300)}")
            }
            val json = JSONObject(resp.body!!.string())
            return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }
}

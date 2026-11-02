package com.jarvis.assistant.agent

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import org.jsoup.Jsoup

data class WebResult(val title: String, val url: String, val snippet: String)

/** DuckDuckGo HTML search. Search results are untrusted external content. */
object WebSearch {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun search(query: String, limit: Int = 6): List<WebResult> {
        require(query.isNotBlank()) { "اكتب عبارة البحث أولاً." }
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val request = Request.Builder()
            .url("https://html.duckduckgo.com/html/?q=$encoded")
            .header("User-Agent", "Mozilla/5.0 (Android 14; Mobile) AppleWebKit/537.36 Chrome/120 Safari/537.36")
            .header("Accept-Language", "ar,en-US;q=0.8,en;q=0.6")
            .get().build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("تعذر البحث: HTTP ${response.code}")
            val doc = Jsoup.parse(response.body?.string().orEmpty())
            val results = doc.select(".result").mapNotNull { item ->
                val link = item.selectFirst("a.result__a") ?: return@mapNotNull null
                val url = link.absUrl("href").ifBlank { link.attr("href") }
                val title = link.text().trim()
                val snippet = item.selectFirst(".result__snippet")?.text()?.trim().orEmpty()
                if (title.isBlank() || url.isBlank()) null else WebResult(title, url, snippet)
            }.distinctBy { it.url }.take(limit)
            if (results.isEmpty()) throw IllegalStateException(
                "لم أجد نتائج. جرّب صياغة بحث مختلفة أو تحقق من اتصال الإنترنت."
            )
            return results
        }
    }
}

package com.skul9x.locateshare.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder

object LocationParser {

    private val client by lazy {
        OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

    /**
     * Giải quyết URL rút gọn sang URL đầy đủ và trích xuất tên địa điểm.
     */
    suspend fun resolveRedirectUrlToName(shortUrl: String): String = withContext(Dispatchers.IO) {
        if (!shortUrl.startsWith("http://") && !shortUrl.startsWith("https://")) {
            return@withContext ""
        }

        var currentUrl = shortUrl
        var extracted = extractNameFromFullUrl(currentUrl)
        if (extracted.isNotEmpty()) {
            return@withContext extracted
        }

        var redirectCount = 0
        while (redirectCount < 5) {
            val request = Request.Builder()
                .url(currentUrl)
                .head()
                .build()

            var nextUrl: String? = null
            var responseCode = 0
            var resolvedPathUrl: String? = null

            try {
                client.newCall(request).execute().use { response ->
                    responseCode = response.code
                    if (responseCode in 300..399) {
                        nextUrl = response.header("Location")
                        if (nextUrl != null) {
                            resolvedPathUrl = response.request.url.resolve(nextUrl!!)?.toString()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                break
            }

            if (responseCode !in 300..399 || nextUrl == null) {
                break
            }

            val resolvedUrl = resolvedPathUrl ?: nextUrl!!
            extracted = extractNameFromFullUrl(resolvedUrl)
            if (extracted.isNotEmpty()) {
                return@withContext extracted
            }

            currentUrl = resolvedUrl
            redirectCount++
        }
        return@withContext ""
    }


    /**
     * Trích xuất tên địa điểm từ văn bản chia sẻ của Google Maps
     */
    fun parseNameFromSharedText(sharedText: String, url: String): String {
        val lines = sharedText.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val urlIndex = lines.indexOfFirst { it.contains(url) }
        if (urlIndex <= 0) return ""

        val firstLine = lines[0]
        val genericKeywords = listOf(
            "đã ghim", "dropped pin", "pinned location", "vị trí đã ghim", 
            "vị trí được chia sẻ", "shared location", "lộ trình được chia sẻ", "shared route"
        )
        val isGeneric = genericKeywords.any { firstLine.contains(it, ignoreCase = true) }

        return if (isGeneric && lines.size > 1 && urlIndex > 1) {
            // Nếu là nhãn ghim chung chung, ghép dòng thứ 2 (địa chỉ/khu vực) để rõ thông tin hơn
            "$firstLine (${lines[1]})"
        } else {
            firstLine
        }
    }

    /**
     * Trích xuất tên địa điểm từ URL đầy đủ của Google Maps
     */
    fun extractNameFromFullUrl(url: String): String {
        try {
            val decodedUrl = URLDecoder.decode(url, "UTF-8")
            // Khớp mọi ký tự từ sau "/place/" cho đến khi gặp dấu "/", "?" hoặc "#"
            val regex = "/place/([^/?#]+)".toRegex()
            val match = regex.find(decodedUrl)
            if (match != null) {
                // URLDecoder đã tự động chuyển '+' thành khoảng trắng ' ' và giải mã Unicode
                return match.groupValues[1]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}

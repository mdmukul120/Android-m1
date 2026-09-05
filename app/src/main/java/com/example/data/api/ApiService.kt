package com.example.data.api

import android.util.Log
import com.example.data.model.LiveChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object ApiService {
    private const val TAG = "MukulApiService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // 1. Ping Proxy & Tracking Pixel
    suspend fun pingServices() = withContext(Dispatchers.IO) {
        try {
            val pingReq = Request.Builder()
                .url("https://streambd-proxy.aininjaibrahim.workers.dev/?ping=1")
                .header("User-Agent", "MukulPlus/1.0")
                .build()
            client.newCall(pingReq).execute().close()
        } catch (e: Exception) {
            Log.w(TAG, "Proxy ping failed: ${e.message}")
        }

        try {
            val pixelReq = Request.Builder()
                .url("https://sleepoverlimitprofound.com/pixel/ase")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            client.newCall(pixelReq).execute().close()
        } catch (e: Exception) {
            Log.w(TAG, "Pixel tracking failed: ${e.message}")
        }
    }

    // 2. Fetch Bioscope+ Live API
    suspend fun fetchBioscopePage(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api-static.bioscopelive.com/v2?language=en&country=BD&platform=web")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Origin", "https://www.bioscopeplus.com")
                .header("Referer", "https://www.bioscopeplus.com/")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return@withContext response.body?.string()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Bioscope API: ${e.message}")
        }
        null
    }

    // 3. Fetch IPTV M3U Playlist
    suspend fun fetchIptvPlaylist(): List<LiveChannel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<LiveChannel>()
        try {
            val request = Request.Builder()
                .url("https://raw.githubusercontent.com/abusaeeidx/Ayna-BDIX-IPTV-Playlist/refs/heads/main/ayna-playlist.m3u")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val m3uContent = response.body?.string().orEmpty()
                    channels.addAll(parseM3u(m3uContent))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching IPTV playlist: ${e.message}")
        }
        channels
    }

    // 4. Try fetching Hamyra API endpoints
    suspend fun fetchHamyraEndpoint(endpoint: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(endpoint)
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "https://www.bioscopeplus.com/")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return@withContext response.body?.string()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Hamyra API request failed for $endpoint: ${e.message}")
        }
        null
    }

    private fun parseM3u(content: String): List<LiveChannel> {
        val list = mutableListOf<LiveChannel>()
        val lines = content.lines()
        var currentName = ""
        var currentLogo = ""
        var currentGroup = "General"
        var currentId = ""

        val tvgNamePattern = Pattern.compile("tvg-name=\"([^\"]+)\"")
        val tvgLogoPattern = Pattern.compile("tvg-logo=\"([^\"]+)\"")
        val groupPattern = Pattern.compile("group-title=\"([^\"]+)\"")
        val tvgIdPattern = Pattern.compile("tvg-id=\"([^\"]+)\"")

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                // Extract group
                val groupMatcher = groupPattern.matcher(trimmed)
                if (groupMatcher.find()) {
                    currentGroup = groupMatcher.group(1) ?: "General"
                }

                // Extract logo
                val logoMatcher = tvgLogoPattern.matcher(trimmed)
                if (logoMatcher.find()) {
                    currentLogo = logoMatcher.group(1) ?: ""
                }

                // Extract ID
                val idMatcher = tvgIdPattern.matcher(trimmed)
                if (idMatcher.find()) {
                    currentId = idMatcher.group(1) ?: ""
                }

                // Extract Name
                val nameMatcher = tvgNamePattern.matcher(trimmed)
                if (nameMatcher.find()) {
                    currentName = nameMatcher.group(1) ?: ""
                } else {
                    val commaIndex = trimmed.lastIndexOf(',')
                    if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                        currentName = trimmed.substring(commaIndex + 1).trim()
                    }
                }
            } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                if (currentName.isNotEmpty()) {
                    list.add(
                        LiveChannel(
                            id = if (currentId.isNotEmpty()) currentId else "ch_${list.size + 1}",
                            name = currentName,
                            logoUrl = currentLogo,
                            group = currentGroup.ifEmpty { "General" },
                            streamUrl = trimmed
                        )
                    )
                }
                // reset
                currentName = ""
                currentLogo = ""
                currentGroup = "General"
                currentId = ""
            }
        }
        return list
    }
}

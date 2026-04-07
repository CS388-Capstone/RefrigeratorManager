package com.cs388group.refrigeratormanager.services


import android.util.Log
import com.cs388group.refrigeratormanager.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object OpenAIService {
    private val client = OkHttpClient()
    private const val URL = "https://api.openai.com/v1/chat/completions"

    fun sendMessage(userMessage: String): String {
        val json = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            })
            put("max_tokens", 1000)
        }

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(URL)
            .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
        Log.d("OPENAI_AUTH_HEADER", "Bearer ${BuildConfig.OPENAI_API_KEY}...")
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val responseBody = response.body?.string()
                ?: throw IOException("Empty response body")

            val responseJson = JSONObject(responseBody)
            return responseJson
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }
}
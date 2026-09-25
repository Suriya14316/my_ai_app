package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askSaya(userPrompt: String, customApiKey: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey
            try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" } != "MY_GEMINI_API_KEY" -> {
                try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
            }
            else -> ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Friendly Neighbor fallback response when no API key is provided
            return@withContext Result.success(getFriendlyNeighborFallback(userPrompt))
        }

        try {
            val systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        "You are SAYA, a warm, helpful, conversational voice assistant that acts like a friendly neighbor. " +
                        "Speak concisely in 1-3 sentences with a friendly, welcoming, neighborly tone. " +
                        "Avoid markdown formatting or bullet points so your response sounds natural when spoken aloud."
                    )
                )
            )

            val request = GeminiRequest(
                systemInstruction = systemInstruction,
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(userPrompt)))
                )
            )

            val response = api.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.trim()

            if (!replyText.isNullOrBlank()) {
                Result.success(replyText)
            } else {
                Result.success(getFriendlyNeighborFallback(userPrompt))
            }
        } catch (e: Exception) {
            // Provide a graceful friendly neighbor response in case of network or quota hiccups
            Result.success("Hey neighbor! I ran into a network hiccup while reaching out: ${e.localizedMessage ?: "connection error"}. Feel free to ask me again or check your Gemini key.")
        }
    }

    private fun getFriendlyNeighborFallback(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("who are you") || lower.contains("your name") ->
                "Hey there! I'm SAYA, your friendly neighbor voice assistant. I'm here to lend a hand, check the weather, open your apps, or just share a warm chat!"
            lower.contains("how are you") ->
                "Doing wonderful, neighbor! It's great to hear your voice today. What can I help you take care of?"
            lower.contains("joke") ->
                "Why did the neighbor bring a ladder to the chat? Because they heard the conversation was at a whole new level! Always happy to brighten your day."
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello neighbor! Always great to see you. How can I lend a helping hand right now?"
            lower.contains("thank") ->
                "Anytime, neighbor! That's what friends next door are for. Let me know if you need anything else!"
            else ->
                "I hear you loud and clear, neighbor! SAYA is ready to assist. (Tip: Set your GEMINI_API_KEY in the AI Studio Secrets panel or Settings to unlock full conversational answers!)"
        }
    }
}

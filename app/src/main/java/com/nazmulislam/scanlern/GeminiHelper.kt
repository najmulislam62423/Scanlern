package com.nazmulislam.scanlern

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Request data classes
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

// Response data classes
data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-flash-latest:generateContent")
    fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}

object GeminiHelper {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GeminiApi::class.java)

    fun summarizeText(inputText: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val prompt = "Summarize this text in simple, easy to understand points for a student:\n\n$inputText"

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        api.generateContent(BuildConfig.GEMINI_API_KEY, request)
            .enqueue(object : Callback<GeminiResponse> {
                override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                    if (response.isSuccessful) {
                        val summary = response.body()
                            ?.candidates?.get(0)
                            ?.content?.parts?.get(0)?.text
                        onResult(summary ?: "No summary generated")
                    } else {
                        onError("API Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
    }
}
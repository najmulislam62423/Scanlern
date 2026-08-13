package com.nazmulislam.scanlern

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// Request models
data class GroqMessage(val role: String, val content: String)
data class GroqRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>
)

// Response models
data class GroqChoice(val message: GroqMessage)
data class GroqResponse(val choices: List<GroqChoice>?)

interface GroqApi {
    @POST("openai/v1/chat/completions")
    fun chatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: GroqRequest
    ): Call<GroqResponse>
}

object GroqHelper {

    private const val BASE_URL = "https://api.groq.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GroqApi::class.java)

    private fun callGroq(prompt: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val request = GroqRequest(
            messages = listOf(GroqMessage(role = "user", content = prompt))
        )

        api.chatCompletion("Bearer ${BuildConfig.GROQ_API_KEY}", request)
            .enqueue(object : Callback<GroqResponse> {
                override fun onResponse(call: Call<GroqResponse>, response: Response<GroqResponse>) {
                    if (response.isSuccessful) {
                        val result = response.body()?.choices?.get(0)?.message?.content
                        onResult(result ?: "No response generated")
                    } else {
                        onError("API Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<GroqResponse>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
    }

    fun summarizeText(inputText: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val prompt = "Summarize this text in simple, easy to understand points for a student. IMPORTANT: Respond in the SAME language as the input text (if input is in Bangla, respond in Bangla; if English, respond in English; if mixed, respond in the dominant language):\n\n$inputText"
        callGroq(prompt, onResult, onError)
    }

    fun generateFlashcards(inputText: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val prompt = """
            Based on this text, create exactly 5 flashcards for studying.
            IMPORTANT: Write the questions and answers in the SAME language as the input text below.
            Return ONLY a valid JSON array in this exact format, no extra text:
            [{"question": "...", "answer": "..."}, {"question": "...", "answer": "..."}]
            
            Text: $inputText
        """.trimIndent()
        callGroq(prompt, onResult, onError)
    }

    fun generateQuiz(inputText: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val prompt = """
        Based on this text, create exactly 5 multiple-choice quiz questions.
        IMPORTANT: Write everything in the SAME language as the input text below.
        Return ONLY a valid JSON array in this exact format, no extra text:
        [{"question": "...", "options": ["A", "B", "C", "D"], "correctAnswer": 0}]
        The correctAnswer field is the index (0-3) of the correct option in the options array.
        
        Text: $inputText
    """.trimIndent()
        callGroq(prompt, onResult, onError)
    }

    fun askQuestion(question: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val prompt = "You are a helpful study assistant for students. Answer this question clearly and simply, using short paragraphs or bullet points where helpful. IMPORTANT: Respond in the SAME language the student used to ask (Bangla or English):\n\n$question"
        callGroq(prompt, onResult, onError)
    }

    fun askQuestionWithContext(
        question: String,
        context: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val prompt = """
            You are a helpful study assistant. The student has attached a document/image with this content:
            ---
            $context
            ---
            
            Now answer the student's question about it clearly and simply, using short paragraphs or bullet points. IMPORTANT: Respond in the SAME language the student used to ask.
            
            Student's question: $question
        """.trimIndent()
        callGroq(prompt, onResult, onError)
    }

    fun analyzeWeakTopics(wrongQuestions: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val prompt = """
            A student got these quiz questions wrong:
            $wrongQuestions
            
            Based on these questions, identify the 2-3 main topics/subjects the student is weak in.
            Respond in ONE short friendly sentence (max 20 words) starting with "You're weak in" or similar, mentioning the topic names.
            Do NOT list the questions back, just name the topics.
        """.trimIndent()
        callGroq(prompt, onResult, onError)
    }

    fun explainText(selectedText: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val prompt = """
            A student selected this text from their notes and wants it explained simply:
            "$selectedText"
            
            Explain this in very simple, easy-to-understand language, as if explaining to a beginner. Use short sentences or bullet points. Keep it concise (max 100 words).
        """.trimIndent()
        callGroq(prompt, onResult, onError)
    }

    fun generateStudyPlan(
        examTitle: String,
        daysUntilExam: Int,
        topics: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val prompt = """
            A student has an exam called "$examTitle" in $daysUntilExam days.
            Topics to cover: $topics
            
            Create a day-by-day study plan distributing these topics evenly across all $daysUntilExam days.
            Return ONLY a valid JSON array in this exact format, no extra text, no markdown:
            [{"day": 1, "topic": "...", "task": "..."}, {"day": 2, "topic": "...", "task": "..."}]
            
            Keep "topic" short (2-4 words) and "task" short (one actionable sentence, max 15 words).
            Create exactly $daysUntilExam entries, one for each day.
        """.trimIndent()
        callGroq(prompt, onResult, onError)
    }

}
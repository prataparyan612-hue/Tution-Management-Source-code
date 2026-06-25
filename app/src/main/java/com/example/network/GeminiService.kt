package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request Data Models ---

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>
)

// --- Gemini Response Data Models ---

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

// --- Retrofit Api Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Retrofit Client Singleton ---

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}

// --- Helper for AI Performance Analytics Prompting ---

object GeminiAiHelper {
    suspend fun generateStudentAnalytics(
        studentName: String,
        grade: String,
        attendanceRate: Float,
        reportCards: List<com.example.data.model.ReportCard>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "AI Insights require a configured GEMINI_API_KEY in the Secrets Panel. To setup, go to Google AI Studio Secrets."
        }

        val subjectGrades = reportCards.joinToString("\n") { 
            "- ${it.subject}: Marks ${it.marksObtained}/${it.maxMarks} (Grade: ${it.grade}). Remarks: ${it.remarks}" 
        }

        val prompt = """
            You are an advanced educational analytics AI. Synthesize an actionable academic performance report for the following student:
            
            Student: $studentName
            Grade Level: $grade
            Attendance Rate: ${String.format("%.1f", attendanceRate)}%
            
            Subject Performance Log:
            $subjectGrades
            
            Provide a concise synthesis (under 200 words) comprising:
            1. Short Executive Summary (Strength and growth opportunity)
            2. Concrete actionable recommendations for the student (Max 2)
            3. A supportive, professional closing note.
            
            Format clearly using bullet points and brief bold section titles. Use a professional, encouraging, and clear tone.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Unable to extract response from AI model. Please verify API key."
        } catch (e: Exception) {
            "Error prompting Gemini AI: ${e.localizedMessage ?: "Connection Timeout"}"
        }
    }
}

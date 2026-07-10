package com.companion.learning.data.remote

import com.companion.learning.data.remote.dto.RoadmapSkeletonDto
import com.companion.learning.domain.provider.LlmProvider
import com.companion.learning.data.local.security.SecureStorage
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.io.IOException

class GeminiProvider @Inject constructor(
    private val secureStorage: SecureStorage,
    private val client: OkHttpClient
) : LlmProvider {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateSkeleton(goal: String, duration: String, level: String): Result<RoadmapSkeletonDto> = withContext(Dispatchers.IO) {
        try {
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                return@withContext Result.failure(Exception("API Key not set"))
            }

            val prompt = """
                You are an expert learning companion. Create a high-level roadmap skeleton.
                Goal: $goal
                Duration: $duration
                Experience Level: $level
                
                Respond ONLY in valid JSON matching this schema:
                {
                  "title": "String",
                  "summary": "String",
                  "milestones": [
                    { "weekNumber": Int, "title": "String", "summary": "String" }
                  ]
                }
            """.trimIndent()

            val requestBodyJson = buildJsonObject {
                put("contents", buildJsonArray {
                    addJsonObject {
                        put("parts", buildJsonArray {
                            addJsonObject { put("text", prompt) }
                        })
                    }
                })
                put("generationConfig", buildJsonObject {
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "No error body"
                return@withContext Result.failure(Exception("HTTP ${response.code}: $errorBody"))
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response")
            val jsonObject = json.parseToJsonElement(responseBody).jsonObject
            
            val candidates = jsonObject["candidates"]?.jsonArray
            val text = candidates?.firstOrNull()?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: throw Exception("Invalid response format")

            val dto = json.decodeFromString<RoadmapSkeletonDto>(text)
            Result.success(dto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun standardizeRoadmap(rawText: String, title: String): Result<RoadmapSkeletonDto> = withContext(Dispatchers.IO) {
        try {
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                return@withContext Result.failure(Exception("API Key not set in Settings! Please add a valid Gemini key first."))
            }

            val prompt = """
                You are an expert learning companion. Analyze the following unstructured text containing a study roadmap, course plan, or curriculum, and standardize it into a structured, clean week-by-week and day-by-day roadmap skeleton.

                Roadmap Title: $title
                Raw input text:
                $rawText

                Respond ONLY in valid JSON matching this schema:
                {
                  "title": "String",
                  "summary": "String",
                  "milestones": [
                    {
                      "weekNumber": Int,
                      "title": "String",
                      "summary": "String",
                      "days": [
                        { "dayNumber": Int, "topic": "String", "description": "String" }
                      ]
                    }
                  ]
                }

                Instructions:
                1. Group the material logically into week-by-week Milestones (weekNumber should start from 1, 2, ...).
                2. Within each week, list the corresponding Day topics (dayNumber should start from 1 and increment continuously across all weeks: e.g. Week 1 has Day 1-7, Week 2 has Day 8-14, etc.).
                3. If the input doesn't explicitly mention days, split the milestone's contents logically into daily chunks.
                4. Keep the day topics and descriptions clear, actionable and concise.
                5. Do NOT output markdown code blocks (like ```json) in your JSON output. Just output the raw JSON string.
            """.trimIndent()

            val requestBodyJson = buildJsonObject {
                put("contents", buildJsonArray {
                    addJsonObject {
                        put("parts", buildJsonArray {
                            addJsonObject { put("text", prompt) }
                        })
                    }
                })
                put("generationConfig", buildJsonObject {
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "No error body"
                return@withContext Result.failure(Exception("HTTP ${response.code}: $errorBody"))
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response")
            val jsonObject = json.parseToJsonElement(responseBody).jsonObject

            val candidates = jsonObject["candidates"]?.jsonArray
            val text = candidates?.firstOrNull()?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: throw Exception("Invalid response format")

            val dto = json.decodeFromString<RoadmapSkeletonDto>(text)
            Result.success(dto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

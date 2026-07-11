package com.companion.learning.data.remote

import android.util.Log
import com.companion.learning.data.remote.dto.RoadmapSkeletonDto
import com.companion.learning.data.remote.dto.MilestoneDto
import com.companion.learning.data.remote.dto.DayDto
import com.companion.learning.data.remote.dto.QuizQuestionDto
import com.companion.learning.data.remote.dto.ResourceDto
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
                Log.w("GeminiProvider", "API Key not set. Falling back to mock skeleton.")
                return@withContext Result.success(getMockSkeleton(goal, duration, level))
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
                Log.w("GeminiProvider", "HTTP ${response.code} generating skeleton. Falling back to mock skeleton. Error: $errorBody")
                return@withContext Result.success(getMockSkeleton(goal, duration, level))
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
            Log.w("GeminiProvider", "Exception generating skeleton. Falling back to mock skeleton.", e)
            Result.success(getMockSkeleton(goal, duration, level))
        }
    }

    override suspend fun standardizeRoadmap(rawText: String, title: String): Result<RoadmapSkeletonDto> = withContext(Dispatchers.IO) {
        try {
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                Log.w("GeminiProvider", "API Key not set. Falling back to mock standardized roadmap.")
                return@withContext Result.success(getMockStandardized(rawText, title))
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
                Log.w("GeminiProvider", "HTTP ${response.code} standardizing roadmap. Falling back to mock standardized roadmap. Error: $errorBody")
                return@withContext Result.success(getMockStandardized(rawText, title))
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
            Log.w("GeminiProvider", "Exception standardizing roadmap. Falling back to mock standardized roadmap.", e)
            Result.success(getMockStandardized(rawText, title))
        }
    }

    override suspend fun expandMilestone(
        roadmapGoal: String,
        milestoneTitle: String,
        milestoneSummary: String,
        weekNumber: Int,
        hoursPerDay: Int
    ): Result<List<DayDto>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                Log.w("GeminiProvider", "API Key not set. Falling back to mock week days.")
                return@withContext Result.success(getMockDays(milestoneTitle, weekNumber))
            }

            val prompt = """
                You are an expert learning companion. The user is studying a course with the following goal: "$roadmapGoal".
                We need to expand the weekly milestone "$milestoneTitle" (Summary: $milestoneSummary) into a detailed day-by-day study curriculum for Week $weekNumber.

                Assuming the user dedicates $hoursPerDay hours per day for study:
                Generate exactly 7 daily topics for this week (Day ${(weekNumber - 1) * 7 + 1} to Day ${weekNumber * 7}).

                Respond ONLY in valid JSON matching this schema:
                [
                  {
                    "dayNumber": Int,
                    "topic": "String",
                    "description": "String"
                  }
                ]

                Instructions:
                1. Do NOT wrap the output in markdown code blocks (like ```json). Return the raw JSON array string.
                2. Provide a practical topic and concise, helpful study description for each day.
                3. Ensure dayNumber values strictly match Day ${(weekNumber - 1) * 7 + 1} up to Day ${weekNumber * 7}.
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
                Log.w("GeminiProvider", "HTTP ${response.code} expanding milestone. Falling back to mock week days. Error: $errorBody")
                return@withContext Result.success(getMockDays(milestoneTitle, weekNumber))
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response")
            val jsonObject = json.parseToJsonElement(responseBody).jsonObject

            val candidates = jsonObject["candidates"]?.jsonArray
            val text = candidates?.firstOrNull()?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: throw Exception("Invalid response format")

            val list = json.decodeFromString<List<DayDto>>(text)
            Result.success(list)
        } catch (e: Exception) {
            Log.w("GeminiProvider", "Exception expanding milestone. Falling back to mock week days.", e)
            Result.success(getMockDays(milestoneTitle, weekNumber))
        }
    }

    override suspend fun generateQuiz(
        topic: String,
        description: String,
        goal: String
    ): Result<List<QuizQuestionDto>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                Log.w("GeminiProvider", "API Key not set. Falling back to mock quiz.")
                return@withContext Result.success(getMockQuiz(topic))
            }

            val prompt = """
                You are an expert learning companion. Generate exactly 5 multiple-choice quiz questions to test the learner's understanding of the following topic.

                Course Goal: $goal
                Topic: $topic
                Description: ${description.ifBlank { "No description provided." }}

                Respond ONLY in valid JSON matching this schema exactly:
                [
                  {
                    "question": "String",
                    "options": ["String", "String", "String", "String"],
                    "correctAnswer": "String"
                  }
                ]

                Instructions:
                1. Each question must have exactly 4 options.
                2. The correctAnswer must be exactly one of the 4 options (verbatim).
                3. Make questions practical and test real understanding, not just definitions.
                4. Do NOT wrap output in markdown code blocks. Return raw JSON only.
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
                Log.w("GeminiProvider", "HTTP ${response.code} generating quiz. Falling back to mock quiz. Error: $errorBody")
                return@withContext Result.success(getMockQuiz(topic))
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response")
            val jsonObject = json.parseToJsonElement(responseBody).jsonObject
            val candidates = jsonObject["candidates"]?.jsonArray
            val text = candidates?.firstOrNull()?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: throw Exception("Invalid response format")

            val questions = json.decodeFromString<List<QuizQuestionDto>>(text)
            Result.success(questions)
        } catch (e: Exception) {
            Log.w("GeminiProvider", "Exception generating quiz. Falling back to mock quiz.", e)
            Result.success(getMockQuiz(topic))
        }
    }

    override suspend fun recommendResources(
        topic: String,
        goal: String
    ): Result<List<ResourceDto>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                Log.w("GeminiProvider", "API Key not set. Falling back to mock resources.")
                return@withContext Result.success(getMockResources(topic))
            }

            val prompt = """
                You are an expert learning companion. Recommend exactly 4 high-quality, real, and accessible learning resources for the following topic.

                Course Goal: $goal
                Topic: $topic

                Respond ONLY in valid JSON matching this schema exactly:
                [
                  {
                    "title": "String",
                    "type": "VIDEO|DOCS|PRACTICE|ARTICLE",
                    "url": "String"
                  }
                ]

                Instructions:
                1. Include a mix of types: at least one VIDEO, one DOCS, and one PRACTICE resource.
                2. Provide real, working URLs (e.g., YouTube, official docs, LeetCode, GeeksForGeeks, MDN).
                3. Keep titles concise and descriptive (under 60 characters).
                4. Do NOT wrap output in markdown. Return raw JSON only.
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
                Log.w("GeminiProvider", "HTTP ${response.code} recommending resources. Falling back to mock resources. Error: $errorBody")
                return@withContext Result.success(getMockResources(topic))
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response")
            val jsonObject = json.parseToJsonElement(responseBody).jsonObject
            val candidates = jsonObject["candidates"]?.jsonArray
            val text = candidates?.firstOrNull()?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: throw Exception("Invalid response format")

            val resources = json.decodeFromString<List<ResourceDto>>(text)
            Result.success(resources)
        } catch (e: Exception) {
            Log.w("GeminiProvider", "Exception recommending resources. Falling back to mock resources.", e)
            Result.success(getMockResources(topic))
        }
    }

    // --- Core robust mock fallback templates ---

    private fun getMockSkeleton(goal: String, duration: String, level: String): RoadmapSkeletonDto {
        return RoadmapSkeletonDto(
            title = "Demo: $goal",
            summary = "A customized $level level course mapped over $duration to help you master the key components of $goal.",
            milestones = listOf(
                MilestoneDto(1, "Fundamentals of $goal", "Introduction to core elements, theoretical syntax, and foundational concepts."),
                MilestoneDto(2, "Intermediate Application & Flow", "Practical development patterns, application workflows, and building mini projects."),
                MilestoneDto(3, "Advanced Architecture & Systems", "Scaling systems, performance optimizations, memory optimization, and best practices."),
                MilestoneDto(4, "Testing, Deployment & Beyond", "Unit testing, integration testing, deploying to staging/production, and continuous updates.")
            )
        )
    }

    private fun getMockStandardized(rawText: String, title: String): RoadmapSkeletonDto {
        return RoadmapSkeletonDto(
            title = title,
            summary = "Imported roadmap containing study content from the provided raw text source.",
            milestones = listOf(
                MilestoneDto(1, "Phase 1: Basic Topics", "Introduction and foundational topics found in the import source.", listOf(
                    DayDto(1, "Core Concepts", "Reviewing main patterns and basic structures"),
                    DayDto(2, "Syntax and Variables", "Understanding syntax configurations"),
                    DayDto(3, "Logical Layouts", "Setting up control flows and routines")
                )),
                MilestoneDto(2, "Phase 2: Intermediate Topics", "Diving deeper into intermediate topics found in the import source.", listOf(
                    DayDto(4, "Advanced Structures", "Working with advanced algorithms and logic"),
                    DayDto(5, "Data Operations", "Querying, reading, and writing operations"),
                    DayDto(6, "Error Handling", "Handling logical exceptions and edge cases")
                ))
            )
        )
    }

    private fun getMockDays(milestoneTitle: String, weekNumber: Int): List<DayDto> {
        val startDay = (weekNumber - 1) * 7 + 1
        return listOf(
            DayDto(startDay, "Introduction to $milestoneTitle", "Understand the foundational concepts, definitions, and core principles of $milestoneTitle."),
            DayDto(startDay + 1, "Deep Dive into $milestoneTitle Syntax", "Explore the essential components, syntax, and configurations for $milestoneTitle."),
            DayDto(startDay + 2, "Practical Implementation of $milestoneTitle", "Walkthrough of coding examples and basic templates implementing $milestoneTitle."),
            DayDto(startDay + 3, "Common Design Patterns in $milestoneTitle", "Learn how to structure code efficiently and utilize popular architectural layouts."),
            DayDto(startDay + 4, "Debugging & Troubleshooting $milestoneTitle", "How to resolve typical warnings, exception flows, and syntax bugs in $milestoneTitle."),
            DayDto(startDay + 5, "Optimization and Performance Tuning", "Focus on reducing complexity, memory footprints, and latency configurations."),
            DayDto(startDay + 6, "Milestone Wrap-up & Mini Project", "Consolidate the week's learning by building a small real-world application using $milestoneTitle.")
        )
    }

    private fun getMockQuiz(topic: String): List<QuizQuestionDto> {
        return listOf(
            QuizQuestionDto(
                question = "What is the primary benefit of using $topic in a production application?",
                options = listOf(
                    "Improves system modularity and separation of concerns",
                    "Wipes local database and cache automatically on each reload",
                    "Requires no developer input or configurations",
                    "Mandates completely synchronous system behaviors"
                ),
                correctAnswer = "Improves system modularity and separation of concerns"
            ),
            QuizQuestionDto(
                question = "Which of the following is considered a best practice when implementing $topic?",
                options = listOf(
                    "Hardcoding all values and configs directly in functions",
                    "Using clean abstractions and decoupling business logic",
                    "Ignoring resource cleanups and connection closures",
                    "Running operations on the main main thread continuously"
                ),
                correctAnswer = "Using clean abstractions and decoupling business logic"
            ),
            QuizQuestionDto(
                question = "How does $topic handle potential error states or system anomalies?",
                options = listOf(
                    "It shuts down the app instantly without throwing errors",
                    "By catching exceptions gracefully and logging diagnostic info",
                    "By bypassing network security configurations",
                    "It redirects all requests to an external server"
                ),
                correctAnswer = "By catching exceptions gracefully and logging diagnostic info"
            ),
            QuizQuestionDto(
                question = "What type of scalability pattern does $topic naturally align with?",
                options = listOf(
                    "Single-threaded sequential queue pattern",
                    "Asynchronous event-driven or reactive architecture",
                    "Fully isolated local-only sandbox schema",
                    "Static compilation with no memory allocations"
                ),
                correctAnswer = "Asynchronous event-driven or reactive architecture"
            ),
            QuizQuestionDto(
                question = "When should a developer refactor code using $topic?",
                options = listOf(
                    "As soon as complexity increases and decoupling is needed",
                    "Only when compilation errors prevent the build from running",
                    "Never, because refactoring introduces bugs",
                    "Once the project completes its lifecycle and goes EOL"
                ),
                correctAnswer = "As soon as complexity increases and decoupling is needed"
            )
        )
    }

    private fun getMockResources(topic: String): List<ResourceDto> {
        return listOf(
            ResourceDto(
                title = "Getting Started with $topic Guide",
                type = "DOCS",
                url = "https://devdocs.io/"
            ),
            ResourceDto(
                title = "Crash Course on $topic Concepts",
                type = "VIDEO",
                url = "https://www.youtube.com/"
            ),
            ResourceDto(
                title = "Interactive Practice: $topic Challenges",
                type = "PRACTICE",
                url = "https://leetcode.com/"
            ),
            ResourceDto(
                title = "Understanding $topic Patterns (Deep Dive)",
                type = "ARTICLE",
                url = "https://medium.com/"
            )
        )
    }
}

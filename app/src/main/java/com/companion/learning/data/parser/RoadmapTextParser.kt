package com.companion.learning.data.parser

import com.companion.learning.data.remote.dto.DayDto
import com.companion.learning.data.remote.dto.MilestoneDto
import com.companion.learning.data.remote.dto.RoadmapSkeletonDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoadmapTextParser @Inject constructor() {

    /**
     * Parses raw extracted text from a document into a structured RoadmapSkeletonDto.
     *
     * Priority:
     * 1. Detect "=== WEEK N ===" or "WEEK N" section headers, then extract
     *    individual "Day N:" entries using marker-based splitting (handles word-wrap
     *    and multiple days per line).
     * 2. Fall back to generic week/phase/module headings.
     * 3. If no structure, chunk content into groups.
     */
    fun parse(rawText: String, title: String): RoadmapSkeletonDto {
        val milestones = extractWeekSections(rawText)
            ?: extractStructuredMilestones(rawText)
            ?: extractChunkedMilestones(rawText)

        return RoadmapSkeletonDto(
            title = title,
            summary = "Imported from document",
            milestones = milestones
        )
    }

    /**
     * Handles === WEEK N === or WEEK N section headers.
     * Uses marker-based splitting for days so it works regardless of
     * line wrapping or multiple days per line.
     */
    private fun extractWeekSections(rawText: String): List<MilestoneDto>? {
        val weekHeaderPattern = Regex(
            """={0,30}\s*WEEK\s+(\d+)\s*={0,30}""",
            RegexOption.IGNORE_CASE
        )

        val weekMatches = weekHeaderPattern.findAll(rawText).toList()
        if (weekMatches.isEmpty()) return null

        val milestones = mutableListOf<MilestoneDto>()

        weekMatches.forEachIndexed { index, match ->
            val weekNumber = match.groupValues[1].toIntOrNull() ?: (index + 1)
            val sectionStart = match.range.last + 1
            val sectionEnd = weekMatches.getOrNull(index + 1)?.range?.first ?: rawText.length
            val sectionText = rawText.substring(sectionStart, sectionEnd)

            val days = extractDaysFromText(sectionText)

            val summary = if (days.isNotEmpty()) {
                "Topics: ${days.joinToString(", ") { it.topic }.take(250)}"
            } else {
                sectionText.replace("\n", " ").trim().take(200)
            }

            milestones.add(
                MilestoneDto(
                    weekNumber = weekNumber,
                    title = "Week $weekNumber",
                    summary = summary,
                    days = days
                )
            )
        }

        return if (milestones.size >= 2) milestones else null
    }

    /**
     * Extracts Day items from a block of text using marker positions.
     * Works correctly even when:
     * - A single day's topic wraps across multiple lines
     * - Multiple days appear on the same line
     */
    private fun extractDaysFromText(text: String): List<DayDto> {
        val dayMarkerPattern = Regex("""Day\s+(\d+)\s*[:\-]\s*""", RegexOption.IGNORE_CASE)
        val matches = dayMarkerPattern.findAll(text).toList()

        return matches.mapIndexedNotNull { index, match ->
            val dayNumber = match.groupValues[1].toIntOrNull() ?: return@mapIndexedNotNull null
            val contentStart = match.range.last + 1
            val contentEnd = matches.getOrNull(index + 1)?.range?.first ?: text.length

            val topic = text.substring(contentStart, contentEnd)
                .replace(Regex("""\n"""), " ")  // flatten wrapped lines
                .replace(Regex("""\s{2,}"""), " ")  // collapse multiple spaces
                .trim()

            if (topic.isBlank()) return@mapIndexedNotNull null
            DayDto(dayNumber = dayNumber, topic = topic)
        }
    }

    private fun extractStructuredMilestones(rawText: String): List<MilestoneDto>? {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val headingPattern = Regex(
            "^(week|phase|module|step|stage|unit|chapter|section|part)\\s*\\d+[.:)-]?",
            RegexOption.IGNORE_CASE
        )
        val numberedPattern = Regex("^\\d+[.):]\\s+\\w")

        val milestones = mutableListOf<MilestoneDto>()
        var currentTitle = ""
        val currentBody = StringBuilder()
        var weekNumber = 0

        for (line in lines) {
            val isHeading = headingPattern.containsMatchIn(line) || numberedPattern.containsMatchIn(line)
            if (isHeading) {
                if (currentTitle.isNotBlank()) {
                    val sectionText = currentBody.toString()
                    milestones.add(MilestoneDto(
                        weekNumber = weekNumber,
                        title = currentTitle,
                        summary = sectionText.take(200).ifBlank { "Study this topic" },
                        days = extractDaysFromText(sectionText)
                    ))
                }
                weekNumber++
                currentTitle = line
                currentBody.clear()
            } else {
                currentBody.appendLine(line)
            }
        }

        if (currentTitle.isNotBlank()) {
            val sectionText = currentBody.toString()
            milestones.add(MilestoneDto(
                weekNumber = weekNumber,
                title = currentTitle,
                summary = sectionText.take(200).ifBlank { "Study this topic" },
                days = extractDaysFromText(sectionText)
            ))
        }

        return if (milestones.size >= 2) milestones else null
    }

    private fun extractChunkedMilestones(rawText: String): List<MilestoneDto> {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val chunkSize = 7
        val chunks = lines.chunked(chunkSize)
        return chunks.mapIndexed { index, chunk ->
            MilestoneDto(
                weekNumber = index + 1,
                title = "Week ${index + 1}: ${chunk.first().take(50)}",
                summary = chunk.joinToString(". ").take(200),
                days = emptyList()
            )
        }
    }
}

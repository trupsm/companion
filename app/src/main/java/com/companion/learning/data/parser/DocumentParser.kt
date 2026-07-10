package com.companion.learning.data.parser

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentParser @Inject constructor(
    private val context: Context
) {
    fun extractText(uri: Uri): Result<String> {
        return try {
            val mimeType = context.contentResolver.getType(uri) ?: detectTypeFromUri(uri)
            when {
                mimeType.contains("pdf") -> extractFromPdf(uri)
                mimeType.contains("word") || uri.toString().endsWith(".docx") -> extractFromDocx(uri)
                else -> extractFromText(uri)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to read file: ${e.message}"))
        }
    }

    /**
     * Extracts text from PDF using Android's native PdfRenderer.
     * Note: PdfRenderer renders pages as bitmaps, so we extract text via content streams.
     * For true text extraction we parse the raw PDF bytes for text operators.
     */
    private fun extractFromPdf(uri: Uri): Result<String> {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = PdfRenderer(pfd)
                val sb = StringBuilder()
                // PdfRenderer renders pages visually, not text. For text we parse raw content.
                // Close renderer and re-open as stream for raw extraction.
                val pageCount = renderer.pageCount
                renderer.close()

                // Re-read as raw bytes and extract text strings from PDF content
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val rawContent = inputStream.bufferedReader().readText()
                    // Extract text between BT/ET (Begin Text/End Text) markers
                    val btEtPattern = Regex("BT[\\s\\S]*?ET")
                    val textPattern = Regex("\\(([^)]+)\\)\\s*Tj|\\(([^)]+)\\)\\s*TJ")

                    btEtPattern.findAll(rawContent).forEach { block ->
                        textPattern.findAll(block.value).forEach { match ->
                            val text = match.groupValues[1].ifEmpty { match.groupValues[2] }
                            if (text.isNotBlank()) sb.append(text).append(" ")
                        }
                        sb.append("\n")
                    }
                }

                val result = sb.toString().trim()
                if (result.isBlank()) {
                    Result.failure(Exception("Could not extract text from this PDF. Please save it as a .txt file and try again."))
                } else {
                    Result.success(result)
                }
            } ?: Result.failure(Exception("Could not open PDF file"))
        } catch (e: Exception) {
            Result.failure(Exception("PDF parsing failed. Try converting to .txt first.\n(${e.message})"))
        }
    }

    private fun extractFromDocx(uri: Uri): Result<String> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val sb = StringBuilder()
                val zip = ZipInputStream(inputStream)
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val content = zip.bufferedReader().readText()
                        // Strip XML tags and extract plain text
                        val plainText = content
                            .replace(Regex("<w:br[^>]*/>"), "\n")
                            .replace(Regex("<w:p[ >][\\s\\S]*?</w:p>"), { match ->
                                match.value.replace(Regex("<[^>]+>"), "") + "\n"
                            })
                            .replace(Regex("<[^>]+>"), "")
                            .replace(Regex("\\s{2,}"), " ")
                            .trim()
                        sb.append(plainText)
                        break
                    }
                    entry = zip.nextEntry
                }
                if (sb.isEmpty()) Result.failure(Exception("No text found in DOCX file"))
                else Result.success(sb.toString())
            } ?: Result.failure(Exception("Could not open DOCX file"))
        } catch (e: Exception) {
            Result.failure(Exception("DOCX parsing failed: ${e.message}"))
        }
    }

    private fun extractFromText(uri: Uri): Result<String> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val text = BufferedReader(InputStreamReader(inputStream)).readText()
                Result.success(text)
            } ?: Result.failure(Exception("Could not open text file"))
        } catch (e: Exception) {
            Result.failure(Exception("Text file reading failed: ${e.message}"))
        }
    }

    private fun detectTypeFromUri(uri: Uri): String {
        val path = uri.toString().lowercase()
        return when {
            path.endsWith(".pdf") -> "application/pdf"
            path.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "text/plain"
        }
    }
}

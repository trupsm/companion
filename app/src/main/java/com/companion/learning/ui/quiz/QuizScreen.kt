package com.companion.learning.ui.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Generating quiz questions…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { viewModel.retry() }) { Text("Retry") }
                    }
                }

                uiState.isComplete -> {
                    QuizResultsScreen(
                        uiState = uiState,
                        onRetake = { viewModel.retake() },
                        onBack = onNavigateBack
                    )
                }

                uiState.questions.isNotEmpty() -> {
                    QuizQuestionCard(
                        uiState = uiState,
                        onSelectAnswer = { viewModel.selectAnswer(it) },
                        onConfirm = { viewModel.confirmAnswer() },
                        onNext = { viewModel.nextQuestion() }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionCard(
    uiState: QuizUiState,
    onSelectAnswer: (String) -> Unit,
    onConfirm: () -> Unit,
    onNext: () -> Unit
) {
    val question = uiState.questions[uiState.currentIndex]
    val options = uiState.parsedOptions.getOrElse(uiState.currentIndex) { emptyList() }
    val answered = uiState.answeredCorrectly[uiState.currentIndex]
    val isAnswered = answered != null
    val total = uiState.questions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = { (uiState.currentIndex.toFloat()) / total },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Question ${uiState.currentIndex + 1} of $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Topic chip
        SuggestionChip(
            onClick = {},
            label = { Text(uiState.topicName, style = MaterialTheme.typography.labelSmall) }
        )

        // Question text
        AnimatedContent(
            targetState = uiState.currentIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "question"
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = question.question,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Options
        options.forEach { option ->
            val isSelected = uiState.selectedAnswer == option
            val borderColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary
                isAnswered && option == question.correctAnswer -> Color(0xFF4CAF50)
                isAnswered && isSelected && answered == false -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outline
            }
            val containerColor = when {
                isAnswered && option == question.correctAnswer -> Color(0xFFE8F5E9)
                isAnswered && isSelected && answered == false -> MaterialTheme.colorScheme.errorContainer
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }

            OutlinedCard(
                onClick = { if (!isAnswered) onSelectAnswer(option) },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(
                    width = if (isSelected || (isAnswered && option == question.correctAnswer)) 2.dp else 1.dp,
                    color = borderColor
                ),
                colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isAnswered && option == question.correctAnswer) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                    } else if (isAnswered && isSelected && answered == false) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Action button
        if (!isAnswered) {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedAnswer != null
            ) {
                Text("Check Answer")
            }
        } else {
            val isLast = uiState.currentIndex >= uiState.questions.size - 1
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLast) "See Results" else "Next Question →")
            }
        }
    }
}

@Composable
private fun QuizResultsScreen(
    uiState: QuizUiState,
    onRetake: () -> Unit,
    onBack: () -> Unit
) {
    val score = uiState.answeredCorrectly.count { it == true }
    val total = uiState.questions.size
    val percentage = if (total > 0) (score * 100) / total else 0

    val (headline, color) = when {
        percentage >= 80 -> "Excellent! 🎉" to Color(0xFF4CAF50)
        percentage >= 60 -> "Good Job! 👍" to MaterialTheme.colorScheme.primary
        else -> "Keep Practicing 💪" to MaterialTheme.colorScheme.secondary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )

        // Score circle-style card
        Card(
            modifier = Modifier.size(160.dp),
            shape = RoundedCornerShape(80.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score/$total",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Text(
            text = "Topic: ${uiState.topicName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Per-question breakdown
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Question Breakdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                uiState.questions.forEachIndexed { index, q ->
                    val correct = uiState.answeredCorrectly.getOrNull(index)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (correct == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (correct == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Q${index + 1}: ${q.question.take(50)}${if (q.question.length > 50) "…" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back to Topic")
            }
            Button(onClick = onRetake, modifier = Modifier.weight(1f)) {
                Text("Retake Quiz")
            }
        }
    }
}

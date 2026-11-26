package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.AiLabRecord
import com.example.model.AiSourceMode
import com.example.model.AiToolType
import com.example.model.DifficultyLevel
import com.example.ui.AdaptiveQuizUiState
import com.example.ui.AiLabUiState
import com.example.ui.theme.ErrorContainerLight
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen

@Composable
fun LaboratoriumGenAiScreen(
    aiUiState: AiLabUiState,
    adaptiveQuizState: AdaptiveQuizUiState,
    difficulty: DifficultyLevel,
    stageStrategySummary: String,
    aiRecords: List<AiLabRecord>,
    onSelectDifficulty: (DifficultyLevel) -> Unit,
    onGenerateCaseStudy: (String) -> Unit,
    onGenerateQuiz: (String) -> Unit,
    onAnalyzeStrategy: (topicOrCase: String, studentAnswer: String) -> Unit,
    onAskAiTutor: (String) -> Unit,
    onRetryAiTask: (toolType: AiToolType, topicInput: String, studentAnswerInput: String) -> Unit,
    onAnswerAdaptiveQuiz: (questionIndex: Int, optionIndex: Int) -> Unit,
    onContinueToEvaluation: () -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var studentAnswerInput by remember { mutableStateOf("") }
    val topicPresets = listOf(
        "Strategi Pembelajaran PAI",
        "Fikih Muamalah Digital",
        "Akhlak & Moderasi Beragama",
        "Praktik Ibadah Kelas Heterogen",
        "Kearifan Lokal Batik & Gotong Royong"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 800.dp)) {
            Surface(color = GoldContainer, shape = MaterialTheme.shapes.small) {
                Text(
                    text = "LABORATORIUM GENERATIVE AI • FIREBASE AI LOGIC",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldDarkText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Laboratorium AI Strategi PAI",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Terhubung langsung ke Gemini AI melalui backend aman Firebase AI Logic & App Check untuk studi kasus baru, 5 soal kuis, analisis jawaban mahasiswa, dan AI Tutor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Backend Verification & Adaptive Difficulty Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gemini AI Aktif (Firebase AI Logic + App Check)",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Seluruh studi kasus, kuis 5 soal, dan analisis strategi dihasilkan langsung oleh Gemini tanpa jawaban statis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tingkat Kesulitan Adaptif:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DifficultyLevel.entries.forEach { level ->
                            FilterChip(
                                selected = difficulty == level,
                                onClick = { onSelectDifficulty(level) },
                                label = { Text(level.label) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Preset Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topicPresets.forEach { preset ->
                    FilterChip(
                        selected = promptInput == preset,
                        onClick = { promptInput = preset },
                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                label = { Text("Topik Kasus, Topik Kuis, atau Pertanyaan AI Tutor") },
                placeholder = { Text("Contoh: Pembelajaran Fikih Zakat di kelas XI yang heterogen...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("genai_prompt_input"),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = studentAnswerInput,
                onValueChange = { studentAnswerInput = it },
                label = { Text("Jawaban / Rancangan Strategi Mahasiswa (untuk Analisis Strategi)") },
                placeholder = {
                    Text("Contoh: Saya memilih Problem Based Learning dipadukan gotong royong agar siswa visual, auditori, dan kinestetik aktif berkolaborasi...")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("genai_student_answer_input"),
                minLines = 2,
                maxLines = 4
            )

            if (stageStrategySummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                AssistChip(
                    onClick = { studentAnswerInput = stageStrategySummary },
                    label = {
                        Text(
                            text = "Isi Jawaban dari Pilihan Tahap 5–7",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.testTag("fill_stage_strategy_chip")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Required Action Buttons (Smartphone-Responsive)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onGenerateCaseStudy(promptInput) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp)
                        .testTag("btn_generate_case_study")
                ) {
                    Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GENERATE CASE STUDY",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick = { onGenerateQuiz(promptInput) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp)
                        .testTag("btn_generate_quiz"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GENERATE QUIZ",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAnalyzeStrategy(promptInput, studentAnswerInput) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp)
                        .testTag("btn_analyze_strategy")
                ) {
                    Icon(Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ANALISIS STRATEGI",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
                OutlinedButton(
                    onClick = { onAskAiTutor(promptInput) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp)
                        .testTag("btn_ask_ai_tutor")
                ) {
                    Icon(Icons.Filled.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TANYA AI TUTOR",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Streaming / Text Output Area
            when (aiUiState) {
                AiLabUiState.Idle -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tekan GENERATE CASE STUDY untuk kasus baru yang unik setiap kali ditekan, GENERATE QUIZ untuk 5 soal baru, atau tulis jawaban Anda lalu tekan ANALISIS STRATEGI.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                is AiLabUiState.Loading -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Gemini AI sedang menyusun respons akademis PAI...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                is AiLabUiState.Streaming -> {
                    AiOutputCard(
                        title = "Menghasilkan Secara Real-Time...",
                        body = aiUiState.text,
                        sourceMode = aiUiState.sourceMode
                    )
                }
                is AiLabUiState.Success -> {
                    val friendlyTitle = when (aiUiState.toolType) {
                        AiToolType.CASE_STUDY -> "Studi Kasus PAI Baru"
                        AiToolType.STRATEGY_ANALYSIS -> "Analisis Pedagogis Jawaban Mahasiswa"
                        AiToolType.AI_TUTOR -> "Jawaban AI Tutor PAI"
                        AiToolType.QUIZ -> "Kuis Adaptif"
                    }
                    AiOutputCard(
                        title = "$friendlyTitle • Level ${aiUiState.difficulty.label}",
                        body = aiUiState.text,
                        sourceMode = aiUiState.sourceMode
                    )
                }
                is AiLabUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorContainerLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = ErrorCrimson)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Permintaan Gemini AI Belum Berhasil", style = MaterialTheme.typography.titleSmall, color = ErrorCrimson)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(aiUiState.message, style = MaterialTheme.typography.bodySmall, color = ErrorCrimson)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onRetryAiTask(
                                        aiUiState.toolType,
                                        aiUiState.topicInput,
                                        aiUiState.studentAnswerInput
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Coba Lagi dengan Gemini AI")
                            }
                        }
                    }
                }
            }

            // 5-Question Adaptive Quiz Output Area
            Spacer(modifier = Modifier.height(12.dp))
            when (adaptiveQuizState) {
                AdaptiveQuizUiState.Idle -> Unit
                AdaptiveQuizUiState.Loading -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Gemini AI sedang menyusun 5 soal kuis baru level ${difficulty.label}...")
                        }
                    }
                }
                is AdaptiveQuizUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorContainerLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(adaptiveQuizState.message, style = MaterialTheme.typography.bodySmall, color = ErrorCrimson)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { onGenerateQuiz(adaptiveQuizState.topicFocus) }) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Coba Lagi Buat 5 Soal")
                            }
                        }
                    }
                }
                is AdaptiveQuizUiState.Ready -> {
                    val total = adaptiveQuizState.questions.size
                    val answeredCount = adaptiveQuizState.selectedAnswers.size
                    val correctCount = adaptiveQuizState.questions.indices.count { idx ->
                        adaptiveQuizState.selectedAnswers[idx] == adaptiveQuizState.questions[idx].correctIndex
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kuis Gemini AI ($total Soal Baru • ${adaptiveQuizState.difficulty.label})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(color = GoldContainer, shape = MaterialTheme.shapes.small) {
                                    Text(
                                        text = "Terjawab $answeredCount/$total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldDarkText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Topik: ${adaptiveQuizState.topicFocus}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            adaptiveQuizState.questions.forEachIndexed { qIdx, q ->
                                val selectedIdx = adaptiveQuizState.selectedAnswers[qIdx]
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Soal ${qIdx + 1} • ${q.topic}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldDarkText
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = q.question,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        q.options.forEachIndexed { optIdx, optText ->
                                            val isPicked = selectedIdx == optIdx
                                            val isCorrect = optIdx == q.correctIndex
                                            val bg = when {
                                                selectedIdx != null && isCorrect -> SuccessContainer
                                                isPicked && !isCorrect -> ErrorContainerLight
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                            Card(
                                                onClick = { onAnswerAdaptiveQuiz(qIdx, optIdx) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .testTag("ai_quiz_${qIdx}_opt_${optIdx}"),
                                                colors = CardDefaults.cardColors(containerColor = bg)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${'A' + optIdx}. $optText",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (selectedIdx != null && isCorrect) {
                                                        Icon(
                                                            Icons.Filled.CheckCircle,
                                                            contentDescription = null,
                                                            tint = SuccessGreen,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (selectedIdx != null) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Pembahasan Pedagogis: ${q.explanation}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            if (answeredCount == total && total > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = SuccessContainer,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Hasil Kuis Gemini AI: $correctCount dari $total soal benar (${(correctCount * 100) / total}%). Tingkat kesulitan adaptif berikutnya telah disesuaikan ke level ${difficulty.label}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (aiRecords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Riwayat Lab GenAI Tersimpan (${aiRecords.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                aiRecords.take(3).forEach { rec ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${rec.toolType} • ${AiSourceMode.safeValueOf(rec.sourceMode).label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldDarkText
                            )
                            Text(
                                text = rec.promptText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueToEvaluation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("continue_evaluation_button")
            ) {
                Text("Lanjut ke Tahap 9: Evaluasi & Refleksi")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AiOutputCard(
    title: String,
    body: String,
    sourceMode: AiSourceMode
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(color = GoldContainer, shape = MaterialTheme.shapes.small) {
                    Text(
                        text = sourceMode.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldDarkText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

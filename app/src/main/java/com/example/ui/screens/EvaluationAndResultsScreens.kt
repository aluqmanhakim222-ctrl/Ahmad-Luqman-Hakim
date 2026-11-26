package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.LearningContentCatalog
import com.example.model.LearningStage
import com.example.model.StudentProgress
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.ErrorContainerLight
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen

@Composable
fun EvaluasiRefleksiScreen(
    answers: Map<Int, Int>,
    submitted: Boolean,
    initialReflection: String,
    onSelectAnswer: (Int, Int) -> Unit,
    onRetryQuiz: () -> Unit,
    onSubmitEvaluation: (String) -> Unit
) {
    val questions = LearningContentCatalog.evaluationQuestions
    var reflectionText by remember(initialReflection) { mutableStateOf(initialReflection) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 780.dp)) {
            Surface(color = GoldContainer, shape = MaterialTheme.shapes.small) {
                Text(
                    text = "TAHAP 9 • EVALUASI PEMBELAJARAN & REFLEKSI",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldDarkText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Kuis Evaluasi & Jurnal Refleksi",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Jawab ${questions.size} soal pilihan ganda di bawah ini. Umpan balik otomatis dan pembahasan langsung tampil setelah Anda memilih jawaban.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            val evalCompletedItems = answers.size + (if (reflectionText.trim().length >= 10) 1 else 0)
            com.example.ui.components.ActivityCompletionIndicatorCard(
                activityTitle = "Indikator Penyelesaian Evaluasi & Refleksi",
                completedCount = evalCompletedItems,
                totalCount = questions.size + 1,
                optimalCount = if (answers.isEmpty()) null else questions.indices.count { answers[it] == questions[it].correctIndex },
                isCompleted = submitted,
                testTagPrefix = "activity_evaluation"
            )

            Spacer(modifier = Modifier.height(12.dp))

            questions.forEachIndexed { qIdx, qItem ->
                val chosen = answers[qIdx]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Soal ${qIdx + 1} • ${qItem.topicTag}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldDarkText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(qItem.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))

                        qItem.options.forEachIndexed { optIdx, optText ->
                            val isPicked = chosen == optIdx
                            val isCorrect = optIdx == qItem.correctIndex
                            val showFeedback = chosen != null
                            val bg = when {
                                showFeedback && isCorrect -> SuccessContainer
                                isPicked && !isCorrect -> ErrorContainerLight
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                            Card(
                                onClick = { onSelectAnswer(qIdx, optIdx) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .testTag("eval_q${qIdx}_opt${optIdx}"),
                                colors = CardDefaults.cardColors(containerColor = bg),
                                border = if (isPicked) BorderStroke(1.5.dp, if (isCorrect) SuccessGreen else ErrorCrimson) else null
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
                                    if (showFeedback && isCorrect) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        if (chosen != null) {
                            val isCorrect = chosen == qItem.correctIndex
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (isCorrect) SuccessContainer else GoldContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (if (isCorrect) "Benar! " else "Perlu Ditinjau: ") + qItem.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCorrect) SuccessGreen else GoldDarkText,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Refleksi Pembelajaran Mahasiswa",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tuliskan pemahaman terpenting yang Anda peroleh tentang pemilihan strategi pembelajaran PAI dan integrasi kearifan lokal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reflectionText,
                        onValueChange = { reflectionText = it },
                        label = { Text("Refleksi Pembelajaran Anda") },
                        placeholder = { Text("Saya menyadari bahwa strategi PAI harus menyesuaikan gaya belajar siswa dan nilai lokal...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reflection_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (submitted) {
                    OutlinedButton(
                        onClick = onRetryQuiz,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ulangi Kuis", textAlign = TextAlign.Center, maxLines = 2)
                    }
                }
                Button(
                    onClick = { onSubmitEvaluation(reflectionText) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .heightIn(min = 52.dp)
                        .testTag("submit_evaluation_button")
                ) {
                    Text("Simpan & Lihat Hasil Akhir", textAlign = TextAlign.Center, maxLines = 2)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HasilPembelajaranScreen(
    progress: StudentProgress,
    onReviewStage: (LearningStage) -> Unit,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 780.dp)) {
            // Score Banner Card
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                border = BorderStroke(1.5.dp, GoldAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Laporan Hasil Pembelajaran",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = progress.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = GoldContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SKOR AKHIR EVALUASI", style = MaterialTheme.typography.labelSmall, color = GoldDarkText)
                            Text(
                                text = "${progress.quizScore} / 100",
                                style = MaterialTheme.typography.displaySmall,
                                color = EmeraldDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            ResultSectionCard(
                title = "Strategi Pembelajaran yang Berhasil Dipilih",
                body = if (progress.selectedStrategies.isEmpty())
                    "Belum ada strategi tercatat. Kunjungi Tahap 7 untuk memilih strategi."
                else
                    progress.selectedStrategies.joinToString(separator = "\n• ", prefix = "• ")
            )

            ResultSectionCard(
                title = "Hasil Analisis Kasus & Eksplorasi Kelas 3D",
                body = buildString {
                    appendLine("Objek Virtual 3D dieksplorasi: ${progress.exploredObjects.size}/5")
                    appendLine("Analisis Karakteristik Siswa: ${if (progress.studentAnalysisDone) "Selesai" else "Belum selesai"}")
                    appendLine("Integrasi Kearifan Lokal: ${if (progress.localWisdomDone) "Selesai" else "Belum selesai"}")
                    if (progress.caseAnalysisSummary.isNotBlank()) {
                        appendLine()
                        append(progress.caseAnalysisSummary)
                    }
                }
            )

            ResultSectionCard(
                title = "Refleksi Pembelajaran Mahasiswa",
                body = progress.reflectionText.ifBlank { "Belum ada refleksi tersimpan." }
            )

            ResultSectionCard(
                title = "Rekomendasi Pengembangan Kompetensi & Materi Lanjut",
                body = progress.competencyRecommendation.ifBlank {
                    "Selesaikan evaluasi pada Tahap 9 untuk memperoleh rekomendasi pengembangan kompetensi personal."
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onReviewStage(LearningStage.LAB_GEN_AI) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                ) {
                    Text("Kembali ke Lab AI", textAlign = TextAlign.Center, maxLines = 2)
                }
                Button(
                    onClick = onBackToHome,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .testTag("back_to_home_button")
                ) {
                    Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Beranda", textAlign = TextAlign.Center, maxLines = 2)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResultSectionCard(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

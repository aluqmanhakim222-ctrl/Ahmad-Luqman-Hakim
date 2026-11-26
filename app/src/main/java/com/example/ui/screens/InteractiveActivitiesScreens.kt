package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LearningContentCatalog
import com.example.ui.components.ActivityCompletionIndicatorCard
import com.example.ui.theme.ErrorContainerLight
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen

@Composable
fun AnalisisSiswaScreen(
    selections: Map<String, String>,
    onSelectOption: (scenarioId: String, optionId: String) -> Unit,
    onContinueToLocalWisdom: () -> Unit
) {
    val scenarios = LearningContentCatalog.studentScenarios
    val completedCount = scenarios.count { selections.containsKey(it.id) }
    val optimalCount = scenarios.count { selections[it.id] == it.bestOptionId }
    val allAnswered = completedCount >= scenarios.size

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
                    text = "INTERAKSI AKTIF 1 • ANALISIS KARAKTERISTIK PESERTA DIDIK",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldDarkText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Analisis Kebutuhan Belajar Kelas PAI",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Amati karakteristik 4 profil peserta didik berikut (Visual, Auditori, Kinestetik, dan Kemampuan Akademik Beragam), lalu pilih intervensi pedagogis paling tepat untuk melihat umpan balik.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActivityCompletionIndicatorCard(
                activityTitle = "Indikator Penyelesaian Interaksi 1",
                completedCount = completedCount,
                totalCount = scenarios.size,
                optimalCount = optimalCount,
                isCompleted = allAnswered,
                testTagPrefix = "activity_student"
            )

            Spacer(modifier = Modifier.height(12.dp))

            scenarios.forEach { scenario ->
                val chosenId = selections[scenario.id]
                val chosenOpt = scenario.options.firstOrNull { it.id == chosenId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.PersonSearch,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(scenario.studentName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Tipe: ${scenario.learningStyle}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldDarkText
                                    )
                                }
                            }
                            if (chosenOpt != null) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Sudah dipilih",
                                    tint = if (chosenOpt.id == scenario.bestOptionId) SuccessGreen else GoldDarkText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Profil Akademik: ${scenario.academicProfile}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Observasi Kelas: ${scenario.classroomBehavior}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(10.dp))

                        scenario.options.forEach { opt ->
                            val isPicked = chosenId == opt.id
                            val isBest = opt.id == scenario.bestOptionId
                            val container = when {
                                isPicked && isBest -> SuccessContainer
                                isPicked && !isBest -> ErrorContainerLight
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            }
                            Card(
                                onClick = { onSelectOption(scenario.id, opt.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .testTag("student_${scenario.id}_${opt.id}"),
                                colors = CardDefaults.cardColors(containerColor = container),
                                border = if (isPicked) BorderStroke(1.5.dp, if (isBest) SuccessGreen else ErrorCrimson) else null
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(opt.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (chosenOpt != null) {
                            val isBest = chosenOpt.id == scenario.bestOptionId
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (isBest) SuccessContainer else GoldContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("feedback_student_${scenario.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = if (isBest) Icons.Filled.CheckCircle else Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = if (isBest) SuccessGreen else GoldDarkText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isBest) "Respons Pilihan: Tepat" else "Respons Pilihan: Perlu Penyesuaian",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBest) SuccessGreen else GoldDarkText
                                        )
                                        Text(
                                            text = "Umpan Balik: ${chosenOpt.rationale}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isBest) SuccessGreen else GoldDarkText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueToLocalWisdom,
                enabled = allAnswered,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("continue_local_wisdom_button")
            ) {
                Text(
                    if (allAnswered) "Lanjut ke Tahap 6: Integrasi Kearifan Lokal"
                    else "Selesaikan $completedCount/${scenarios.size} Profil Siswa"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun KearifanLokalScreen(
    selections: Map<String, String>,
    onSelectOption: (scenarioId: String, optionId: String) -> Unit,
    onContinueToStrategyChoice: () -> Unit
) {
    val scenarios = LearningContentCatalog.localWisdomScenarios
    val completedCount = scenarios.count { selections.containsKey(it.id) }
    val optimalCount = scenarios.count { selections[it.id] == it.bestOptionId }
    val allAnswered = completedCount >= scenarios.size

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
                    text = "INTERAKSI AKTIF 2 • INTEGRASI KEARIFAN LOKAL INDONESIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldDarkText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Kontekstualisasi Budaya Nusantara dalam PAI",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActivityCompletionIndicatorCard(
                activityTitle = "Indikator Penyelesaian Interaksi 2",
                completedCount = completedCount,
                totalCount = scenarios.size,
                optimalCount = optimalCount,
                isCompleted = allAnswered,
                testTagPrefix = "activity_local_wisdom"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Image(
                painter = painterResource(id = R.drawable.img_batik_pesantren),
                contentDescription = "Ilustrasi integrasi batik dan tradisi pesantren",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            scenarios.forEach { scenario ->
                val chosenId = selections[scenario.id]
                val chosenOpt = scenario.options.firstOrNull { it.id == chosenId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(scenario.themeTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Text("Materi PAI: ${scenario.paiTopic}", style = MaterialTheme.typography.labelMedium, color = GoldDarkText)
                            }
                            if (chosenOpt != null) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Sudah dipilih",
                                    tint = if (chosenOpt.id == scenario.bestOptionId) SuccessGreen else GoldDarkText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(scenario.culturalContext, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))

                        scenario.options.forEach { opt ->
                            val isPicked = chosenId == opt.id
                            val isBest = opt.id == scenario.bestOptionId
                            val container = when {
                                isPicked && isBest -> SuccessContainer
                                isPicked && !isBest -> ErrorContainerLight
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            }
                            Card(
                                onClick = { onSelectOption(scenario.id, opt.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .testTag("local_${scenario.id}_${opt.id}"),
                                colors = CardDefaults.cardColors(containerColor = container),
                                border = if (isPicked) BorderStroke(1.5.dp, if (isBest) SuccessGreen else ErrorCrimson) else null
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(opt.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (chosenOpt != null) {
                            val isBest = chosenOpt.id == scenario.bestOptionId
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (isBest) SuccessContainer else GoldContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("feedback_local_${scenario.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = if (isBest) Icons.Filled.CheckCircle else Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = if (isBest) SuccessGreen else GoldDarkText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isBest) "Respons Integrasi: Tepat" else "Respons Integrasi: Kurang Kontekstual",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBest) SuccessGreen else GoldDarkText
                                        )
                                        Text(
                                            text = "Umpan Balik: ${chosenOpt.rationale}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isBest) SuccessGreen else GoldDarkText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueToStrategyChoice,
                enabled = allAnswered,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("continue_strategy_choice_button")
            ) {
                Text(
                    if (allAnswered) "Lanjut ke Tahap 7: Pemilihan Strategi"
                    else "Pilih Integrasi untuk $completedCount/${scenarios.size} Konteks"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PemilihanStrategiScreen(
    selections: Map<String, String>,
    onSelectStrategy: (caseId: String, strategyId: String) -> Unit,
    onContinueToGenAiLab: () -> Unit
) {
    val cases = LearningContentCatalog.strategyCases
    val completedCount = cases.count { selections.containsKey(it.id) }
    val optimalCount = cases.count { selections[it.id] == it.bestStrategyId }
    val allAnswered = completedCount >= cases.size

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
                    text = "INTERAKSI AKTIF 3 • PEMILIHAN STRATEGI PEMBELAJARAN PAI",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldDarkText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pilih Strategi Paling Tepat Berdasarkan Kasus",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tentukan strategi terbaik dari 6 pendekatan (PBL, PjBL, Cooperative Learning, CTL, Demonstrasi, Diskusi Interaktif) dan pelajari umpan balik pedagogisnya.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActivityCompletionIndicatorCard(
                activityTitle = "Indikator Penyelesaian Interaksi 3",
                completedCount = completedCount,
                totalCount = cases.size,
                optimalCount = optimalCount,
                isCompleted = allAnswered,
                testTagPrefix = "activity_strategy"
            )

            Spacer(modifier = Modifier.height(12.dp))

            cases.forEach { caseItem ->
                val chosenId = selections[caseItem.id]
                val chosenOpt = caseItem.options.firstOrNull { it.id == chosenId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = caseItem.caseTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            if (chosenOpt != null) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Kasus dijawab",
                                    tint = if (chosenOpt.id == caseItem.bestStrategyId) SuccessGreen else GoldDarkText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(caseItem.classroomProblem, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(10.dp))

                        caseItem.options.forEach { opt ->
                            val isPicked = chosenId == opt.id
                            val isBest = opt.id == caseItem.bestStrategyId
                            val container = when {
                                isPicked && isBest -> SuccessContainer
                                isPicked && !isBest -> ErrorContainerLight
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                            Card(
                                onClick = { onSelectStrategy(caseItem.id, opt.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .testTag("strategy_${caseItem.id}_${opt.id}"),
                                colors = CardDefaults.cardColors(containerColor = container),
                                border = if (isPicked) BorderStroke(1.5.dp, if (isBest) SuccessGreen else ErrorCrimson) else null
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(opt.strategyName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                        if (isPicked && isBest) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Text(opt.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (chosenOpt != null) {
                            val isBest = chosenOpt.id == caseItem.bestStrategyId
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (isBest) SuccessContainer else GoldContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("feedback_strategy_${caseItem.id}")
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = if (isBest) Icons.Filled.CheckCircle else Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = if (isBest) SuccessGreen else GoldDarkText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Respons Pilihan: ${chosenOpt.strategyName} (${if (isBest) "Paling Tepat" else "Alternatif Kurang Optimal"})",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBest) SuccessGreen else GoldDarkText
                                        )
                                        Text(
                                            text = "Umpan Balik Pedagogis: ${chosenOpt.feedbackIfChosen}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isBest) SuccessGreen else GoldDarkText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueToGenAiLab,
                enabled = allAnswered,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("continue_genai_lab_button")
            ) {
                Text(
                    if (allAnswered) "Lanjut ke Tahap 8: Laboratorium Generative AI"
                    else "Selesaikan $completedCount/${cases.size} Pemilihan Strategi"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

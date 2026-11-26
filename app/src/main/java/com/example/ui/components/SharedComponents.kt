package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.LearningStage
import com.example.model.ProgressNotification
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaiTopAppBar(
    currentStage: LearningStage,
    unreadCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenGuide: () -> Unit,
    onSignOut: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = EmeraldDark,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            Column {
                Text(
                    text = "PAI IMMERSIVE LEARNING",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tahap ${currentStage.number}/10 • ${currentStage.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            IconButton(
                onClick = onOpenNotifications,
                modifier = Modifier.testTag("notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = GoldAccent, contentColor = GoldDarkText) {
                                Text(text = unreadCount.coerceAtMost(9).toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (unreadCount > 0) Icons.Filled.NotificationsActive else Icons.Filled.Notifications,
                        contentDescription = "Notifikasi Progres Real-Time"
                    )
                }
            }
            IconButton(
                onClick = onOpenGuide,
                modifier = Modifier.testTag("top_guide_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Petunjuk Penggunaan"
                )
            }
            IconButton(
                onClick = onSignOut,
                modifier = Modifier.testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Keluar Akun"
                )
            }
        }
    )
}

@Composable
fun StageProgressStepper(
    currentStage: LearningStage,
    highestUnlockedStage: Int,
    completedStages: List<Long>,
    onStageClick: (LearningStage) -> Unit
) {
    val progressFraction = (completedStages.distinct().size.toFloat() / 10f).coerceIn(0.05f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 8.dp, bottom = 6.dp)
    ) {
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = GoldAccent,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LearningStage.entries.forEach { stage ->
                val isUnlocked = stage.number <= highestUnlockedStage
                val isCompleted = completedStages.contains(stage.number.toLong())
                val isActive = stage == currentStage

                val bg = when {
                    isActive -> MaterialTheme.colorScheme.primary
                    isCompleted -> SuccessContainer
                    isUnlocked -> GoldContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val fg = when {
                    isActive -> MaterialTheme.colorScheme.onPrimary
                    isCompleted -> SuccessGreen
                    isUnlocked -> GoldDarkText
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val borderColor = if (isActive) GoldAccent else fg.copy(alpha = 0.25f)

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(bg)
                        .border(1.dp, borderColor, RoundedCornerShape(999.dp))
                        .clickable { onStageClick(stage) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("stage_chip_${stage.number}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCompleted && !isActive) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = fg,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    } else if (!isUnlocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = fg,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "${stage.number}. ${stage.shortTitle}",
                        style = MaterialTheme.typography.labelMedium,
                        color = fg
                    )
                }
            }
        }
    }
}

@Composable
fun RealTimeProgressTicker(
    latestNotification: ProgressNotification?,
    statusBanner: String?,
    onDismissStatus: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    AnimatedVisibility(visible = statusBanner != null || latestNotification != null) {
        val bannerText = statusBanner
        if (bannerText != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bannerText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissStatus, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tutup pesan",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else if (latestNotification != null && !latestNotification.readFlag) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onOpenNotifications() },
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Progres Real-Time: ${latestNotification.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<ProgressNotification>,
    onMarkRead: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Notifikasi Progres Real-Time",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Pembaruan capaian belajar tersinkronisasi langsung dari cloud.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (notifications.isEmpty()) {
                Text(
                    text = "Belum ada notifikasi aktivitas. Mulailah menyelesaikan tahap pembelajaran.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        Card(
                            onClick = { if (!item.readFlag && item.id.isNotBlank()) onMarkRead(item.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.readFlag) MaterialTheme.colorScheme.surfaceVariant
                                else GoldContainer.copy(alpha = 0.65f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tahap ${item.stageNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.message,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Petunjuk Penggunaan Laboratorium",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("1. Ikuti alur 10 tahap secara berurutan. Tahap yang sudah terbuka dapat ditinjau ulang kapan saja melalui bilah tahap di bagian atas.")
                Text("2. Pada Ruang Kelas Virtual 3D, geser layar untuk menjelajah ruang madrasah, ketuk objek (Papan Tulis, Mushaf, Kitab, Batik, Meja Siswa) untuk analisis, dan gunakan tombol Mode VR bila perangkat mendukung WebXR.")
                Text("3. Selesaikan 3 interaksi pedagogis (Analisis Siswa, Kearifan Lokal, Pemilihan Strategi) untuk memperoleh umpan balik langsung.")
                Text("4. Manfaatkan Laboratorium Generative AI (didukung Gemini AI melalui Firebase AI Logic) untuk studi kasus baru yang selalu unik, 5 soal kuis adaptif, analisis pedagogis jawaban mahasiswa, dan tanya AI Tutor.")
                HorizontalDivider()
                Text(
                    text = "Seluruh progres, skor kuis, dan refleksi tersimpan otomatis ke akun Google Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Mengerti")
            }
        }
    )
}

@Composable
fun ActivityCompletionIndicatorCard(
    activityTitle: String,
    completedCount: Int,
    totalCount: Int,
    optimalCount: Int? = null,
    isCompleted: Boolean = completedCount >= totalCount && totalCount > 0,
    testTagPrefix: String = "activity_completion"
) {
    val safeTotal = totalCount.coerceAtLeast(1)
    val fraction = (completedCount.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
    val badgeBg = when {
        isCompleted -> SuccessContainer
        completedCount > 0 -> GoldContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val badgeFg = when {
        isCompleted -> SuccessGreen
        completedCount > 0 -> GoldDarkText
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val badgeText = when {
        isCompleted -> "Aktivitas Selesai"
        completedCount > 0 -> "Sedang Dikerjakan ($completedCount/$totalCount)"
        else -> "Belum Dimulai (0/$totalCount)"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) SuccessContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isCompleted) SuccessGreen else GoldAccent.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activityTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.testTag("${testTagPrefix}_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = badgeFg,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeFg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = if (isCompleted) SuccessGreen else GoldAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (optimalCount != null && completedCount > 0) {
                    "Progres: $completedCount/$totalCount selesai • Pilihan optimal: $optimalCount/$totalCount"
                } else {
                    "Progres penyelesaian aktivitas: $completedCount/$totalCount"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

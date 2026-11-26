package com.example.ui.screens

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.LearningContentCatalog
import com.example.model.ClassroomObjectItem
import com.example.model.LearningStage
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen

class ClassroomJsBridge(
    private val onSelect: (String) -> Unit
) {
    @JavascriptInterface
    fun onObjectSelected(objectId: String) {
        onSelect(objectId)
    }
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun VirtualClassroomScreen(
    currentStage: LearningStage,
    exploredObjects: List<String>,
    selectedObjectId: String?,
    onObjectSelected: (String) -> Unit,
    onContinueToExploration: () -> Unit,
    onContinueToStudentAnalysis: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var expanded3dView by remember { mutableStateOf(false) }

    val selectedObject: ClassroomObjectItem? = remember(selectedObjectId) {
        LearningContentCatalog.classroomObjects.firstOrNull { it.id == selectedObjectId }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 840.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentStage == LearningStage.RUANG_KELAS_3D)
                            "Ruang Kelas Virtual 3D (First-Person A-Frame)"
                        else
                            "Eksplorasi Objek Pembelajaran 3D",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Geser untuk melihat sekeliling, gunakan panah/WASD untuk mendekati objek, dan ketuk objek di ruang 3D (${exploredObjects.size}/5 dieksplorasi).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { expanded3dView = !expanded3dView },
                    modifier = Modifier.testTag("toggle_fullscreen_3d")
                ) {
                    Icon(
                        imageVector = if (expanded3dView) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = if (expanded3dView) "Perkecil Tampilan 3D" else "Perbesar Tampilan 3D",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Viewport A-Frame 3D First-Person dengan Touch Interception Fix
            Card(
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.5.dp, GoldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (expanded3dView) 520.dp else 380.dp)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.large)
                        .testTag("virtual_classroom_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            settings.mediaPlaybackRequiresUserGesture = false
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            // Mencegah parent ScrollView mencuri gesture drag 3D look-around
                            setOnTouchListener { v, event ->
                                when (event.actionMasked) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(true)
                                    }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(false)
                                    }
                                }
                                false
                            }
                            addJavascriptInterface(
                                ClassroomJsBridge { objId ->
                                    post { onObjectSelected(objId) }
                                },
                                "AndroidBridge"
                            )
                            loadUrl("file:///android_asset/virtual_classroom.html")
                            webViewRef = this
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = GoldContainer.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ViewInAr,
                        contentDescription = null,
                        tint = GoldDarkText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tombol Enter VR muncul otomatis di kanan atas kanvas apabila perangkat mendukung WebXR; jika tidak, navigasi First-Person 3D dengan sentuh/mouse tetap aktif penuh.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldDarkText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            com.example.ui.components.ActivityCompletionIndicatorCard(
                activityTitle = "Indikator Eksplorasi Objek Virtual 3D",
                completedCount = exploredObjects.size,
                totalCount = LearningContentCatalog.classroomObjects.size,
                isCompleted = exploredObjects.size >= 3,
                testTagPrefix = "activity_3d_objects"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Daftar Objek Pembelajaran 3D (Ketuk untuk Mendekati)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LearningContentCatalog.classroomObjects.forEach { obj ->
                val isExplored = exploredObjects.contains(obj.id)
                val isSelected = selectedObjectId == obj.id
                Card(
                    onClick = {
                        onObjectSelected(obj.id)
                        webViewRef?.evaluateJavascript("selectObject('${obj.id}')", null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("object_card_${obj.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isExplored) Icons.Filled.CheckCircle else Icons.Filled.TouchApp,
                            contentDescription = null,
                            tint = if (isExplored) SuccessGreen else GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(obj.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = obj.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isExplored) {
                            Surface(
                                color = SuccessContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Ditinjau",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedObject != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Detail Objek: ${selectedObject.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(selectedObject.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Nilai Pedagogis: ${selectedObject.pedagogicalValue}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rekomendasi Strategi: ${selectedObject.suggestedStrategy}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentStage == LearningStage.RUANG_KELAS_3D) {
                Button(
                    onClick = onContinueToExploration,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("continue_object_exploration_button")
                ) {
                    Text("Lanjut ke Tahap 4: Eksplorasi Objek")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                val readyForNext = exploredObjects.size >= 3
                if (!readyForNext) {
                    Text(
                        text = "Eksplorasi minimal 3 objek kelas virtual (${exploredObjects.size}/3) untuk membuka Interaksi 1.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = onContinueToStudentAnalysis,
                    enabled = readyForNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("continue_student_analysis_button")
                ) {
                    Text("Lanjut ke Tahap 5: Analisis Karakteristik Siswa")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
                if (!readyForNext) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onObjectSelected("papan_tulis")
                            webViewRef?.evaluateJavascript("selectObject('papan_tulis')", null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dekati & Tinjau Papan Tulis Interaktif")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

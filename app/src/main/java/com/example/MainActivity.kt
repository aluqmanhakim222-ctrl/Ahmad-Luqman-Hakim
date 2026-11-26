package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.AiLabRepository
import com.example.data.LearningProgressRepository
import com.example.firebase.attemptAutoSignIn
import com.example.firebase.authStateFlow
import com.example.firebase.configureAppCheck
import com.example.firebase.signOutUser
import com.example.model.LearningStage
import com.example.model.StudentProgress
import com.example.ui.LearningViewModel
import com.example.ui.ProgressUiState
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.PaiTopAppBar
import com.example.ui.components.RealTimeProgressTicker
import com.example.ui.components.StageProgressStepper
import com.example.ui.components.UsageGuideDialog
import com.example.ui.screens.AnalisisSiswaScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.EvaluasiRefleksiScreen
import com.example.ui.screens.HasilPembelajaranScreen
import com.example.ui.screens.KearifanLokalScreen
import com.example.ui.screens.LaboratoriumGenAiScreen
import com.example.ui.screens.PemilihanStrategiScreen
import com.example.ui.screens.TujuanPembelajaranScreen
import com.example.ui.screens.VirtualClassroomScreen
import com.example.ui.theme.PaiImmersiveTheme
import com.example.util.SystemNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory Firebase App Check wiring with launch intent BEFORE UI/ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            PaiImmersiveTheme {
                PaiImmersiveAppRoot()
            }
        }
    }
}

@Composable
fun PaiImmersiveAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = CredentialManager.create(context),
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        val uid = user.uid
        val displayName = user.displayName ?: "Mahasiswa PAI"
        key(uid) {
            val viewModel: LearningViewModel = viewModel(
                key = uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val dbId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(dbId)
                        LearningViewModel(
                            progressRepo = LearningProgressRepository(db, Firebase.auth),
                            aiLabRepo = AiLabRepository(),
                            currentUserId = uid,
                            currentDisplayName = displayName
                        )
                    }
                }
            )
            MainLearningShell(
                viewModel = viewModel,
                onSignOut = {
                    signOutUser(
                        credentialManager = CredentialManager.create(context),
                        scope = scope
                    )
                }
            )
        }
    }
}

@Composable
fun MainLearningShell(
    viewModel: LearningViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val progressUiState by viewModel.progressState.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val aiRecords by viewModel.aiRecords.collectAsStateWithLifecycle()
    val currentStage by viewModel.currentStage.collectAsStateWithLifecycle()
    val selectedObjectId by viewModel.selectedObjectId.collectAsStateWithLifecycle()
    val studentSelections by viewModel.studentSelections.collectAsStateWithLifecycle()
    val localWisdomSelections by viewModel.localWisdomSelections.collectAsStateWithLifecycle()
    val strategySelections by viewModel.strategySelections.collectAsStateWithLifecycle()
    val aiUiState by viewModel.aiUiState.collectAsStateWithLifecycle()
    val adaptiveQuizState by viewModel.adaptiveQuizState.collectAsStateWithLifecycle()
    val difficulty by viewModel.difficultyLevel.collectAsStateWithLifecycle()
    val evaluationAnswers by viewModel.evaluationAnswers.collectAsStateWithLifecycle()
    val evaluationSubmitted by viewModel.evaluationSubmitted.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val milestoneEvent by viewModel.milestoneEvent.collectAsStateWithLifecycle()

    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(milestoneEvent) {
        milestoneEvent?.let { (title, message) ->
            SystemNotificationHelper.showProgressMilestone(
                context = context,
                title = title,
                message = message,
                id = title.hashCode()
            )
            viewModel.consumeMilestoneEvent()
        }
    }

    // BackHandler for smooth step-by-step back navigation across the 10 stages
    BackHandler(enabled = currentStage != LearningStage.BERANDA) {
        val prev = LearningStage.fromNumber((currentStage.number - 1).coerceAtLeast(1))
        viewModel.navigateToStage(prev)
    }

    val progress: StudentProgress = when (val state = progressUiState) {
        is ProgressUiState.Success -> state.progress
        else -> StudentProgress()
    }
    val unreadCount = notifications.count { !it.readFlag }

    Scaffold(
        topBar = {
            Column {
                PaiTopAppBar(
                    currentStage = currentStage,
                    unreadCount = unreadCount,
                    onOpenNotifications = { showNotificationsSheet = true },
                    onOpenGuide = { showGuideDialog = true },
                    onSignOut = onSignOut
                )
                StageProgressStepper(
                    currentStage = currentStage,
                    highestUnlockedStage = progress.highestUnlockedStage.toInt().coerceIn(1, 10),
                    completedStages = progress.completedStages,
                    onStageClick = { stage -> viewModel.navigateToStage(stage) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RealTimeProgressTicker(
                latestNotification = notifications.firstOrNull(),
                statusBanner = statusBanner,
                onDismissStatus = { viewModel.clearStatusBanner() },
                onOpenNotifications = { showNotificationsSheet = true }
            )

            if (progressUiState is ProgressUiState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = "Memuat progres pembelajaran...",
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            } else {
                when (currentStage) {
                    LearningStage.BERANDA -> BerandaScreen(
                        progress = progress,
                        onStartLearning = { viewModel.completeStageAndAdvance(LearningStage.BERANDA) },
                        onOpenGuide = { showGuideDialog = true },
                        onJumpStage = { stage -> viewModel.navigateToStage(stage) }
                    )
                    LearningStage.TUJUAN_PEMBELAJARAN -> TujuanPembelajaranScreen(
                        completedStages = progress.completedStages,
                        onCompleteObjectives = { viewModel.completeStageAndAdvance(LearningStage.TUJUAN_PEMBELAJARAN) }
                    )
                    LearningStage.RUANG_KELAS_3D, LearningStage.EKSPLORASI_OBJEK -> VirtualClassroomScreen(
                        currentStage = currentStage,
                        exploredObjects = progress.exploredObjects,
                        selectedObjectId = selectedObjectId,
                        onObjectSelected = { id -> viewModel.onObjectSelectedFrom3D(id) },
                        onContinueToExploration = { viewModel.completeStageAndAdvance(LearningStage.RUANG_KELAS_3D) },
                        onContinueToStudentAnalysis = { viewModel.completeStageAndAdvance(LearningStage.EKSPLORASI_OBJEK) }
                    )
                    LearningStage.ANALISIS_SISWA -> AnalisisSiswaScreen(
                        selections = studentSelections,
                        onSelectOption = { sid, oid -> viewModel.selectStudentOption(sid, oid) },
                        onContinueToLocalWisdom = { viewModel.completeStageAndAdvance(LearningStage.ANALISIS_SISWA) }
                    )
                    LearningStage.KEARIFAN_LOKAL -> KearifanLokalScreen(
                        selections = localWisdomSelections,
                        onSelectOption = { sid, oid -> viewModel.selectLocalWisdomOption(sid, oid) },
                        onContinueToStrategyChoice = { viewModel.completeStageAndAdvance(LearningStage.KEARIFAN_LOKAL) }
                    )
                    LearningStage.PEMILIHAN_STRATEGI -> PemilihanStrategiScreen(
                        selections = strategySelections,
                        onSelectStrategy = { cid, sid -> viewModel.selectStrategyOption(cid, sid) },
                        onContinueToGenAiLab = { viewModel.completeStageAndAdvance(LearningStage.PEMILIHAN_STRATEGI) }
                    )
                    LearningStage.LAB_GEN_AI -> LaboratoriumGenAiScreen(
                        aiUiState = aiUiState,
                        adaptiveQuizState = adaptiveQuizState,
                        difficulty = difficulty,
                        stageStrategySummary = remember(studentSelections, localWisdomSelections, strategySelections) {
                            viewModel.buildStageStrategySummary()
                        },
                        aiRecords = aiRecords,
                        onSelectDifficulty = { viewModel.setDifficulty(it) },
                        onGenerateCaseStudy = { viewModel.generateCaseStudy(it) },
                        onGenerateQuiz = { viewModel.generateAdaptiveQuiz(it) },
                        onAnalyzeStrategy = { topic, answer -> viewModel.analyzeStrategy(topic, answer) },
                        onAskAiTutor = { viewModel.askAiTutor(it) },
                        onRetryAiTask = { tool, topic, answer -> viewModel.retryLastAiTask(tool, topic, answer) },
                        onAnswerAdaptiveQuiz = { qIdx, optIdx -> viewModel.answerAdaptiveQuiz(qIdx, optIdx) },
                        onContinueToEvaluation = { viewModel.completeStageAndAdvance(LearningStage.LAB_GEN_AI) }
                    )
                    LearningStage.EVALUASI_REFLEKSI -> EvaluasiRefleksiScreen(
                        answers = evaluationAnswers,
                        submitted = evaluationSubmitted,
                        initialReflection = progress.reflectionText,
                        onSelectAnswer = { qIdx, optIdx -> viewModel.selectEvaluationAnswer(qIdx, optIdx) },
                        onRetryQuiz = { viewModel.resetEvaluationForRetry() },
                        onSubmitEvaluation = { reflection -> viewModel.submitEvaluationAndReflection(reflection) }
                    )
                    LearningStage.HASIL_PEMBELAJARAN -> HasilPembelajaranScreen(
                        progress = progress,
                        onReviewStage = { stage -> viewModel.navigateToStage(stage) },
                        onBackToHome = { viewModel.navigateToStage(LearningStage.BERANDA) }
                    )
                }
            }
        }
    }

    if (showNotificationsSheet) {
        NotificationsSheet(
            notifications = notifications,
            onMarkRead = { id -> viewModel.markNotificationRead(id) },
            onDismiss = { showNotificationsSheet = false }
        )
    }

    if (showGuideDialog) {
        UsageGuideDialog(onDismiss = { showGuideDialog = false })
    }
}

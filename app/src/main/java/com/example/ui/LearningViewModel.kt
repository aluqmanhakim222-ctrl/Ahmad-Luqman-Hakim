package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiLabRepository
import com.example.data.LearningContentCatalog
import com.example.data.LearningProgressRepository
import com.example.model.AdaptiveAiQuizQuestion
import com.example.model.AiLabRecord
import com.example.model.AiSourceMode
import com.example.model.AiToolType
import com.example.model.DifficultyLevel
import com.example.model.LearningStage
import com.example.model.ProgressNotification
import com.example.model.StudentProgress
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "LearningViewModel"

sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data class Success(val progress: StudentProgress) : ProgressUiState
    data class Error(val message: String) : ProgressUiState
}

sealed interface AiLabUiState {
    data object Idle : AiLabUiState
    data class Loading(val toolType: AiToolType) : AiLabUiState
    data class Streaming(
        val toolType: AiToolType,
        val text: String,
        val sourceMode: AiSourceMode
    ) : AiLabUiState

    data class Success(
        val toolType: AiToolType,
        val text: String,
        val sourceMode: AiSourceMode,
        val difficulty: DifficultyLevel
    ) : AiLabUiState

    data class Error(
        val toolType: AiToolType,
        val message: String,
        val topicInput: String,
        val studentAnswerInput: String = ""
    ) : AiLabUiState
}

sealed interface AdaptiveQuizUiState {
    data object Idle : AdaptiveQuizUiState
    data object Loading : AdaptiveQuizUiState
    data class Ready(
        val topicFocus: String,
        val difficulty: DifficultyLevel,
        val questions: List<AdaptiveAiQuizQuestion>,
        val selectedAnswers: Map<Int, Int> = emptyMap()
    ) : AdaptiveQuizUiState

    data class Error(val message: String, val topicFocus: String) : AdaptiveQuizUiState
}

class LearningViewModel(
    private val progressRepo: LearningProgressRepository,
    private val aiLabRepo: AiLabRepository,
    private val currentUserId: String,
    private val currentDisplayName: String
) : ViewModel() {

    private val _localProgress = MutableStateFlow(
        StudentProgress(
            userId = currentUserId,
            displayName = currentDisplayName.ifBlank { "Mahasiswa PAI" }
        )
    )

    val progressState: StateFlow<ProgressUiState> = combine(
        progressRepo.observeProgress(currentUserId),
        _localProgress
    ) { cloud, local ->
        val merged = if (cloud == null) {
            local
        } else {
            cloud.copy(
                displayName = cloud.displayName.ifBlank { local.displayName },
                highestUnlockedStage = maxOf(cloud.highestUnlockedStage, local.highestUnlockedStage),
                completedStages = (cloud.completedStages + local.completedStages).distinct().sorted(),
                exploredObjects = (cloud.exploredObjects + local.exploredObjects).distinct(),
                selectedStrategies = if (local.selectedStrategies.isNotEmpty()) local.selectedStrategies else cloud.selectedStrategies,
                studentAnalysisDone = cloud.studentAnalysisDone || local.studentAnalysisDone,
                localWisdomDone = cloud.localWisdomDone || local.localWisdomDone,
                strategyChoiceDone = cloud.strategyChoiceDone || local.strategyChoiceDone,
                caseAnalysisSummary = local.caseAnalysisSummary.ifBlank { cloud.caseAnalysisSummary },
                quizCompleted = cloud.quizCompleted || local.quizCompleted,
                quizScore = if (local.quizCompleted) local.quizScore else cloud.quizScore,
                reflectionText = local.reflectionText.ifBlank { cloud.reflectionText },
                competencyRecommendation = local.competencyRecommendation.ifBlank { cloud.competencyRecommendation }
            )
        }
        ProgressUiState.Success(merged) as ProgressUiState
    }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing student progress", rootError)
            emit(ProgressUiState.Success(_localProgress.value))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ProgressUiState.Loading
        )

    val notifications: StateFlow<List<ProgressNotification>> = progressRepo.observeNotifications(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing notifications", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val aiRecords: StateFlow<List<AiLabRecord>> = progressRepo.observeAiRecords(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing AI records", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList())

    private val _currentStage = MutableStateFlow(LearningStage.BERANDA)
    val currentStage: StateFlow<LearningStage> = _currentStage.asStateFlow()

    private val _selectedObjectId = MutableStateFlow<String?>(null)
    val selectedObjectId: StateFlow<String?> = _selectedObjectId.asStateFlow()

    private val _studentSelections = MutableStateFlow<Map<String, String>>(emptyMap())
    val studentSelections: StateFlow<Map<String, String>> = _studentSelections.asStateFlow()

    private val _localWisdomSelections = MutableStateFlow<Map<String, String>>(emptyMap())
    val localWisdomSelections: StateFlow<Map<String, String>> = _localWisdomSelections.asStateFlow()

    private val _strategySelections = MutableStateFlow<Map<String, String>>(emptyMap())
    val strategySelections: StateFlow<Map<String, String>> = _strategySelections.asStateFlow()

    private val _aiUiState = MutableStateFlow<AiLabUiState>(AiLabUiState.Idle)
    val aiUiState: StateFlow<AiLabUiState> = _aiUiState.asStateFlow()

    private val _adaptiveQuizState = MutableStateFlow<AdaptiveQuizUiState>(AdaptiveQuizUiState.Idle)
    val adaptiveQuizState: StateFlow<AdaptiveQuizUiState> = _adaptiveQuizState.asStateFlow()

    private val _difficultyLevel = MutableStateFlow(DifficultyLevel.MENENGAH)
    val difficultyLevel: StateFlow<DifficultyLevel> = _difficultyLevel.asStateFlow()

    private val _evaluationAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val evaluationAnswers: StateFlow<Map<Int, Int>> = _evaluationAnswers.asStateFlow()

    private val _evaluationSubmitted = MutableStateFlow(false)
    val evaluationSubmitted: StateFlow<Boolean> = _evaluationSubmitted.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _milestoneEvent = MutableStateFlow<Pair<String, String>?>(null)
    val milestoneEvent: StateFlow<Pair<String, String>?> = _milestoneEvent.asStateFlow()

    private var adaptiveHistorySummary: String = "Belum ada riwayat kuis AI."
    private var caseStudyGenerationCount: Int = 0
    private val generatedCaseSummaries = mutableListOf<String>()
    private var lastGeneratedCaseStudy: String? = null
    private var quizGenerationCount: Int = 0
    private val generatedQuizQuestionsHistory = mutableListOf<String>()

    private val caseDiversitySeeds = listOf(
        "Madrasah Tsanawiyah (MTs) pedesaan • Materi Fikih Muamalah & Jual Beli Jujur • Konteks kearifan lokal gotong royong dan pasar rakyat",
        "Madrasah Aliyah (MA) perkotaan • Materi Akhlak Digital, Tabayyun & Moderasi Beragama • Konteks kelas heterogen pengguna media sosial aktif",
        "Pondok Pesantren Modern • Materi Al-Qur'an Hadis & Adab Menuntut Ilmu • Konteks tradisi halaqah, kitab klasik, dan diskusi kritis",
        "SMK Kejuruan Inklusif • Materi Sejarah Kebudayaan Islam (SKI) Nusantara & Nilai Walisongo • Konteks seni batik dan dakwah kultural",
        "Madrasah Ibtidaiyah (MI) / SD Islam • Materi Praktik Ibadah Shalat & Thaharah • Konteks siswa kinestetik aktif dan dukungan visual-peraga",
        "SMA Negeri Multikultural • Materi Fikih Zakat, Wakaf Produktif & Kepedulian Sosial • Konteks proyek sosial berbasis komunitas lokal"
    )

    init {
        viewModelScope.launch {
            val result = progressRepo.ensureProgressInitialized(currentUserId, currentDisplayName)
            result.onFailure {
                _statusBanner.value = "Mode tampilan lokal aktif sementara (sinkronisasi cloud tertunda)."
            }
        }
    }

    private fun currentProgressSnapshot(): StudentProgress {
        return (progressState.value as? ProgressUiState.Success)?.progress
            ?: StudentProgress(
                userId = currentUserId,
                displayName = currentDisplayName.ifBlank { "Mahasiswa PAI" }
            )
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun consumeMilestoneEvent() {
        _milestoneEvent.value = null
    }

    fun navigateToStage(stage: LearningStage) {
        val highest = currentProgressSnapshot().highestUnlockedStage.toInt().coerceIn(1, 10)
        if (stage.number <= highest) {
            _currentStage.value = stage
        } else {
            _statusBanner.value = "Selesaikan Tahap $highest terlebih dahulu untuk membuka ${stage.title}."
        }
    }

    private fun applyOptimisticStage(
        completedStage: Int,
        exploredObjects: List<String>? = null,
        selectedStrategies: List<String>? = null,
        studentAnalysisDone: Boolean? = null,
        localWisdomDone: Boolean? = null,
        strategyChoiceDone: Boolean? = null,
        caseAnalysisSummary: String? = null,
        quizScore: Long? = null,
        quizCompleted: Boolean? = null,
        reflectionText: String? = null,
        competencyRecommendation: String? = null
    ) {
        val cur = _localProgress.value
        val nextStage = maxOf(cur.highestUnlockedStage, (completedStage + 1).coerceAtMost(10).toLong())
        _localProgress.value = cur.copy(
            highestUnlockedStage = nextStage,
            completedStages = (cur.completedStages + completedStage.toLong()).distinct().sorted(),
            exploredObjects = exploredObjects ?: cur.exploredObjects,
            selectedStrategies = selectedStrategies ?: cur.selectedStrategies,
            studentAnalysisDone = studentAnalysisDone ?: cur.studentAnalysisDone,
            localWisdomDone = localWisdomDone ?: cur.localWisdomDone,
            strategyChoiceDone = strategyChoiceDone ?: cur.strategyChoiceDone,
            caseAnalysisSummary = caseAnalysisSummary ?: cur.caseAnalysisSummary,
            quizScore = quizScore ?: cur.quizScore,
            quizCompleted = quizCompleted ?: cur.quizCompleted,
            reflectionText = reflectionText ?: cur.reflectionText,
            competencyRecommendation = competencyRecommendation ?: cur.competencyRecommendation
        )
    }

    fun completeStageAndAdvance(stage: LearningStage) {
        applyOptimisticStage(completedStage = stage.number)
        val next = LearningStage.fromNumber((stage.number + 1).coerceAtMost(10))
        _currentStage.value = next

        viewModelScope.launch {
            val snapshot = currentProgressSnapshot()
            val result = progressRepo.updateStageProgress(
                userId = currentUserId,
                currentProgress = snapshot,
                completedStage = stage.number
            )
            result.onFailure {
                _statusBanner.value = "Progres tersimpan di sesi ini; sinkronisasi cloud tertunda (${it.localizedMessage})."
            }

            val notifTitle = "Tahap ${stage.number} Selesai"
            val notifMsg = "${stage.title} tuntas. Tahap ${next.number} (${next.shortTitle}) terbuka."
            _milestoneEvent.value = notifTitle to notifMsg
            progressRepo.pushNotification(currentUserId, notifTitle, notifMsg, stage.number)
        }
    }

    fun onObjectSelectedFrom3D(objectId: String) {
        _selectedObjectId.value = objectId
        val snapshotBefore = currentProgressSnapshot()
        val updatedObjects = (snapshotBefore.exploredObjects + objectId).distinct()
        val completedNow = if (updatedObjects.size >= 3) 4 else 3
        applyOptimisticStage(completedStage = completedNow, exploredObjects = updatedObjects)

        viewModelScope.launch {
            progressRepo.updateStageProgress(
                userId = currentUserId,
                currentProgress = snapshotBefore,
                completedStage = completedNow,
                exploredObjects = updatedObjects
            )
            val objName = LearningContentCatalog.classroomObjects
                .firstOrNull { it.id == objectId }?.name ?: objectId
            progressRepo.pushNotification(
                userId = currentUserId,
                title = "Objek 3D Dieksplorasi",
                message = "Anda telah meninjau $objName (${updatedObjects.size}/5 objek).",
                stageNumber = 4
            )
        }
    }

    fun selectStudentOption(scenarioId: String, optionId: String) {
        val updated = _studentSelections.value.toMutableMap().apply { put(scenarioId, optionId) }
        _studentSelections.value = updated
        if (updated.size >= LearningContentCatalog.studentScenarios.size) {
            val correctCount = LearningContentCatalog.studentScenarios.count {
                updated[it.id] == it.bestOptionId
            }
            val summary = "Analisis $correctCount/${LearningContentCatalog.studentScenarios.size} profil gaya belajar peserta didik (Visual, Auditori, Kinestetik, Akademik Beragam)."
            applyOptimisticStage(
                completedStage = 5,
                studentAnalysisDone = true,
                caseAnalysisSummary = summary
            )
            viewModelScope.launch {
                val snapshot = currentProgressSnapshot()
                progressRepo.updateStageProgress(
                    userId = currentUserId,
                    currentProgress = snapshot,
                    completedStage = 5,
                    studentAnalysisDone = true,
                    caseAnalysisSummary = summary
                )
                val msg = "Interaksi 1 selesai ($correctCount/${LearningContentCatalog.studentScenarios.size} tepat)."
                _milestoneEvent.value = "Analisis Siswa Selesai" to msg
                progressRepo.pushNotification(currentUserId, "Interaksi 1 Selesai", msg, 5)
            }
        }
    }

    fun selectLocalWisdomOption(scenarioId: String, optionId: String) {
        val updated = _localWisdomSelections.value.toMutableMap().apply { put(scenarioId, optionId) }
        _localWisdomSelections.value = updated
        if (updated.size >= LearningContentCatalog.localWisdomScenarios.size) {
            applyOptimisticStage(completedStage = 6, localWisdomDone = true)
            viewModelScope.launch {
                val snapshot = currentProgressSnapshot()
                progressRepo.updateStageProgress(
                    userId = currentUserId,
                    currentProgress = snapshot,
                    completedStage = 6,
                    localWisdomDone = true
                )
                val msg = "Integrasi Pesantren, Gotong Royong, Batik & Tradisi Lokal tersimpan."
                _milestoneEvent.value = "Kearifan Lokal Selesai" to msg
                progressRepo.pushNotification(currentUserId, "Interaksi 2 Selesai", msg, 6)
            }
        }
    }

    fun selectStrategyOption(caseId: String, strategyId: String) {
        val updated = _strategySelections.value.toMutableMap().apply { put(caseId, strategyId) }
        _strategySelections.value = updated

        val selectedNames = LearningContentCatalog.strategyCases.mapNotNull { c ->
            val pickedId = updated[c.id] ?: return@mapNotNull null
            c.options.firstOrNull { it.id == pickedId }?.strategyName
        }

        if (updated.size >= LearningContentCatalog.strategyCases.size) {
            val correctCases = LearningContentCatalog.strategyCases.count {
                updated[it.id] == it.bestStrategyId
            }
            val summary = "Analisis Kasus Strategi: $correctCases/${LearningContentCatalog.strategyCases.size} kasus terpecahkan optimal (${selectedNames.joinToString()})."
            applyOptimisticStage(
                completedStage = 7,
                strategyChoiceDone = true,
                selectedStrategies = selectedNames,
                caseAnalysisSummary = summary
            )
            viewModelScope.launch {
                val snapshot = currentProgressSnapshot()
                progressRepo.updateStageProgress(
                    userId = currentUserId,
                    currentProgress = snapshot,
                    completedStage = 7,
                    strategyChoiceDone = true,
                    selectedStrategies = selectedNames,
                    caseAnalysisSummary = summary
                )
                val msg = "Interaksi 3 selesai: ${selectedNames.size} strategi diuji."
                _milestoneEvent.value = "Pemilihan Strategi Selesai" to msg
                progressRepo.pushNotification(currentUserId, "Interaksi 3 Selesai", msg, 7)
            }
        }
    }

    fun setDifficulty(level: DifficultyLevel) {
        _difficultyLevel.value = level
    }

    fun buildStageStrategySummary(): String {
        val strategyText = LearningContentCatalog.strategyCases.mapNotNull { c ->
            val pickedId = _strategySelections.value[c.id] ?: return@mapNotNull null
            val opt = c.options.firstOrNull { it.id == pickedId } ?: return@mapNotNull null
            "${c.caseTitle}: memilih ${opt.strategyName}"
        }.joinToString("; ")

        val wisdomText = LearningContentCatalog.localWisdomScenarios.mapNotNull { w ->
            val pickedId = _localWisdomSelections.value[w.id] ?: return@mapNotNull null
            val opt = w.options.firstOrNull { it.id == pickedId } ?: return@mapNotNull null
            "${w.themeTitle}: ${opt.title}"
        }.joinToString("; ")

        val studentText = LearningContentCatalog.studentScenarios.mapNotNull { s ->
            val pickedId = _studentSelections.value[s.id] ?: return@mapNotNull null
            val opt = s.options.firstOrNull { it.id == pickedId } ?: return@mapNotNull null
            "${s.studentName} (${s.learningStyle}): ${opt.title}"
        }.joinToString("; ")

        return listOfNotNull(
            strategyText.takeIf { it.isNotBlank() }?.let { "Strategi terpilih -> $it" },
            wisdomText.takeIf { it.isNotBlank() }?.let { "Kearifan lokal -> $it" },
            studentText.takeIf { it.isNotBlank() }?.let { "Penanganan siswa -> $it" }
        ).joinToString(" | ")
    }

    fun generateCaseStudy(topicInput: String) {
        caseStudyGenerationCount += 1
        val seedIndex = (caseStudyGenerationCount - 1) % caseDiversitySeeds.size
        val rotatedContext = caseDiversitySeeds[seedIndex]
        val userTopic = topicInput.trim()
        val effectiveTopic = if (userTopic.isNotBlank()) userTopic else rotatedContext

        val previousSummaryBlock = if (generatedCaseSummaries.isEmpty()) {
            "Belum ada kasus sebelumnya."
        } else {
            generatedCaseSummaries.takeLast(5).mapIndexed { idx, summary ->
                "${idx + 1}. $summary"
            }.joinToString("\n")
        }

        val prompt = """
            Buatkan 1 skenario studi kasus pembelajaran PAI perguruan tinggi yang UNIK, BARU, dan BERBEDA (generasi ke-$caseStudyGenerationCount, variasi sudut pandang: $rotatedContext).
            Topik/fokus permintaan mahasiswa: "$effectiveTopic".
            Tingkat kompleksitas: ${_difficultyLevel.value.label}.

            ATURAN KEUNIKAN WAJIB:
            - Buat judul kasus, nama tokoh guru/siswa, latar sekolah/madrasah, dan inti permasalahan kelas yang BENAR-BENAR BERBEDA dari kasus-kasus sebelumnya berikut:
            $previousSummaryBlock

            Struktur wajib dalam respons:
            1) Judul Kasus & Profil Satuan Pendidikan
            2) Konteks Kelas & Karakteristik Siswa (Visual, Auditori, Kinestetik, Kemampuan Akademik Beragam)
            3) Permasalahan Pedagogis PAI yang Spesifik
            4) Pertanyaan Pemantik untuk Mahasiswa (agar mahasiswa merumuskan strategi pembelajaran PAI yang tepat pada fitur Analisis Strategi)
            5) Rambu-rambu Nilai Islam & Kearifan Lokal (tanpa mengarang ayat Al-Qur'an atau hadis).
        """.trimIndent()

        runStreamingAiTask(
            toolType = AiToolType.CASE_STUDY,
            topicInput = effectiveTopic,
            studentAnswerInput = "",
            prompt = prompt,
            highVariation = true
        )
    }

    fun analyzeStrategy(topicOrCaseInput: String, studentAnswerInput: String) {
        val cleanAnswer = studentAnswerInput.trim()
        val cleanTopic = topicOrCaseInput.trim()
        val stageContext = buildStageStrategySummary()

        val effectiveStudentAnswer = when {
            cleanAnswer.isNotBlank() -> cleanAnswer
            cleanTopic.isNotBlank() -> cleanTopic
            stageContext.isNotBlank() -> stageContext
            else -> ""
        }

        if (effectiveStudentAnswer.isBlank()) {
            val msg = "Tuliskan jawaban atau pilihan strategi pembelajaran Anda terlebih dahulu pada kolom Jawaban Mahasiswa agar Gemini AI dapat menganalisisnya."
            _statusBanner.value = msg
            _aiUiState.value = AiLabUiState.Error(
                toolType = AiToolType.STRATEGY_ANALYSIS,
                message = msg,
                topicInput = cleanTopic,
                studentAnswerInput = cleanAnswer
            )
            return
        }

        val caseContext = when {
            cleanTopic.isNotBlank() && cleanAnswer.isNotBlank() -> cleanTopic
            !lastGeneratedCaseStudy.isNullOrBlank() -> lastGeneratedCaseStudy!!.take(600)
            else -> "Permasalahan pembelajaran PAI di kelas madrasah/sekolah heterogen."
        }

        val stageBlock = if (stageContext.isNotBlank()) {
            "\nRiwayat pilihan interaksi mahasiswa di Tahap 5-7: $stageContext"
        } else {
            ""
        }

        val prompt = """
            Lakukan ANALISIS PEDAGOGIS secara objektif, konstruktif, dan mendalam BERDASARKAN JAWABAN MAHASISWA berikut ini.

            Konteks Kasus / Topik Pembelajaran PAI:
            "$caseContext"

            JAWABAN / RANCANGAN STRATEGI MAHASISWA:
            "$effectiveStudentAnswer"
            $stageBlock

            Tingkat kedalaman analisis: ${_difficultyLevel.value.label}.

            Berikan umpan balik terstruktur yang secara eksplisit menilai jawaban mahasiswa tersebut:
            1) Evaluasi Ketepatan Jawaban Mahasiswa (apresiasi kekuatan argumen mahasiswa dan tunjukkan bagian yang masih kurang tepat atau terlewat).
            2) Analisis Kesesuaian Pedagogis (kesesuaian strategi pilihan mahasiswa dengan karakteristik siswa visual, auditori, kinestetik, kemampuan akademik beragam, serta kearifan lokal Indonesia).
            3) Perbandingan Strategi Alternatif (bandingkan jawaban mahasiswa dengan minimal 2 strategi PAI lain dari: PBL, PjBL, Cooperative Learning, CTL, Demonstrasi, Diskusi Interaktif).
            4) Rekomendasi Perbaikan & Contoh Langkah Kegiatan Pembelajaran PAI (Pendahuluan, Inti, Penutup) yang menyempurnakan jawaban mahasiswa.
            5) Catatan Integritas Sumber: jangan pernah mengarang ayat Al-Qur'an, nomor ayat, atau hadis.
        """.trimIndent()

        runStreamingAiTask(
            toolType = AiToolType.STRATEGY_ANALYSIS,
            topicInput = cleanTopic.ifBlank { "Analisis Jawaban Mahasiswa" },
            studentAnswerInput = effectiveStudentAnswer,
            prompt = prompt,
            highVariation = false
        )
    }

    fun askAiTutor(questionInput: String) {
        val question = questionInput.trim().ifBlank { "Bagaimana memadukan kearifan lokal dengan Cooperative Learning di kelas PAI?" }
        val prompt = """
            Mahasiswa Strategi Pembelajaran PAI bertanya: "$question".
            Berikan jawaban tutor akademis dari Gemini AI yang jelas, terstruktur, praktis untuk calon guru PAI, serta tetap menjaga validitas sumber tanpa mengarang ayat atau hadis.
        """.trimIndent()
        runStreamingAiTask(
            toolType = AiToolType.AI_TUTOR,
            topicInput = question,
            studentAnswerInput = "",
            prompt = prompt,
            highVariation = false
        )
    }

    fun retryLastAiTask(toolType: AiToolType, topicInput: String, studentAnswerInput: String) {
        when (toolType) {
            AiToolType.CASE_STUDY -> generateCaseStudy(topicInput)
            AiToolType.STRATEGY_ANALYSIS -> analyzeStrategy(topicInput, studentAnswerInput)
            AiToolType.AI_TUTOR -> askAiTutor(topicInput)
            AiToolType.QUIZ -> generateAdaptiveQuiz(topicInput)
        }
    }

    private fun runStreamingAiTask(
        toolType: AiToolType,
        topicInput: String,
        studentAnswerInput: String,
        prompt: String,
        highVariation: Boolean
    ) {
        viewModelScope.launch {
            val diff = _difficultyLevel.value
            _aiUiState.value = AiLabUiState.Loading(toolType)
            var finalText = ""
            try {
                aiLabRepo.streamPedagogicalContent(prompt, highVariation = highVariation).collect { accumulated ->
                    finalText = accumulated
                    _aiUiState.value = AiLabUiState.Streaming(toolType, accumulated, AiSourceMode.GEMINI_AI)
                }
                if (toolType == AiToolType.CASE_STUDY && finalText.isNotBlank()) {
                    lastGeneratedCaseStudy = finalText
                    val firstLine = finalText.lineSequence().firstOrNull { it.isNotBlank() }?.take(140)
                        ?: finalText.take(140)
                    generatedCaseSummaries.add(firstLine)
                }
                _aiUiState.value = AiLabUiState.Success(toolType, finalText, AiSourceMode.GEMINI_AI, diff)
                val recordPrompt = if (studentAnswerInput.isNotBlank()) {
                    "Kasus: $topicInput | Jawaban: ${studentAnswerInput.take(160)}"
                } else {
                    topicInput
                }
                persistAiUsage(toolType, recordPrompt, finalText, AiSourceMode.GEMINI_AI, diff)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI streaming timed out", e)
                _aiUiState.value = AiLabUiState.Error(
                    toolType = toolType,
                    message = "Waktu koneksi Gemini AI habis (60 detik). Periksa jaringan Anda lalu tekan Coba Lagi.",
                    topicInput = topicInput,
                    studentAnswerInput = studentAnswerInput
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI streaming failed", e)
                _aiUiState.value = AiLabUiState.Error(
                    toolType = toolType,
                    message = "Gagal terhubung ke Gemini AI: ${e.localizedMessage ?: "layanan tidak terjangkau"}. Pastikan koneksi internet aktif lalu tekan Coba Lagi.",
                    topicInput = topicInput,
                    studentAnswerInput = studentAnswerInput
                )
            }
        }
    }

    fun generateAdaptiveQuiz(topicInput: String) {
        val topic = topicInput.trim().ifBlank { "Strategi Pembelajaran PAI" }
        val diff = _difficultyLevel.value
        quizGenerationCount += 1

        viewModelScope.launch {
            _adaptiveQuizState.value = AdaptiveQuizUiState.Loading
            try {
                val questions = aiLabRepo.generateAdaptiveQuizSet(
                    topicFocus = topic,
                    difficulty = diff,
                    previousSummary = adaptiveHistorySummary,
                    previousQuestions = generatedQuizQuestionsHistory,
                    generationIndex = quizGenerationCount
                )
                questions.forEach { generatedQuizQuestionsHistory.add(it.question.take(140)) }
                _adaptiveQuizState.value = AdaptiveQuizUiState.Ready(
                    topicFocus = topic,
                    difficulty = diff,
                    questions = questions,
                    selectedAnswers = emptyMap()
                )
                val summaryLog = questions.mapIndexed { idx, q ->
                    "${idx + 1}. ${q.question}"
                }.joinToString("\n")
                persistAiUsage(
                    AiToolType.QUIZ,
                    "5 Soal Kuis Baru: $topic",
                    summaryLog,
                    AiSourceMode.GEMINI_AI,
                    diff
                )
            } catch (e: TimeoutCancellationException) {
                _adaptiveQuizState.value = AdaptiveQuizUiState.Error(
                    "Pembuatan 5 soal kuis Gemini AI melebihi batas waktu. Silakan tekan Coba Lagi.",
                    topic
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _adaptiveQuizState.value = AdaptiveQuizUiState.Error(
                    "Gagal membuat 5 soal kuis dari Gemini AI: ${e.localizedMessage ?: "koneksi gagal"}.",
                    topic
                )
            }
        }
    }

    fun answerAdaptiveQuiz(questionIndex: Int, optionIndex: Int) {
        val state = _adaptiveQuizState.value as? AdaptiveQuizUiState.Ready ?: return
        if (state.selectedAnswers.containsKey(questionIndex)) return
        if (questionIndex !in state.questions.indices) return

        val updatedAnswers = state.selectedAnswers.toMutableMap().apply {
            put(questionIndex, optionIndex)
        }
        _adaptiveQuizState.value = state.copy(selectedAnswers = updatedAnswers)

        if (updatedAnswers.size == state.questions.size) {
            val correctCount = state.questions.indices.count { idx ->
                updatedAnswers[idx] == state.questions[idx].correctIndex
            }
            val weakTopics = state.questions.indices
                .filter { idx -> updatedAnswers[idx] != state.questions[idx].correctIndex }
                .map { idx -> state.questions[idx].topic }
                .distinct()

            adaptiveHistorySummary = "Skor kuis AI sebelumnya: $correctCount/${state.questions.size} pada level ${state.difficulty.label}. " +
                if (weakTopics.isEmpty()) "Seluruh subtopik dikuasai." else "Perlu pendalaman pada: ${weakTopics.joinToString(", ")}."

            _difficultyLevel.value = when {
                correctCount >= 4 && _difficultyLevel.value == DifficultyLevel.DASAR -> DifficultyLevel.MENENGAH
                correctCount >= 4 && _difficultyLevel.value == DifficultyLevel.MENENGAH -> DifficultyLevel.LANJUT
                correctCount <= 2 && _difficultyLevel.value == DifficultyLevel.LANJUT -> DifficultyLevel.MENENGAH
                correctCount <= 2 && _difficultyLevel.value == DifficultyLevel.MENENGAH -> DifficultyLevel.DASAR
                else -> _difficultyLevel.value
            }
        }
    }

    private suspend fun persistAiUsage(
        toolType: AiToolType,
        promptText: String,
        responseText: String,
        sourceMode: AiSourceMode,
        difficulty: DifficultyLevel
    ) {
        progressRepo.saveAiRecord(
            userId = currentUserId,
            toolType = toolType.name,
            promptText = promptText,
            responseText = responseText,
            sourceMode = sourceMode.name,
            difficultyLevel = difficulty.name
        )
        val snapshot = currentProgressSnapshot()
        if (snapshot.highestUnlockedStage < 9L) {
            applyOptimisticStage(completedStage = 8)
            progressRepo.updateStageProgress(
                userId = currentUserId,
                currentProgress = snapshot,
                completedStage = 8
            )
            val msg = "Eksperimen Laboratorium GenAI tercatat. Tahap Evaluasi kini terbuka."
            _milestoneEvent.value = "Lab GenAI Aktif" to msg
            progressRepo.pushNotification(currentUserId, "Tahap 8 Tercatat", msg, 8)
        }
    }

    fun selectEvaluationAnswer(questionIndex: Int, optionIndex: Int) {
        if (_evaluationSubmitted.value) return
        val updated = _evaluationAnswers.value.toMutableMap().apply { put(questionIndex, optionIndex) }
        _evaluationAnswers.value = updated
    }

    fun resetEvaluationForRetry() {
        _evaluationAnswers.value = emptyMap()
        _evaluationSubmitted.value = false
    }

    fun submitEvaluationAndReflection(reflectionInput: String) {
        val questions = LearningContentCatalog.evaluationQuestions
        val answers = _evaluationAnswers.value
        if (answers.size < questions.size) {
            _statusBanner.value = "Harap jawab seluruh ${questions.size} soal evaluasi sebelum mengirim."
            return
        }
        val cleanReflection = reflectionInput.trim()
        if (cleanReflection.length < 10) {
            _statusBanner.value = "Tuliskan refleksi pembelajaran minimal 10 karakter."
            return
        }

        val correctCount = questions.indices.count { idx ->
            answers[idx] == questions[idx].correctIndex
        }
        val score = ((correctCount * 100.0) / questions.size).toInt().coerceIn(0, 100)
        val weakTopics = questions.indices
            .filter { idx -> answers[idx] != questions[idx].correctIndex }
            .map { idx -> questions[idx].topicTag }
            .distinct()

        val recommendation = if (weakTopics.isEmpty()) {
            "Penguasaan Anda sangat baik ($score/100). Kompetensi lanjutan: kembangkan RPP/modul ajar PAI berbasis VR & PBL kolaboratif untuk dipraktikkan dalam microteaching."
        } else {
            "Skor Anda $score/100. Materi yang disarankan untuk dipelajari kembali: ${weakTopics.joinToString(", ")}. Manfaatkan fitur Analisis Strategi di Lab GenAI untuk pendalaman."
        }

        _evaluationSubmitted.value = true
        applyOptimisticStage(
            completedStage = 9,
            quizScore = score.toLong(),
            quizCompleted = true,
            reflectionText = cleanReflection,
            competencyRecommendation = recommendation
        )
        _currentStage.value = LearningStage.HASIL_PEMBELAJARAN

        viewModelScope.launch {
            val snapshot = currentProgressSnapshot()
            val result = progressRepo.updateStageProgress(
                userId = currentUserId,
                currentProgress = snapshot,
                completedStage = 9,
                quizScore = score.toLong(),
                quizCompleted = true,
                reflectionText = cleanReflection,
                competencyRecommendation = recommendation
            )
            val msg = "Evaluasi selesai dengan skor $score/100. Laporan Hasil Pembelajaran siap."
            _milestoneEvent.value = "Evaluasi Selesai ($score/100)" to msg
            result.onSuccess {
                progressRepo.pushNotification(currentUserId, "Evaluasi Selesai", msg, 9)
            }.onFailure {
                _statusBanner.value = "Hasil evaluasi tampil di sesi ini; sinkronisasi cloud tertunda (${it.localizedMessage})."
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            progressRepo.markNotificationRead(currentUserId, notificationId)
        }
    }
}

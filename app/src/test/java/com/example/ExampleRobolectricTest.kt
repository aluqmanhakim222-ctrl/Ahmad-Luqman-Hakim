package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.LearningContentCatalog
import com.example.model.AdaptiveAiQuizQuestion
import com.example.model.AiSourceMode
import com.example.model.DifficultyLevel
import com.example.model.StudentProgress
import com.example.ui.AdaptiveQuizUiState
import com.example.ui.AiLabUiState
import com.example.ui.screens.AnalisisSiswaScreen
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.EvaluasiRefleksiScreen
import com.example.ui.screens.HasilPembelajaranScreen
import com.example.ui.screens.KearifanLokalScreen
import com.example.ui.screens.LaboratoriumGenAiScreen
import com.example.ui.screens.PemilihanStrategiScreen
import com.example.ui.screens.TujuanPembelajaranScreen
import com.example.ui.theme.PaiImmersiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `app name matches PAI Immersive`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("PAI Immersive", context.getString(R.string.app_name))
    }

    @Test
    fun `beranda and tujuan pembelajaran buttons respond`() {
        var started = false
        var guideOpened = false
        var enteredClass = false

        composeRule.setContent {
            PaiImmersiveTheme {
                var showObjectives by remember { mutableStateOf(false) }
                if (!showObjectives) {
                    BerandaScreen(
                        progress = StudentProgress(displayName = "Luqman"),
                        onStartLearning = {
                            started = true
                            showObjectives = true
                        },
                        onOpenGuide = { guideOpened = true },
                        onJumpStage = {}
                    )
                } else {
                    TujuanPembelajaranScreen(
                        completedStages = listOf(1L),
                        onCompleteObjectives = { enteredClass = true }
                    )
                }
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("guide_button").performClick()
        assertTrue(guideOpened)

        composeRule.onNodeWithTag("start_learning_button").performClick()
        composeRule.waitForIdle()
        assertTrue(started)

        composeRule.onNodeWithTag("enter_virtual_class_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue(enteredClass)
    }

    @Test
    fun `interaksi 1 analisis siswa memberi umpan balik setiap pilihan dan indikator selesai`() {
        var continued = false

        composeRule.setContent {
            PaiImmersiveTheme {
                var selections by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
                AnalisisSiswaScreen(
                    selections = selections,
                    onSelectOption = { sid, oid ->
                        selections = selections + (sid to oid)
                    },
                    onContinueToLocalWisdom = { continued = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Belum Dimulai (0/4)").assertExists()
        composeRule.onNodeWithTag("continue_local_wisdom_button").performScrollTo().assertIsNotEnabled()

        // Pilih opsi kurang tepat lalu opsi tepat pada siswa visual, pastikan umpan balik muncul keduanya
        composeRule.onNodeWithTag("student_visual_student_opt_v2").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("feedback_student_visual_student").assertExists()
        composeRule.onNodeWithText("Respons Pilihan: Perlu Penyesuaian").assertExists()
        composeRule.onNodeWithText("Sedang Dikerjakan (1/4)").assertExists()

        composeRule.onNodeWithTag("student_visual_student_opt_v1").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Respons Pilihan: Tepat").assertExists()

        // Selesaikan 3 profil siswa lainnya (Auditori, Kinestetik, Akademik Beragam)
        composeRule.onNodeWithTag("student_auditory_student_opt_a2").performScrollTo().performClick()
        composeRule.onNodeWithTag("student_kinesthetic_student_opt_k2").performScrollTo().performClick()
        composeRule.onNodeWithTag("student_diverse_academic_opt_d1").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("feedback_student_auditory_student").assertExists()
        composeRule.onNodeWithTag("feedback_student_kinesthetic_student").assertExists()
        composeRule.onNodeWithTag("feedback_student_diverse_academic").assertExists()

        composeRule.onNodeWithText("Aktivitas Selesai").assertExists()
        composeRule.onNodeWithTag("continue_local_wisdom_button")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertTrue(continued)
    }

    @Test
    fun `interaksi 2 kearifan lokal memberi umpan balik setiap pilihan dan indikator selesai`() {
        var continued = false

        composeRule.setContent {
            PaiImmersiveTheme {
                var selections by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
                KearifanLokalScreen(
                    selections = selections,
                    onSelectOption = { sid, oid ->
                        selections = selections + (sid to oid)
                    },
                    onContinueToStrategyChoice = { continued = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Belum Dimulai (0/4)").assertExists()
        composeRule.onNodeWithTag("continue_strategy_choice_button").performScrollTo().assertIsNotEnabled()

        // Uji pilihan kurang kontekstual lalu tepat pada tradisi pesantren
        composeRule.onNodeWithTag("local_pesantren_tradition_lw_p2").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("feedback_local_pesantren_tradition").assertExists()
        composeRule.onNodeWithText("Respons Integrasi: Kurang Kontekstual").assertExists()

        composeRule.onNodeWithTag("local_pesantren_tradition_lw_p1").performScrollTo().performClick()
        composeRule.onNodeWithTag("local_gotong_royong_lw_g1").performScrollTo().performClick()
        composeRule.onNodeWithTag("local_batik_context_lw_b1").performScrollTo().performClick()
        composeRule.onNodeWithTag("local_local_tradition_lw_l1").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("feedback_local_gotong_royong").assertExists()
        composeRule.onNodeWithTag("feedback_local_batik_context").assertExists()
        composeRule.onNodeWithTag("feedback_local_local_tradition").assertExists()

        composeRule.onNodeWithText("Aktivitas Selesai").assertExists()
        composeRule.onNodeWithTag("continue_strategy_choice_button")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertTrue(continued)
    }

    @Test
    fun `interaksi 3 pemilihan strategi menguji seluruh pilihan dan indikator selesai`() {
        var continued = false

        composeRule.setContent {
            PaiImmersiveTheme {
                var selections by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
                PemilihanStrategiScreen(
                    selections = selections,
                    onSelectStrategy = { cid, sid ->
                        selections = selections + (cid to sid)
                    },
                    onContinueToGenAiLab = { continued = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Belum Dimulai (0/3)").assertExists()
        composeRule.onNodeWithTag("continue_genai_lab_button").performScrollTo().assertIsNotEnabled()

        // Pastikan seluruh 6 alternatif strategi pada kasus 1 memberikan respons umpan balik
        val case1Options = listOf("demonstrasi", "pjbl", "cooperative", "ctl", "diskusi", "pbl")
        case1Options.forEach { optId ->
            composeRule.onNodeWithTag("strategy_case_pbl_$optId").performScrollTo().performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("feedback_strategy_case_pbl").assertExists()
        }

        // Selesaikan kasus 2 & kasus 3
        composeRule.onNodeWithTag("strategy_case_demo_demonstrasi").performScrollTo().performClick()
        composeRule.onNodeWithTag("strategy_case_pjbl_coop_pjbl").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("feedback_strategy_case_demo").assertExists()
        composeRule.onNodeWithTag("feedback_strategy_case_pjbl_coop").assertExists()

        composeRule.onNodeWithText("Aktivitas Selesai").assertExists()
        composeRule.onNodeWithTag("continue_genai_lab_button")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertTrue(continued)
    }

    @Test
    fun `laboratorium genai memicu seluruh tombol dan menampilkan 5 soal kuis`() {
        var caseTopicCaptured = ""
        var quizTopicCaptured = ""
        var analyzedAnswerCaptured = ""
        var tutorQuestionCaptured = ""

        val sampleFiveQuestions = (1..5).map { idx ->
            AdaptiveAiQuizQuestion(
                difficulty = DifficultyLevel.MENENGAH,
                topic = "Strategi PAI #$idx",
                question = "Pertanyaan uji strategi PAI nomor $idx?",
                options = listOf("Opsi A$idx", "Opsi B$idx", "Opsi C$idx", "Opsi D$idx"),
                correctIndex = 0,
                explanation = "Penjelasan pedagogis soal $idx.",
                sourceMode = AiSourceMode.GEMINI_AI
            )
        }

        composeRule.setContent {
            PaiImmersiveTheme {
                var selectedQuizAnswers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
                LaboratoriumGenAiScreen(
                    aiUiState = AiLabUiState.Idle,
                    adaptiveQuizState = AdaptiveQuizUiState.Ready(
                        topicFocus = "Strategi Pembelajaran PAI",
                        difficulty = DifficultyLevel.MENENGAH,
                        questions = sampleFiveQuestions,
                        selectedAnswers = selectedQuizAnswers
                    ),
                    difficulty = DifficultyLevel.MENENGAH,
                    stageStrategySummary = "Strategi terpilih -> Problem Based Learning (PBL)",
                    aiRecords = emptyList(),
                    onSelectDifficulty = {},
                    onGenerateCaseStudy = { caseTopicCaptured = it },
                    onGenerateQuiz = { quizTopicCaptured = it },
                    onAnalyzeStrategy = { _, ans -> analyzedAnswerCaptured = ans },
                    onAskAiTutor = { tutorQuestionCaptured = it },
                    onRetryAiTask = { _, _, _ -> },
                    onAnswerAdaptiveQuiz = { qIdx, optIdx ->
                        selectedQuizAnswers = selectedQuizAnswers + (qIdx to optIdx)
                    },
                    onContinueToEvaluation = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("genai_prompt_input").performScrollTo().performTextInput("Fikih Muamalah")
        composeRule.onNodeWithTag("genai_student_answer_input").performScrollTo().performTextInput("Saya memakai PBL dan gotong royong")

        composeRule.onNodeWithTag("btn_generate_case_study").performScrollTo().performClick()
        composeRule.onNodeWithTag("btn_generate_quiz").performScrollTo().performClick()
        composeRule.onNodeWithTag("btn_analyze_strategy").performScrollTo().performClick()
        composeRule.onNodeWithTag("btn_ask_ai_tutor").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals("Fikih Muamalah", caseTopicCaptured)
        assertEquals("Fikih Muamalah", quizTopicCaptured)
        assertEquals("Saya memakai PBL dan gotong royong", analyzedAnswerCaptured)
        assertEquals("Fikih Muamalah", tutorQuestionCaptured)

        // Pastikan seluruh 5 soal kuis tampil dan merespons klik jawaban
        composeRule.onNodeWithText("Kuis Gemini AI (5 Soal Baru • Menengah)").performScrollTo().assertExists()
        for (qIdx in 0 until 5) {
            composeRule.onNodeWithTag("ai_quiz_${qIdx}_opt_0").performScrollTo().performClick()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Terjawab 5/5").performScrollTo().assertExists()
    }

    @Test
    fun `evaluasi refleksi dan hasil pembelajaran berfungsi penuh`() {
        var submittedReflection = ""

        composeRule.setContent {
            PaiImmersiveTheme {
                var answers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
                var submitted by remember { mutableStateOf(false) }

                if (!submitted) {
                    EvaluasiRefleksiScreen(
                        answers = answers,
                        submitted = submitted,
                        initialReflection = "",
                        onSelectAnswer = { qIdx, optIdx -> answers = answers + (qIdx to optIdx) },
                        onRetryQuiz = { answers = emptyMap(); submitted = false },
                        onSubmitEvaluation = {
                            submittedReflection = it
                            submitted = true
                        }
                    )
                } else {
                    HasilPembelajaranScreen(
                        progress = StudentProgress(
                            displayName = "Luqman",
                            quizScore = 100L,
                            quizCompleted = true,
                            selectedStrategies = listOf("Problem Based Learning (PBL)"),
                            studentAnalysisDone = true,
                            localWisdomDone = true,
                            reflectionText = submittedReflection,
                            competencyRecommendation = "Penguasaan Anda sangat baik (100/100)."
                        ),
                        onReviewStage = {},
                        onBackToHome = {}
                    )
                }
            }
        }

        composeRule.waitForIdle()
        val questions = LearningContentCatalog.evaluationQuestions
        questions.forEachIndexed { idx, q ->
            composeRule.onNodeWithTag("eval_q${idx}_opt${q.correctIndex}").performScrollTo().performClick()
        }
        composeRule.onNodeWithTag("reflection_input").performScrollTo()
            .performTextInput("Strategi PAI harus selaras dengan gaya belajar dan budaya lokal.")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Sedang Dikerjakan (7/7)").assertExists()
        composeRule.onNodeWithTag("submit_evaluation_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertTrue(submittedReflection.contains("Strategi PAI"))
        composeRule.onNodeWithText("Laporan Hasil Pembelajaran").assertExists()
        composeRule.onNodeWithText("100 / 100").assertExists()
    }
}

package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class LearningStage(
    val number: Int,
    val title: String,
    val shortTitle: String,
    val subtitle: String
) {
    BERANDA(1, "Beranda", "Beranda", "Pengantar Laboratorium Virtual PAI"),
    TUJUAN_PEMBELAJARAN(2, "Tujuan Pembelajaran", "Tujuan", "CPMK & Peta Alur Strategi PAI"),
    RUANG_KELAS_3D(3, "Ruang Kelas Virtual 3D", "Kelas 3D", "Eksplorasi Ruang Madrasah A-Frame / WebXR"),
    EKSPLORASI_OBJEK(4, "Eksplorasi Objek Pembelajaran", "Objek 3D", "Analisis Artefak Pedagogis Kelas"),
    ANALISIS_SISWA(5, "Analisis Karakteristik Siswa", "Siswa", "Interaksi 1: Diferensiasi Gaya Belajar"),
    KEARIFAN_LOKAL(6, "Integrasi Kearifan Lokal", "Lokal", "Interaksi 2: Pesantren, Gotong Royong & Batik"),
    PEMILIHAN_STRATEGI(7, "Pemilihan Strategi Pembelajaran", "Strategi", "Interaksi 3: Penentuan Model PAI Tepat"),
    LAB_GEN_AI(8, "Laboratorium Generative AI", "Lab GenAI", "Simulasi Kasus, Kuis & AI Tutor"),
    EVALUASI_REFLEKSI(9, "Evaluasi & Refleksi", "Evaluasi", "Kuis Akhir & Refleksi Pembelajaran"),
    HASIL_PEMBELAJARAN(10, "Hasil Pembelajaran", "Hasil", "Laporan Kompetensi & Rekomendasi");

    companion object {
        fun fromNumber(num: Int): LearningStage =
            entries.firstOrNull { it.number == num } ?: BERANDA
    }
}

enum class AiToolType {
    CASE_STUDY,
    QUIZ,
    STRATEGY_ANALYSIS,
    AI_TUTOR;

    companion object {
        fun safeValueOf(raw: String): AiToolType =
            entries.firstOrNull { it.name == raw } ?: AI_TUTOR
    }
}

enum class AiSourceMode(val label: String) {
    GEMINI_AI("Gemini AI Logic"),
    LOCAL_SIMULATION("Simulasi Lokal");

    companion object {
        fun safeValueOf(raw: String): AiSourceMode =
            entries.firstOrNull { it.name == raw } ?: LOCAL_SIMULATION
    }
}

enum class DifficultyLevel(val label: String) {
    DASAR("Dasar"),
    MENENGAH("Menengah"),
    LANJUT("Lanjut");

    companion object {
        fun safeValueOf(raw: String): DifficultyLevel =
            entries.firstOrNull { it.name == raw } ?: MENENGAH
    }
}

@Keep
@IgnoreExtraProperties
data class StudentProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Mahasiswa PAI",
    val highestUnlockedStage: Long = 1L,
    val completedStages: List<Long> = emptyList(),
    val exploredObjects: List<String> = emptyList(),
    val selectedStrategies: List<String> = emptyList(),
    val studentAnalysisDone: Boolean = false,
    val localWisdomDone: Boolean = false,
    val strategyChoiceDone: Boolean = false,
    val caseAnalysisSummary: String = "",
    val quizScore: Long = 0L,
    val quizCompleted: Boolean = false,
    val reflectionText: String = "",
    val competencyRecommendation: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AiLabRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val toolType: String = AiToolType.AI_TUTOR.name,
    val promptText: String = "",
    val responseText: String = "",
    val sourceMode: String = AiSourceMode.GEMINI_AI.name,
    val difficultyLevel: String = DifficultyLevel.MENENGAH.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ProgressNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val stageNumber: Long = 1L,
    val readFlag: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ClassroomObjectItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val pedagogicalValue: String,
    val suggestedStrategy: String
)

data class StudentProfileScenario(
    val id: String,
    val studentName: String,
    val learningStyle: String,
    val academicProfile: String,
    val classroomBehavior: String,
    val options: List<PedagogicalOption>,
    val bestOptionId: String
)

data class LocalWisdomScenario(
    val id: String,
    val themeTitle: String,
    val culturalContext: String,
    val paiTopic: String,
    val options: List<PedagogicalOption>,
    val bestOptionId: String
)

data class StrategyCaseScenario(
    val id: String,
    val caseTitle: String,
    val classroomProblem: String,
    val options: List<StrategyOption>,
    val bestStrategyId: String
)

data class PedagogicalOption(
    val id: String,
    val title: String,
    val rationale: String
)

data class StrategyOption(
    val id: String,
    val strategyName: String,
    val summary: String,
    val feedbackIfChosen: String
)

data class QuizQuestionItem(
    val id: String,
    val topicTag: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class AdaptiveAiQuizQuestion(
    val difficulty: DifficultyLevel,
    val topic: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val sourceMode: AiSourceMode
)

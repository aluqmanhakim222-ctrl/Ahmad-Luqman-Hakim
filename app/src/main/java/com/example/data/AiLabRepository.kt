package com.example.data

import android.util.Log
import com.example.model.AdaptiveAiQuizQuestion
import com.example.model.AiSourceMode
import com.example.model.DifficultyLevel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiLabRepository"

class AiLabRepository {

    private val systemAcademicInstruction = content("system") {
        text(
            """
            Anda adalah Dosen & Asisten Akademik ahli mata kuliah Strategi Pembelajaran Pendidikan Agama Islam (PAI) di perguruan tinggi Indonesia.
            Gunakan bahasa Indonesia akademis yang jelas, sistematis, santun, dan aplikatif untuk mahasiswa calon guru PAI.
            ATURAN WAJIB INTEGRITAS DALIL:
            - Jangan pernah mengarang ayat Al-Qur'an, nomor ayat, teks Arab, hadis, atau referensi kitab palsu.
            - Jika tidak menyebutkan teks primer secara pasti, gunakan prinsip nilai ajaran Islam secara umum (misalnya: prinsip tabayyun, ta'awun, musyawarah, akhlak mulia, hikmah) tanpa mengklaim kutipan harfiah yang dikarang.
            - Fokus pada analisis pedagogis: karakteristik peserta didik, kearifan lokal Indonesia, pemilihan strategi pembelajaran (PBL, PjBL, Cooperative Learning, CTL, Demonstrasi, Diskusi Interaktif), dan contoh rancangan kegiatan pembelajaran PAI.
            """.trimIndent()
        )
    }

    // Model streaming standar untuk Analisis Strategi & AI Tutor
    private val streamingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = systemAcademicInstruction,
            generationConfig = generationConfig {
                temperature = 0.7f
            }
        )
    }

    // Model dengan temperature tinggi untuk variasi kasus pembelajaran PAI yang berbeda setiap kali ditekan
    private val caseStudyModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = systemAcademicInstruction,
            generationConfig = generationConfig {
                temperature = 0.95f
                topP = 0.95f
            }
        )
    }

    private val quizItemSchema = Schema.obj(
        properties = mapOf(
            "topic" to Schema.string(description = "Subtopik spesifik Strategi Pembelajaran PAI dalam bahasa Indonesia"),
            "question" to Schema.string(description = "Pertanyaan studi kasus pilihan ganda level perguruan tinggi"),
            "options" to Schema.array(
                items = Schema.string(),
                description = "Tepat 4 opsi jawaban pilihan ganda yang jelas dan berbeda"
            ),
            "correctIndex" to Schema.integer(description = "Indeks jawaban benar dari 0 sampai 3"),
            "explanation" to Schema.string(description = "Penjelasan pedagogis mengapa jawaban tersebut tepat dan mengapa opsi lain kurang tepat")
        )
    )

    private val quizSetSchema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = quizItemSchema,
                description = "Daftar berisi tepat 5 soal pilihan ganda baru yang saling berbeda tentang Strategi Pembelajaran PAI"
            )
        )
    )

    private val quizModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = systemAcademicInstruction,
            generationConfig = generationConfig {
                temperature = 0.85f
                responseMimeType = "application/json"
                responseSchema = quizSetSchema
            }
        )
    }

    fun streamPedagogicalContent(prompt: String, highVariation: Boolean = false): Flow<String> = flow {
        val model = if (highVariation) caseStudyModel else streamingModel
        val sb = StringBuilder()
        withTimeout(60_000L) {
            model.generateContentStream(prompt).collect { chunk ->
                val part = chunk.text.orEmpty()
                if (part.isNotEmpty()) {
                    sb.append(part)
                    emit(sb.toString())
                }
            }
        }
        if (sb.isBlank()) {
            throw IllegalStateException("Gemini AI mengembalikan respons kosong.")
        }
    }

    suspend fun generateAdaptiveQuizSet(
        topicFocus: String,
        difficulty: DifficultyLevel,
        previousSummary: String,
        previousQuestions: List<String>,
        generationIndex: Int
    ): List<AdaptiveAiQuizQuestion> {
        val avoidList = if (previousQuestions.isEmpty()) {
            "Belum ada soal sebelumnya."
        } else {
            previousQuestions.takeLast(10).mapIndexed { i, q -> "${i + 1}. $q" }.joinToString("\n")
        }

        val prompt = """
            Buatkan TEPAT 5 soal kuis pilihan ganda BARU (bernomor 1 sampai 5 di array questions) untuk mata kuliah Strategi Pembelajaran PAI tingkat mahasiswa perguruan tinggi.
            Sesi pembuatan ke-$generationIndex.
            Topik fokus: $topicFocus
            Tingkat kesulitan adaptif saat ini: ${difficulty.label}
            Riwayat performa mahasiswa: $previousSummary

            Ketentuan wajib:
            1. Array "questions" HARUS berisi tepat 5 soal yang saling berbeda sudut pandang (misalnya: analisis gaya belajar visual/auditori/kinestetik, integrasi kearifan lokal Indonesia, pemilihan strategi PBL/PjBL/CTL/Cooperative/Demonstrasi/Diskusi Interaktif, evaluasi pembelajaran PAI, dan skenario kelas madrasah/pesantren).
            2. Setiap soal memiliki tepat 4 opsi jawaban yang masuk akal dan 1 correctIndex (0..3).
            3. DILARANG mengulang atau memparafrasekan soal-soal berikut yang sudah pernah muncul:
            $avoidList
            4. JANGAN mengarang ayat Al-Qur'an, nomor ayat, atau hadis palsu.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                quizModel.generateContent(prompt)
            }
            val raw = response.text ?: throw IllegalStateException("Respons JSON kuis dari Gemini AI kosong.")
            val json = JSONObject(raw)
            val questionsArray = json.getJSONArray("questions")
            val parsedQuestions = mutableListOf<AdaptiveAiQuizQuestion>()

            for (i in 0 until questionsArray.length()) {
                val item = questionsArray.getJSONObject(i)
                val topic = item.optString("topic", topicFocus).ifBlank { topicFocus }
                val question = item.getString("question").trim()
                val optArray = item.getJSONArray("options")
                val options = (0 until optArray.length()).map { optArray.getString(it).trim() }.take(4)
                require(options.size == 4) { "Soal ke-${i + 1} tidak memiliki 4 opsi jawaban." }
                val correctIdx = item.getInt("correctIndex").coerceIn(0, 3)
                val explanation = item.getString("explanation").trim()
                parsedQuestions.add(
                    AdaptiveAiQuizQuestion(
                        difficulty = difficulty,
                        topic = topic,
                        question = question,
                        options = options,
                        correctIndex = correctIdx,
                        explanation = explanation,
                        sourceMode = AiSourceMode.GEMINI_AI
                    )
                )
            }

            if (parsedQuestions.size < 5) {
                throw IllegalStateException(
                    "Gemini AI hanya menghasilkan ${parsedQuestions.size} soal (dibutuhkan 5 soal). Silakan tekan Coba Lagi."
                )
            }
            parsedQuestions.take(5)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Quiz set generation timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Quiz set generation failed", e)
            throw e
        }
    }
}

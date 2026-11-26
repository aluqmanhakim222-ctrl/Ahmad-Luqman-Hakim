package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.AiLabRecord
import com.example.model.ProgressNotification
import com.example.model.StudentProgress
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LearningProgressRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireVerifiedUserId(expectedUid: String): String {
        val current = auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google terlebih dahulu.")
        check(current == expectedUid) { "Sesi akun berubah; penulisan dibatalkan." }
        return current
    }

    fun observeProgress(userId: String): Flow<StudentProgress?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(
                        StudentProgress::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeNotifications(userId: String): Flow<List<ProgressNotification>> = flow {
        val path = "users/$userId/notifications"
        emitAll(
            db.collection("users").document(userId).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ProgressNotification::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeAiRecords(userId: String): Flow<List<AiLabRecord>> = flow {
        val path = "users/$userId/ai_records"
        emitAll(
            db.collection("users").document(userId).collection("ai_records")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(15)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        AiLabRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureProgressInitialized(userId: String, displayName: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireVerifiedUserId(userId)
            val safeName = displayName.trim().ifBlank { "Mahasiswa PAI" }.take(80)
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "highestUnlockedStage" to 1L,
                    "completedStages" to emptyList<Long>(),
                    "exploredObjects" to emptyList<String>(),
                    "selectedStrategies" to emptyList<String>(),
                    "studentAnalysisDone" to false,
                    "localWisdomDone" to false,
                    "strategyChoiceDone" to false,
                    "caseAnalysisSummary" to "",
                    "quizScore" to 0L,
                    "quizCompleted" to false,
                    "reflectionText" to "",
                    "competencyRecommendation" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                pushNotification(
                    userId = uid,
                    title = "Selamat Datang di PAI Immersive",
                    message = "Sesi laboratorium pembelajaran Anda aktif. Mulailah dari Tahap 1 menuju Tahap 10.",
                    stageNumber = 1
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateStageProgress(
        userId: String,
        currentProgress: StudentProgress,
        completedStage: Int,
        exploredObjects: List<String> = currentProgress.exploredObjects,
        selectedStrategies: List<String> = currentProgress.selectedStrategies,
        studentAnalysisDone: Boolean = currentProgress.studentAnalysisDone,
        localWisdomDone: Boolean = currentProgress.localWisdomDone,
        strategyChoiceDone: Boolean = currentProgress.strategyChoiceDone,
        caseAnalysisSummary: String = currentProgress.caseAnalysisSummary,
        quizScore: Long = currentProgress.quizScore,
        quizCompleted: Boolean = currentProgress.quizCompleted,
        reflectionText: String = currentProgress.reflectionText,
        competencyRecommendation: String = currentProgress.competencyRecommendation
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireVerifiedUserId(userId)
            val updatedCompleted = (currentProgress.completedStages + completedStage.toLong())
                .distinct()
                .filter { it in 1L..10L }
                .sorted()
                .take(10)
            val nextUnlocked = maxOf(
                currentProgress.highestUnlockedStage,
                (completedStage + 1).coerceAtMost(10).toLong()
            ).coerceIn(1L, 10L)

            val docRef = db.collection("users").document(uid)
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val safeName = currentProgress.displayName.trim().ifBlank { "Mahasiswa PAI" }.take(80)
                val fullPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "highestUnlockedStage" to nextUnlocked,
                    "completedStages" to updatedCompleted,
                    "exploredObjects" to exploredObjects.distinct().take(10),
                    "selectedStrategies" to selectedStrategies.distinct().take(15),
                    "studentAnalysisDone" to studentAnalysisDone,
                    "localWisdomDone" to localWisdomDone,
                    "strategyChoiceDone" to strategyChoiceDone,
                    "caseAnalysisSummary" to caseAnalysisSummary.take(1000),
                    "quizScore" to quizScore.coerceIn(0L, 100L),
                    "quizCompleted" to quizCompleted,
                    "reflectionText" to reflectionText.take(2000),
                    "competencyRecommendation" to competencyRecommendation.take(1000),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(fullPayload).await()
            } else {
                val updates = mapOf(
                    "highestUnlockedStage" to nextUnlocked,
                    "completedStages" to updatedCompleted,
                    "exploredObjects" to exploredObjects.distinct().take(10),
                    "selectedStrategies" to selectedStrategies.distinct().take(15),
                    "studentAnalysisDone" to studentAnalysisDone,
                    "localWisdomDone" to localWisdomDone,
                    "strategyChoiceDone" to strategyChoiceDone,
                    "caseAnalysisSummary" to caseAnalysisSummary.take(1000),
                    "quizScore" to quizScore.coerceIn(0L, 100L),
                    "quizCompleted" to quizCompleted,
                    "reflectionText" to reflectionText.take(2000),
                    "competencyRecommendation" to competencyRecommendation.take(1000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveAiRecord(
        userId: String,
        toolType: String,
        promptText: String,
        responseText: String,
        sourceMode: String,
        difficultyLevel: String
    ): Result<Unit> {
        val recordId = "ai_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val path = "users/$userId/ai_records/$recordId"
        return try {
            val uid = requireVerifiedUserId(userId)
            val payload = mapOf(
                "userId" to uid,
                "toolType" to toolType,
                "promptText" to promptText.trim().ifBlank { "Sesi AI PAI" }.take(600),
                "responseText" to responseText.trim().ifBlank { "Konten dihasilkan." }.take(5000),
                "sourceMode" to sourceMode,
                "difficultyLevel" to difficultyLevel,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid)
                .collection("ai_records").document(recordId)
                .set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun pushNotification(
        userId: String,
        title: String,
        message: String,
        stageNumber: Int
    ): Result<Unit> {
        val notifId = "notif_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val path = "users/$userId/notifications/$notifId"
        return try {
            val uid = requireVerifiedUserId(userId)
            val payload = mapOf(
                "userId" to uid,
                "title" to title.trim().ifBlank { "Pembaruan Progres" }.take(120),
                "message" to message.trim().ifBlank { "Progres tersimpan." }.take(400),
                "stageNumber" to stageNumber.coerceIn(1, 10).toLong(),
                "readFlag" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid)
                .collection("notifications").document(notifId)
                .set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(userId: String, notificationId: String): Result<Unit> {
        val path = "users/$userId/notifications/$notificationId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(notificationId.isNotBlank()) { "Notification ID kosong." }
            db.collection("users").document(uid)
                .collection("notifications").document(notificationId)
                .update(
                    mapOf(
                        "readFlag" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}

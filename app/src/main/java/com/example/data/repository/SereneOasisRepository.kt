package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ReferralCodeRecord
import com.example.data.model.ReferralRedemption
import com.example.data.model.ReviewTargetType
import com.example.data.model.ServiceReview
import com.example.data.model.SpaBooking
import com.example.data.model.UserProfile
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
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

class SereneOasisRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(
    context: Context
  ) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth,
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

  fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
    val docRef = db.collection("users").document(userId)
    emitAll(
      docRef
        .snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(
              UserProfile::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
          } else {
            null
          }
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path, auth)
          }
          throw error
        }
    )
  }

  fun observeBookings(userId: String): Flow<List<SpaBooking>> = flow {
    val colRef = db.collection("users").document(userId).collection("bookings")
    emitAll(
      colRef
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            SpaBooking::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path, auth)
          }
          throw error
        }
    )
  }

  private suspend fun allocateUniqueReferralCode(): String {
    val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    val rng = java.security.SecureRandom()
    repeat(5) {
      val suffix = buildString {
        repeat(6) { append(alphabet[rng.nextInt(alphabet.length)]) }
      }
      val candidate = "SALONX-$suffix"
      val snap = db.collection("referral_codes").document(candidate).get().await()
      if (!snap.exists()) return candidate
    }
    throw IllegalStateException("Could not generate a unique referral code. Please try again.")
  }

  private suspend fun createReferralCodeDoc(code: String, uid: String, ownerName: String) {
    val payload =
      mapOf(
        "code" to code,
        "ownerUid" to uid,
        "ownerName" to ownerName.trim().take(80).ifEmpty { "Sanctuary Guest" },
        "successfulReferrals" to 0L,
        "pointsEarned" to 0L,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    db.collection("referral_codes").document(code).set(payload).await()
  }

  suspend fun ensureUserProfile(
    displayName: String?,
    email: String?,
    preferredPressure: String = "Medium",
    aromatherapyNote: String? = "Eucalyptus & Bergamot",
  ): Result<Unit> {
    return try {
      val uid = requireUserId()
      val docRef = db.collection("users").document(uid)
      val existing = docRef.get().await()
      val validPressure =
        if (preferredPressure in listOf("Gentle", "Medium", "Firm", "Deep")) preferredPressure
        else "Medium"
      val cleanName = displayName?.trim()?.take(80)?.takeIf { it.isNotEmpty() } ?: "Sanctuary Guest"
      val cleanAroma = aromatherapyNote?.trim()?.take(200)
      if (!existing.exists()) {
        val generatedCode = allocateUniqueReferralCode()
        createReferralCodeDoc(code = generatedCode, uid = uid, ownerName = cleanName)
        val cleanEmail = email?.trim()?.take(120)?.takeIf { it.isNotEmpty() }
        val payload =
          mapOf(
            "userId" to uid,
            "displayName" to cleanName,
            "email" to cleanEmail,
            "membershipTier" to "SAGE_MEMBER",
            "wellnessPoints" to 250L,
            "preferredPressure" to validPressure,
            "aromatherapyNote" to (cleanAroma ?: "Eucalyptus & Bergamot"),
            "favoriteTreatmentIds" to emptyList<String>(),
            "referralCode" to generatedCode,
            "claimedReferralPoints" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ).filterValues { it != null }
        docRef.set(payload).await()
      } else if (existing.getString("referralCode").isNullOrBlank()) {
        val generatedCode = allocateUniqueReferralCode()
        val existingName = existing.getString("displayName").orEmpty().ifBlank { cleanName }
        createReferralCodeDoc(code = generatedCode, uid = uid, ownerName = existingName)
        docRef
          .update(
            mapOf(
              "referralCode" to generatedCode,
              "claimedReferralPoints" to 0L,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      handleFirestoreError(e, OperationType.CREATE, "users/$uid", auth)
      Result.failure(e)
    }
  }

  suspend fun createBooking(
    treatmentId: String,
    treatmentTitle: String,
    treatmentCategory: String,
    therapistId: String,
    therapistName: String,
    dateLabel: String,
    dateIso: String,
    timeSlot: String,
    durationMinutes: Long,
    priceUsd: Long,
    addOns: List<String>,
    specialRequests: String,
  ): Result<String> {
    return try {
      val uid = requireUserId()
      val docRef = db.collection("users").document(uid).collection("bookings").document()
      val payload =
        mapOf(
          "userId" to uid,
          "treatmentId" to treatmentId,
          "treatmentTitle" to treatmentTitle.take(120),
          "treatmentCategory" to treatmentCategory.take(60),
          "therapistId" to therapistId,
          "therapistName" to therapistName.take(80),
          "dateLabel" to dateLabel.take(40),
          "dateIso" to dateIso.take(20),
          "timeSlot" to timeSlot.take(20),
          "durationMinutes" to durationMinutes,
          "priceUsd" to priceUsd,
          "addOns" to addOns.take(10),
          "specialRequests" to specialRequests.trim().take(300),
          "status" to "confirmed",
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload).await()

      // Award wellness points if profile exists
      val userRef = db.collection("users").document(uid)
      val userSnap = userRef.get().await()
      if (userSnap.exists()) {
        val currentPoints = (userSnap.getLong("wellnessPoints") ?: 0L)
        val newPoints = (currentPoints + 75L).coerceAtMost(1_000_000L)
        val newTier = if (newPoints >= 500L) "GOLD_SANCTUARY" else "SAGE_MEMBER"
        userRef
          .update(
            mapOf(
              "wellnessPoints" to newPoints,
              "membershipTier" to newTier,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      handleFirestoreError(e, OperationType.CREATE, "users/$uid/bookings", auth)
      Result.failure(e)
    }
  }

  suspend fun getBookingById(bookingId: String): Result<SpaBooking?> {
    return try {
      val uid = requireUserId()
      val docRef = db.collection("users").document(uid).collection("bookings").document(bookingId)
      val snap = docRef.get().await()
      val booking =
        if (snap.exists()) {
          snap.toObject(SpaBooking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
          null
        }
      Result.success(booking)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      handleFirestoreError(e, OperationType.GET, "users/$uid/bookings/$bookingId", auth)
      Result.failure(e)
    }
  }

  suspend fun rescheduleBooking(
    bookingId: String,
    dateLabel: String,
    dateIso: String,
    timeSlot: String,
    specialRequests: String,
  ): Result<Unit> {
    return try {
      val uid = requireUserId()
      val docRef = db.collection("users").document(uid).collection("bookings").document(bookingId)
      docRef
        .update(
          mapOf(
            "dateLabel" to dateLabel.take(40),
            "dateIso" to dateIso.take(20),
            "timeSlot" to timeSlot.take(20),
            "specialRequests" to specialRequests.trim().take(300),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      handleFirestoreError(e, OperationType.UPDATE, "users/$uid/bookings/$bookingId", auth)
      Result.failure(e)
    }
  }

  suspend fun cancelBooking(bookingId: String): Result<Unit> {
    return try {
      val uid = requireUserId()
      val docRef = db.collection("users").document(uid).collection("bookings").document(bookingId)
      docRef
        .update(
          mapOf(
            "status" to "cancelled",
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      handleFirestoreError(e, OperationType.UPDATE, "users/$uid/bookings/$bookingId", auth)
      Result.failure(e)
    }
  }

  suspend fun toggleFavoriteTreatment(
    treatmentId: String,
    currentFavorites: List<String>,
  ): Result<Unit> {
    return try {
      val uid = requireUserId()
      val updated =
        if (treatmentId in currentFavorites) {
          currentFavorites.filterNot { it == treatmentId }
        } else {
          (currentFavorites + treatmentId).distinct().take(30)
        }
      val docRef = db.collection("users").document(uid)
      docRef
        .update(
          mapOf(
            "favoriteTreatmentIds" to updated,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      handleFirestoreError(e, OperationType.UPDATE, "users/$uid", auth)
      Result.failure(e)
    }
  }

  suspend fun updatePreferences(
    displayName: String,
    preferredPressure: String,
    aromatherapyNote: String,
  ): Result<Unit> {
    return try {
      val uid = requireUserId()
      val cleanName = displayName.trim().take(80).ifEmpty { "Sanctuary Guest" }
      val docRef = db.collection("users").document(uid)
      docRef
        .update(
          mapOf(
            "displayName" to cleanName,
            "preferredPressure" to preferredPressure,
            "aromatherapyNote" to aromatherapyNote.trim().take(200),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      handleFirestoreError(e, OperationType.UPDATE, "users/$uid", auth)
      Result.failure(e)
    }
  }

  fun observeReviews(userId: String): Flow<List<ServiceReview>> = flow {
    val colRef = db.collection("users").document(userId).collection("reviews")
    emitAll(
      colRef
        .orderBy("updatedAt", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            ServiceReview::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path, auth)
          }
          throw error
        }
    )
  }

  suspend fun submitReview(
    bookingId: String,
    targetType: ReviewTargetType,
    treatmentId: String,
    treatmentTitle: String,
    therapistId: String,
    therapistName: String,
    rating: Int,
    comment: String,
    reviewerName: String,
  ): Result<Unit> {
    return try {
      val uid = requireUserId()
      val cleanBookingId = bookingId.trim()
      require(cleanBookingId.isNotEmpty()) { "Booking ID is required to submit a review." }
      val cleanRating = rating.coerceIn(1, 5).toLong()
      val cleanComment = comment.trim().take(500)
      require(cleanComment.isNotEmpty()) { "Written feedback must not be empty." }
      val cleanReviewer = reviewerName.trim().take(80).ifEmpty { "Sanctuary Guest" }

      val docRef = db.collection("users").document(uid).collection("reviews").document(cleanBookingId)
      val existing = docRef.get().await()
      if (!existing.exists()) {
        val payload =
          mapOf(
            "userId" to uid,
            "bookingId" to cleanBookingId,
            "targetType" to targetType.name,
            "treatmentId" to treatmentId,
            "treatmentTitle" to treatmentTitle.take(120).ifEmpty { "Signature Ritual" },
            "therapistId" to therapistId,
            "therapistName" to therapistName.take(80).ifEmpty { "Lead Specialist" },
            "rating" to cleanRating,
            "comment" to cleanComment,
            "reviewerName" to cleanReviewer,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      } else {
        docRef
          .update(
            mapOf(
              "targetType" to targetType.name,
              "rating" to cleanRating,
              "comment" to cleanComment,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      val op = OperationType.WRITE
      handleFirestoreError(e, op, "users/$uid/reviews/$bookingId", auth)
      Result.failure(e)
    }
  }

  fun observeReferralCode(code: String): Flow<ReferralCodeRecord?> = flow {
    val docRef = db.collection("referral_codes").document(code)
    emitAll(
      docRef
        .snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(
              ReferralCodeRecord::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
          } else {
            null
          }
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path, auth)
          }
          throw error
        }
    )
  }

  fun observeReferralHistory(userId: String): Flow<List<ReferralRedemption>> = flow {
    val colRef = db.collection("referral_redemptions")
    emitAll(
      colRef
        .whereEqualTo("referrerUid", userId)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ReferralRedemption::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path, auth)
          }
          throw error
        }
    )
  }

  suspend fun syncReferralRewardsIfNeeded(
    codeRecord: ReferralCodeRecord,
    profile: UserProfile,
  ): Result<Long> {
    return try {
      val uid = requireUserId()
      val unclaimed = (codeRecord.pointsEarned - profile.claimedReferralPoints).coerceAtLeast(0L)
      if (unclaimed == 0L) return Result.success(0L)
      val newPoints = (profile.wellnessPoints + unclaimed).coerceAtMost(1_000_000L)
      val newTier = if (newPoints >= 500L) "GOLD_SANCTUARY" else "SAGE_MEMBER"
      db.collection("users")
        .document(uid)
        .update(
          mapOf(
            "wellnessPoints" to newPoints,
            "membershipTier" to newTier,
            "claimedReferralPoints" to codeRecord.pointsEarned,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(unclaimed)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      handleFirestoreError(e, OperationType.UPDATE, "users/$uid", auth)
      Result.failure(e)
    }
  }

  suspend fun redeemReferralCode(rawCode: String): Result<Long> {
    val cleanCode = rawCode.trim().uppercase()
    return try {
      val uid = requireUserId()
      require(REFERRAL_CODE_REGEX.matches(cleanCode)) {
        "Enter a valid code (e.g. SALONX-A1B2C3)."
      }

      val codeRef = db.collection("referral_codes").document(cleanCode)
      val codeSnap = codeRef.get().await()
      require(codeSnap.exists()) { "Referral code $cleanCode was not found." }
      val referrerUid = codeSnap.getString("ownerUid").orEmpty()
      require(referrerUid.isNotEmpty() && referrerUid != uid) {
        "You cannot redeem your own referral code."
      }

      val redemptionRef = db.collection("referral_redemptions").document(uid)
      val existingRedemption = redemptionRef.get().await()
      require(!existingRedemption.exists()) {
        "A referral welcome bonus has already been claimed on this account."
      }

      val userRef = db.collection("users").document(uid)
      val userSnap = userRef.get().await()
      require(userSnap.exists()) { "Please wait for your profile to finish loading." }
      val currentPoints = userSnap.getLong("wellnessPoints") ?: 250L
      val refereeName =
        userSnap.getString("displayName")?.trim()?.take(80)?.ifEmpty { "Sanctuary Guest" }
          ?: "Sanctuary Guest"
      val newPoints = (currentPoints + REFEREE_WELCOME_POINTS).coerceAtMost(1_000_000L)
      val newTier = if (newPoints >= 500L) "GOLD_SANCTUARY" else "SAGE_MEMBER"
      val prevReferrals = codeSnap.getLong("successfulReferrals") ?: 0L
      val prevEarned = codeSnap.getLong("pointsEarned") ?: 0L

      val batch = db.batch()
      batch.set(
        redemptionRef,
        mapOf(
          "refereeUid" to uid,
          "refereeName" to refereeName,
          "referrerUid" to referrerUid,
          "referralCode" to cleanCode,
          "rewardPointsReferrer" to REFERRER_REWARD_POINTS,
          "rewardPointsReferee" to REFEREE_WELCOME_POINTS,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.update(
        codeRef,
        mapOf(
          "successfulReferrals" to prevReferrals + 1L,
          "pointsEarned" to prevEarned + REFERRER_REWARD_POINTS,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.update(
        userRef,
        mapOf(
          "wellnessPoints" to newPoints,
          "membershipTier" to newTier,
          "redeemedReferralCode" to cleanCode,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
      Result.success(REFEREE_WELCOME_POINTS)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val uid = auth.currentUser?.uid ?: "unauthenticated"
      handleFirestoreError(e, OperationType.WRITE, "referral_redemptions/$uid", auth)
      Result.failure(e)
    }
  }

  companion object {
    const val REFERRER_REWARD_POINTS = 150L
    const val REFEREE_WELCOME_POINTS = 100L
    private val REFERRAL_CODE_REGEX = Regex("^SALONX-[A-Z0-9]{6}$")
  }
}

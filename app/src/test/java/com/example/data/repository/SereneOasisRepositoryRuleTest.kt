package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SereneOasisRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureUserProfile_authenticatedOwner_createsAndObservesProfile() = runBlocking {
    val freshEmail = "elena_${java.util.UUID.randomUUID().toString().take(8)}@sereneoasis.test"
    val uid = signInTestUser(freshEmail)
    val repo = SereneOasisRepository(firestore, auth)

    val result = withTimeout(DEFAULT_TIMEOUT_MS) { repo.ensureUserProfile("Elena Vance", freshEmail) }
    assertTrue(result.isSuccess)

    val profile = withTimeout(FLOW_TIMEOUT_MS) { repo.observeUserProfile(uid).first { it != null } }
    assertEquals(uid, profile?.userId)
    assertEquals("SAGE_MEMBER", profile?.membershipTier)
  }

  @Test
  fun createBooking_and_cancelBooking_lifecycleWorksAndLocksTerminalState() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = SereneOasisRepository(firestore, auth)
    withTimeout(DEFAULT_TIMEOUT_MS) { repo.ensureUserProfile("Elena Vance", ALICE_EMAIL).getOrThrow() }

    val bookingId =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.createBooking(
            treatmentId = "sage_botanical_facial",
            treatmentTitle = "Sage Botanical Renewal Facial",
            treatmentCategory = "Facials",
            therapistId = "claire_moreau",
            therapistName = "Claire Moreau",
            dateLabel = "Fri, Nov 20",
            dateIso = "2026-11-20",
            timeSlot = "2:30 PM",
            durationMinutes = 75L,
            priceUsd = 210L,
            addOns = listOf("Jade Gua Sha Lift"),
            specialRequests = "Warm blanket",
          )
          .getOrThrow()
      }
    assertTrue(bookingId.isNotEmpty())

    val bookings =
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeBookings(uid).first { list -> list.any { it.id == bookingId } }
      }
    assertEquals("confirmed", bookings.first { it.id == bookingId }.status)

    val cancelResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.cancelBooking(bookingId) }
    assertTrue(cancelResult.isSuccess)

    val rescheduleAfterCancel =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.rescheduleBooking(bookingId, "Sat, Nov 21", "2026-11-21", "4:00 PM", "Updated")
      }
    assertFalse(rescheduleAfterCancel.isSuccess)
    val ex = rescheduleAfterCancel.exceptionOrNull()?.unwrapFirestoreException()
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
  }

  @Test
  fun getBookingById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = SereneOasisRepository(firestore, auth)
    val bookingId =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        aliceRepo.createBooking(
            treatmentId = "golden_hammam_ritual",
            treatmentTitle = "Golden Hammam Ritual",
            treatmentCategory = "Body",
            therapistId = "nadia_khoury",
            therapistName = "Nadia Khoury",
            dateLabel = "Sat, Nov 21",
            dateIso = "2026-11-21",
            timeSlot = "11:00 AM",
            durationMinutes = 90L,
            priceUsd = 265L,
            addOns = emptyList(),
            specialRequests = "",
          )
          .getOrThrow()
      }

    signInTestUser(BOB_EMAIL)
    val bobRepo = SereneOasisRepository(firestore, auth)
    var crossUserError: Throwable? = null
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("users").document(aliceUid).collection("bookings").document(bookingId).get().await()
      }
    } catch (e: Throwable) {
      crossUserError = e
    }
    assertEquals(
      FirebaseFirestoreException.Code.PERMISSION_DENIED,
      crossUserError?.unwrapFirestoreException()?.code,
    )

    auth.signOut()
    val unauthResult = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getBookingById(bookingId) }
    assertTrue(unauthResult.isFailure)
  }

  @Test
  fun observeBookings_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = SereneOasisRepository(firestore, auth)

    var caught: Throwable? = null
    try {
      withTimeout(FLOW_TIMEOUT_MS) { repo.observeBookings("some_user_123").first() }
    } catch (e: Throwable) {
      caught = e
    }
    val firestoreException =
      caught?.unwrapFirestoreException()
        ?: error("Expected nested FirebaseFirestoreException, got $caught")
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
  }

  @Test
  fun submitReview_and_observeReviews_linksServiceAndStaffAndRejectsUnauthenticated() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = SereneOasisRepository(firestore, auth)

    val bookingId =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.createBooking(
            treatmentId = "sage_botanical_facial",
            treatmentTitle = "Sage Botanical Renewal Facial",
            treatmentCategory = "Facials",
            therapistId = "claire_moreau",
            therapistName = "Claire Moreau",
            dateLabel = "Mon, Nov 9",
            dateIso = "2026-11-09",
            timeSlot = "11:00 AM",
            durationMinutes = 75L,
            priceUsd = 210L,
            addOns = emptyList(),
            specialRequests = "",
          )
          .getOrThrow()
      }

    val createReviewResult =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.submitReview(
          bookingId = bookingId,
          targetType = com.example.data.model.ReviewTargetType.BOTH,
          treatmentId = "sage_botanical_facial",
          treatmentTitle = "Sage Botanical Renewal Facial",
          therapistId = "claire_moreau",
          therapistName = "Claire Moreau",
          rating = 5,
          comment = "Calming botanical facial and attentive specialist.",
          reviewerName = "Elena Vance",
        )
      }
    assertTrue("createReview failed: ${createReviewResult.exceptionOrNull()}", createReviewResult.isSuccess)

    val reviews =
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeReviews(uid).first { list -> list.any { it.bookingId == bookingId } }
      }
    val saved = reviews.first { it.bookingId == bookingId }
    assertEquals(5L, saved.rating)
    assertEquals("BOTH", saved.targetType)
    assertEquals("claire_moreau", saved.therapistId)

    // Update existing review rating & target type
    val updateResult =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.submitReview(
          bookingId = bookingId,
          targetType = com.example.data.model.ReviewTargetType.STAFF,
          treatmentId = "sage_botanical_facial",
          treatmentTitle = "Sage Botanical Renewal Facial",
          therapistId = "claire_moreau",
          therapistName = "Claire Moreau",
          rating = 4,
          comment = "Claire Moreau was wonderful and very gentle.",
          reviewerName = "Elena Vance",
        )
      }
    assertTrue("updateReview failed: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)

    // Unauthenticated observation must fail with PERMISSION_DENIED
    auth.signOut()
    var unauthError: Throwable? = null
    try {
      withTimeout(FLOW_TIMEOUT_MS) { repo.observeReviews(uid).first() }
    } catch (e: Throwable) {
      unauthError = e
    }
    assertEquals(
      FirebaseFirestoreException.Code.PERMISSION_DENIED,
      unauthError?.unwrapFirestoreException()?.code,
    )
  }

  @Test
  fun referralCode_generatedRedeemableOnce_blocksSelfReferralAndSyncsReferrerPoints() = runBlocking {
    val suffix = java.util.UUID.randomUUID().toString().take(8)
    val referrerEmail = "referrer_$suffix@sereneoasis.test"
    val refereeEmail = "referee_$suffix@sereneoasis.test"

    val referrerUid = signInTestUser(referrerEmail)
    val referrerRepo = SereneOasisRepository(firestore, auth)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      referrerRepo.ensureUserProfile("Claire Referrer", referrerEmail).getOrThrow()
    }

    val referrerProfile =
      withTimeout(FLOW_TIMEOUT_MS) {
        referrerRepo.observeUserProfile(referrerUid).first { it != null && it.referralCode.startsWith("SALONX-") }
      }!!
    val referralCode = referrerProfile.referralCode

    val codeRecordInitial =
      withTimeout(FLOW_TIMEOUT_MS) {
        referrerRepo.observeReferralCode(referralCode).first { it != null }
      }!!
    assertEquals(referrerUid, codeRecordInitial.ownerUid)
    assertEquals(0L, codeRecordInitial.successfulReferrals)

    // Self-referral must fail
    val selfRedeem = withTimeout(DEFAULT_TIMEOUT_MS) { referrerRepo.redeemReferralCode(referralCode) }
    assertTrue("Expected self-referral to fail", selfRedeem.isFailure)

    // Second user redeems the referral code
    val refereeUid = signInTestUser(refereeEmail)
    val refereeRepo = SereneOasisRepository(firestore, auth)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      refereeRepo.ensureUserProfile("Marcus Referee", refereeEmail).getOrThrow()
    }

    val redeemResult = withTimeout(DEFAULT_TIMEOUT_MS) { refereeRepo.redeemReferralCode(referralCode) }
    assertTrue("redeemReferralCode failed: ${redeemResult.exceptionOrNull()}", redeemResult.isSuccess)
    assertEquals(SereneOasisRepository.REFEREE_WELCOME_POINTS, redeemResult.getOrNull())

    val refereeProfile =
      withTimeout(FLOW_TIMEOUT_MS) {
        refereeRepo.observeUserProfile(refereeUid).first { it?.redeemedReferralCode == referralCode }
      }!!
    assertEquals(350L, refereeProfile.wellnessPoints)

    // Second redemption attempt by same referee must fail
    val duplicateRedeem = withTimeout(DEFAULT_TIMEOUT_MS) { refereeRepo.redeemReferralCode(referralCode) }
    assertTrue("Expected duplicate referral claim to fail", duplicateRedeem.isFailure)

    // Sign back in as referrer and verify history + automatic points sync (+150 pts)
    signInTestUser(referrerEmail)
    val updatedCodeRecord =
      withTimeout(FLOW_TIMEOUT_MS) {
        referrerRepo.observeReferralCode(referralCode).first { (it?.successfulReferrals ?: 0L) >= 1L }
      }!!
    assertEquals(1L, updatedCodeRecord.successfulReferrals)
    assertEquals(SereneOasisRepository.REFERRER_REWARD_POINTS, updatedCodeRecord.pointsEarned)

    val history =
      withTimeout(FLOW_TIMEOUT_MS) {
        referrerRepo.observeReferralHistory(referrerUid).first { list -> list.any { it.refereeUid == refereeUid } }
      }
    assertEquals("Marcus Referee", history.first { it.refereeUid == refereeUid }.refereeName)

    val syncedDelta =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        referrerRepo.syncReferralRewardsIfNeeded(updatedCodeRecord, referrerProfile).getOrThrow()
      }
    assertEquals(SereneOasisRepository.REFERRER_REWARD_POINTS, syncedDelta)

    val creditedReferrer =
      withTimeout(FLOW_TIMEOUT_MS) {
        referrerRepo.observeUserProfile(referrerUid).first { (it?.wellnessPoints ?: 0L) >= 400L }
      }!!
    assertEquals(400L, creditedReferrer.wellnessPoints)
    assertEquals(SereneOasisRepository.REFERRER_REWARD_POINTS, creditedReferrer.claimedReferralPoints)
  }

  private companion object {
    const val ALICE_EMAIL = "elena@sereneoasis.test"
    const val BOB_EMAIL = "marcus@sereneoasis.test"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}

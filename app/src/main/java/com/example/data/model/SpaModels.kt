package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "Sanctuary Guest",
  val email: String? = null,
  val membershipTier: String = "SAGE_MEMBER",
  val wellnessPoints: Long = 250L,
  val preferredPressure: String = "Medium",
  val aromatherapyNote: String? = "Eucalyptus & Bergamot",
  val favoriteTreatmentIds: List<String> = emptyList(),
  val referralCode: String = "",
  val claimedReferralPoints: Long = 0L,
  val redeemedReferralCode: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ReferralCodeRecord(
  @DocumentId val id: String = "",
  val code: String = "",
  val ownerUid: String = "",
  val ownerName: String = "Sanctuary Guest",
  val successfulReferrals: Long = 0L,
  val pointsEarned: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ReferralRedemption(
  @DocumentId val id: String = "",
  val refereeUid: String = "",
  val refereeName: String = "Sanctuary Guest",
  val referrerUid: String = "",
  val referralCode: String = "",
  val rewardPointsReferrer: Long = 150L,
  val rewardPointsReferee: Long = 100L,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class SpaBooking(
  @DocumentId val id: String = "",
  val userId: String = "",
  val treatmentId: String = "",
  val treatmentTitle: String = "",
  val treatmentCategory: String = "",
  val therapistId: String = "",
  val therapistName: String = "",
  val dateLabel: String = "",
  val dateIso: String = "",
  val timeSlot: String = "",
  val durationMinutes: Long = 60L,
  val priceUsd: Long = 180L,
  val addOns: List<String> = emptyList(),
  val specialRequests: String = "",
  val status: String = "confirmed",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

enum class ReviewTargetType(val label: String) {
  BOTH("Service & Specialist"),
  SERVICE("Service Only"),
  STAFF("Specialist Only"),
}

@IgnoreExtraProperties
data class ServiceReview(
  @DocumentId val id: String = "",
  val userId: String = "",
  val bookingId: String = "",
  val targetType: String = ReviewTargetType.BOTH.name,
  val treatmentId: String = "",
  val treatmentTitle: String = "",
  val therapistId: String = "",
  val therapistName: String = "",
  val rating: Long = 5L,
  val comment: String = "",
  val reviewerName: String = "Sanctuary Guest",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)


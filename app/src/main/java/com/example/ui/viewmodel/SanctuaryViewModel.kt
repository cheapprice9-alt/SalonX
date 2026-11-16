package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ReferralCodeRecord
import com.example.data.model.ReferralRedemption
import com.example.data.model.ReviewTargetType
import com.example.data.model.ServiceReview
import com.example.data.model.SpaBooking
import com.example.data.model.SpaCatalog
import com.example.data.model.SpaTreatment
import com.example.data.model.UserProfile
import com.example.data.repository.SereneOasisRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>

  data class Success<T>(val data: T) : UiState<T>

  data class Error(val message: String) : UiState<Nothing>
}

data class RegistrationPreferences(
  val displayName: String,
  val preferredPressure: String,
  val aromatherapyNote: String,
  val referralCode: String? = null,
)

class SanctuaryViewModel(
  private val repository: SereneOasisRepository,
  private val currentUserId: String,
  initialDisplayName: String?,
  initialEmail: String?,
  registrationPreferences: RegistrationPreferences? = null,
) : ViewModel() {

  private val bookingsRetryTrigger = MutableStateFlow(0)

  val profileState: StateFlow<UiState<UserProfile?>> =
    repository
      .observeUserProfile(currentUserId)
      .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing user profile", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load sanctuary profile"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  @OptIn(ExperimentalCoroutinesApi::class)
  val bookingsState: StateFlow<UiState<List<SpaBooking>>> =
    bookingsRetryTrigger
      .flatMapLatest {
        repository
          .observeBookings(currentUserId)
          .map<List<SpaBooking>, UiState<List<SpaBooking>>> { UiState.Success(it) }
          .catch { error ->
            val firestoreError =
              generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing bookings", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load reservations"))
          }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val reviewsState: StateFlow<UiState<List<ServiceReview>>> =
    repository
      .observeReviews(currentUserId)
      .map<List<ServiceReview>, UiState<List<ServiceReview>>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing reviews", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load reviews"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  @OptIn(ExperimentalCoroutinesApi::class)
  val referralCodeState: StateFlow<ReferralCodeRecord?> =
    profileState
      .flatMapLatest { state ->
        val code = (state as? UiState.Success)?.data?.referralCode.orEmpty()
        if (code.isBlank()) {
          kotlinx.coroutines.flow.flowOf(null)
        } else {
          repository.observeReferralCode(code).catch { error ->
            Log.w(TAG, "Error observing referral code", error)
            emit(null)
          }
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = null,
      )

  val referralHistoryState: StateFlow<UiState<List<ReferralRedemption>>> =
    repository
      .observeReferralHistory(currentUserId)
      .map<List<ReferralRedemption>, UiState<List<ReferralRedemption>>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing referral history", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load referral history"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  private val _selectedCategory = MutableStateFlow("All")
  val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _isSubmittingBooking = MutableStateFlow(false)
  val isSubmittingBooking: StateFlow<Boolean> = _isSubmittingBooking.asStateFlow()

  private val _isSubmittingReview = MutableStateFlow(false)
  val isSubmittingReview: StateFlow<Boolean> = _isSubmittingReview.asStateFlow()

  private val _isRedeemingReferral = MutableStateFlow(false)
  val isRedeemingReferral: StateFlow<Boolean> = _isRedeemingReferral.asStateFlow()

  private val _lastCreatedBookingId = MutableStateFlow<String?>(null)
  val lastCreatedBookingId: StateFlow<String?> = _lastCreatedBookingId.asStateFlow()

  private val _actionMessage = MutableStateFlow<String?>(null)
  val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

  init {
    viewModelScope.launch {
      val chosenName =
        registrationPreferences?.displayName?.trim()?.takeIf { it.isNotEmpty() }
          ?: initialDisplayName
      repository.ensureUserProfile(
        displayName = chosenName,
        email = initialEmail,
        preferredPressure = registrationPreferences?.preferredPressure ?: "Medium",
        aromatherapyNote = registrationPreferences?.aromatherapyNote ?: "Eucalyptus & Bergamot",
      )
      if (registrationPreferences != null) {
        repository.updatePreferences(
          displayName = chosenName ?: "Sanctuary Guest",
          preferredPressure = registrationPreferences.preferredPressure,
          aromatherapyNote = registrationPreferences.aromatherapyNote,
        )
        val pendingReferral = registrationPreferences.referralCode?.trim()
        if (!pendingReferral.isNullOrBlank()) {
          repository
            .redeemReferralCode(pendingReferral)
            .onSuccess { bonus ->
              _actionMessage.value = "Referral applied! +$bonus bonus wellness points added."
            }
            .onFailure { err ->
              _actionMessage.value = err.message ?: "Could not apply referral code."
            }
        }
      }
    }

    // Automatically credit any newly earned referral points from Firestore into UserProfile
    viewModelScope.launch {
      kotlinx.coroutines.flow.combine(profileState, referralCodeState) { pState, codeRecord ->
        Pair((pState as? UiState.Success)?.data, codeRecord)
      }.collect { (profile, codeRecord) ->
        if (profile != null && codeRecord != null && codeRecord.pointsEarned > profile.claimedReferralPoints) {
          repository.syncReferralRewardsIfNeeded(codeRecord, profile).onSuccess { credited ->
            if (credited > 0L) {
              _actionMessage.value = "A friend joined with your code! +$credited Wellness Points credited."
            }
          }
        }
      }
    }
  }

  fun selectCategory(category: String) {
    _selectedCategory.value = category
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun retryBookings() {
    bookingsRetryTrigger.value += 1
  }

  fun clearActionMessage() {
    _actionMessage.value = null
  }

  fun clearLastCreatedBooking() {
    _lastCreatedBookingId.value = null
  }

  fun toggleFavorite(treatmentId: String) {
    val currentFavs =
      (profileState.value as? UiState.Success)?.data?.favoriteTreatmentIds ?: emptyList()
    viewModelScope.launch {
      val result = repository.toggleFavoriteTreatment(treatmentId, currentFavs)
      if (result.isFailure) {
        _actionMessage.value = "Could not update saved treatments."
      }
    }
  }

  fun submitBooking(
    treatment: SpaTreatment,
    therapistId: String,
    therapistName: String,
    dateLabel: String,
    dateIso: String,
    timeSlot: String,
    selectedAddOnIds: Set<String>,
    specialRequests: String,
  ) {
    if (_isSubmittingBooking.value) return
    val chosenAddOns = SpaCatalog.addOns.filter { it.id in selectedAddOnIds }
    val totalMinutes = treatment.durationMinutes + chosenAddOns.sumOf { it.durationExtraMinutes }
    val totalPrice = treatment.priceUsd + chosenAddOns.sumOf { it.priceUsd }

    viewModelScope.launch {
      _isSubmittingBooking.value = true
      val result =
        repository.createBooking(
          treatmentId = treatment.id,
          treatmentTitle = treatment.title,
          treatmentCategory = treatment.category,
          therapistId = therapistId,
          therapistName = therapistName,
          dateLabel = dateLabel,
          dateIso = dateIso,
          timeSlot = timeSlot,
          durationMinutes = totalMinutes.toLong(),
          priceUsd = totalPrice.toLong(),
          addOns = chosenAddOns.map { it.name },
          specialRequests = specialRequests,
        )
      _isSubmittingBooking.value = false
      result
        .onSuccess { bookingId ->
          _lastCreatedBookingId.value = bookingId
          _actionMessage.value = "Reservation confirmed! +75 Wellness Points earned."
        }
        .onFailure {
          _actionMessage.value = "Unable to complete reservation. Please try again."
        }
    }
  }

  fun rescheduleBooking(
    bookingId: String,
    dateLabel: String,
    dateIso: String,
    timeSlot: String,
    specialRequests: String,
  ) {
    viewModelScope.launch {
      val result =
        repository.rescheduleBooking(
          bookingId = bookingId,
          dateLabel = dateLabel,
          dateIso = dateIso,
          timeSlot = timeSlot,
          specialRequests = specialRequests,
        )
      _actionMessage.value =
        if (result.isSuccess) "Appointment rescheduled to $dateLabel at $timeSlot."
        else "Could not reschedule appointment."
    }
  }

  fun cancelBooking(bookingId: String) {
    viewModelScope.launch {
      val result = repository.cancelBooking(bookingId)
      _actionMessage.value =
        if (result.isSuccess) "Your reservation has been cancelled."
        else "Could not cancel reservation."
    }
  }

  fun savePreferences(
    displayName: String,
    preferredPressure: String,
    aromatherapyNote: String,
  ) {
    viewModelScope.launch {
      val result = repository.updatePreferences(displayName, preferredPressure, aromatherapyNote)
      _actionMessage.value =
        if (result.isSuccess) "Sanctuary preferences saved."
        else "Unable to save preferences."
    }
  }

  fun submitReview(
    booking: SpaBooking,
    targetType: ReviewTargetType,
    rating: Int,
    comment: String,
    onCompleted: () -> Unit = {},
  ) {
    if (_isSubmittingReview.value) return
    val reviewer =
      (profileState.value as? UiState.Success)?.data?.displayName ?: "Sanctuary Guest"
    viewModelScope.launch {
      _isSubmittingReview.value = true
      val result =
        repository.submitReview(
          bookingId = booking.id,
          targetType = targetType,
          treatmentId = booking.treatmentId,
          treatmentTitle = booking.treatmentTitle,
          therapistId = booking.therapistId,
          therapistName = booking.therapistName,
          rating = rating,
          comment = comment,
          reviewerName = reviewer,
        )
      _isSubmittingReview.value = false
      result
        .onSuccess {
          _actionMessage.value = "Thank you! Your review has been saved."
          onCompleted()
        }
        .onFailure {
          _actionMessage.value = "Unable to submit review. Please try again."
        }
    }
  }

  fun redeemReferralCode(rawCode: String, onSuccess: () -> Unit = {}) {
    if (_isRedeemingReferral.value) return
    viewModelScope.launch {
      _isRedeemingReferral.value = true
      val result = repository.redeemReferralCode(rawCode)
      _isRedeemingReferral.value = false
      result
        .onSuccess { bonus ->
          _actionMessage.value = "Referral code applied! +$bonus Wellness Points awarded."
          onSuccess()
        }
        .onFailure { err ->
          _actionMessage.value = err.message ?: "Could not apply referral code."
        }
    }
  }

  private companion object {
    const val TAG = "SanctuaryVM"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}

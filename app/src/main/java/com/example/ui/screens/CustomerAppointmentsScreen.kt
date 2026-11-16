package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ReviewTargetType
import com.example.data.model.ServiceReview
import com.example.data.model.SpaBooking
import com.example.data.model.SpaCatalog
import com.example.ui.components.GoldBadge
import com.example.ui.components.ReviewComponent
import com.example.ui.components.SubmittedReviewCard
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.RefinedGold
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppointmentTab {
  UPCOMING,
  PAST,
}

fun currentTodayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

fun isUpcomingAppointment(booking: SpaBooking, todayIso: String): Boolean {
  if (booking.status != "confirmed") return false
  return booking.dateIso.isBlank() || booking.dateIso >= todayIso
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomerAppointmentsScreen(
  bookingsState: UiState<List<SpaBooking>>,
  onRetry: () -> Unit,
  onRescheduleBooking: (
    bookingId: String,
    dateLabel: String,
    dateIso: String,
    timeSlot: String,
    specialRequests: String,
  ) -> Unit,
  onCancelBooking: (String) -> Unit,
  onRebookTreatment: (treatmentId: String) -> Unit,
  onBrowseTreatments: () -> Unit,
  modifier: Modifier = Modifier,
  todayIso: String = remember { currentTodayIso() },
  reviews: List<ServiceReview> = emptyList(),
  isSubmittingReview: Boolean = false,
  onSubmitReview: (
    booking: SpaBooking,
    targetType: ReviewTargetType,
    rating: Int,
    comment: String,
    onCompleted: () -> Unit,
  ) -> Unit = { _, _, _, _, _ -> },
) {
  var selectedTab by remember { mutableStateOf(AppointmentTab.UPCOMING) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedDetailBooking by remember { mutableStateOf<SpaBooking?>(null) }
  var bookingToCancel by remember { mutableStateOf<SpaBooking?>(null) }
  var bookingToReschedule by remember { mutableStateOf<SpaBooking?>(null) }
  var bookingToReview by remember { mutableStateOf<SpaBooking?>(null) }
  val reviewsByBookingId = remember(reviews) { reviews.associateBy { it.bookingId } }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    when (bookingsState) {
      is UiState.Loading -> {
        Column(
          modifier =
            Modifier.fillMaxSize()
              .widthIn(max = 760.dp)
              .padding(24.dp)
              .testTag("appointments_loading"),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          CircularProgressIndicator(color = OasisPrimary)
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = stringResource(R.string.appointments_syncing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      is UiState.Error -> {
        Box(
          modifier =
            Modifier.fillMaxSize()
              .widthIn(max = 760.dp)
              .padding(24.dp)
              .testTag("appointments_error"),
          contentAlignment = Alignment.Center,
        ) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
          ) {
            Column(
              modifier = Modifier.padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp),
              )
              Text(
                text = "Could not load appointments",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
              )
              Text(
                text = bookingsState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Button(
                onClick = onRetry,
                shape = RoundedCornerShape(16.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = OasisPrimary,
                    contentColor = OasisOnPrimary,
                  ),
                modifier = Modifier.testTag("appointments_retry_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Refresh,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = stringResource(R.string.appointments_retry),
                  style = MaterialTheme.typography.labelLarge,
                )
              }
            }
          }
        }
      }

      is UiState.Success -> {
        val allBookings = bookingsState.data
        val upcomingBookings =
          remember(allBookings, todayIso) {
            allBookings
              .filter { isUpcomingAppointment(it, todayIso) }
              .sortedWith(compareBy<SpaBooking> { it.dateIso }.thenBy { it.timeSlot })
          }
        val pastBookings =
          remember(allBookings, todayIso) {
            allBookings
              .filterNot { isUpcomingAppointment(it, todayIso) }
              .sortedByDescending { it.dateIso }
          }

        val activeList =
          if (selectedTab == AppointmentTab.UPCOMING) upcomingBookings else pastBookings
        val filteredList =
          remember(activeList, searchQuery) {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) activeList
            else {
              activeList.filter {
                it.treatmentTitle.lowercase().contains(q) ||
                  it.therapistName.lowercase().contains(q) ||
                  it.dateLabel.lowercase().contains(q) ||
                  it.treatmentCategory.lowercase().contains(q)
              }
            }
          }

        Column(
          modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = stringResource(R.string.appointments_title),
                  style = MaterialTheme.typography.headlineLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                  shape = RoundedCornerShape(999.dp),
                  color = SoftSageTint,
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                  ) {
                    Box(
                      modifier =
                        Modifier.size(7.dp).clip(CircleShape).background(OasisPrimary)
                    )
                    Text(
                      text = "LIVE",
                      style = MaterialTheme.typography.labelSmall,
                      color = OasisPrimary,
                    )
                  }
                }
              }
              Text(
                text = stringResource(R.string.appointments_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              AppointmentTabChip(
                label = "Upcoming (${upcomingBookings.size})",
                icon = Icons.Filled.CalendarMonth,
                selected = selectedTab == AppointmentTab.UPCOMING,
                onClick = { selectedTab = AppointmentTab.UPCOMING },
                modifier = Modifier.weight(1f).testTag("tab_upcoming_appointments"),
              )
              AppointmentTabChip(
                label = "Past (${pastBookings.size})",
                icon = Icons.Filled.History,
                selected = selectedTab == AppointmentTab.PAST,
                onClick = { selectedTab = AppointmentTab.PAST },
                modifier = Modifier.weight(1f).testTag("tab_past_appointments"),
              )
            }
          }

          LazyColumn(
            modifier =
              Modifier.weight(1f)
                .fillMaxWidth()
                .testTag("appointments_lazy_column"),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
          ) {
            // Overview Highlights Card
            if (allBookings.isNotEmpty()) {
              item {
                AppointmentsOverviewCard(
                  nextUpcoming = upcomingBookings.firstOrNull(),
                  upcomingCount = upcomingBookings.size,
                  pastCount = pastBookings.size,
                )
              }
            }

          // Search filter when there are bookings in the current tab
          if (activeList.isNotEmpty()) {
            item {
              OutlinedTextField(
                value = searchQuery,
                onValueChange = { if (it.length <= 60) searchQuery = it },
                placeholder = { Text("Filter by treatment or specialist…") },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.cd_search),
                    tint = OasisPrimary,
                  )
                },
                trailingIcon = {
                  if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                      Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.cd_clear_search),
                      )
                    }
                  }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors =
                  OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OasisPrimary,
                    unfocusedBorderColor = OasisOutlineVariant.copy(alpha = 0.7f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                  ),
                modifier = Modifier.fillMaxWidth().testTag("appointments_search_input"),
              )
            }
          }

          // Appointment List or Empty State
          if (filteredList.isEmpty()) {
            item {
              EmptyAppointmentsCard(
                isPastTab = selectedTab == AppointmentTab.PAST,
                hasSearchFilter = searchQuery.isNotBlank(),
                onClearFilter = { searchQuery = "" },
                onBrowseTreatments = onBrowseTreatments,
              )
            }
          } else {
            items(filteredList, key = { it.id.ifBlank { "${it.treatmentId}_${it.dateIso}_${it.timeSlot}" } }) { booking ->
              val isUpcoming = isUpcomingAppointment(booking, todayIso)
              CustomerAppointmentCard(
                booking = booking,
                isUpcoming = isUpcoming,
                existingReview = reviewsByBookingId[booking.id],
                onOpenDetails = { selectedDetailBooking = booking },
                onReschedule = { bookingToReschedule = booking },
                onCancel = { bookingToCancel = booking },
                onLeaveReview = { bookingToReview = booking },
                onBookAgain = { onRebookTreatment(booking.treatmentId) },
              )
            }
          }
        }
        }
      }
    }
  }

  // Appointment Detail Modal Sheet
  selectedDetailBooking?.let { detail ->
    val isUpcoming = isUpcomingAppointment(detail, todayIso)
    ModalBottomSheet(
      onDismissRequest = { selectedDetailBooking = null },
      containerColor = MaterialTheme.colorScheme.surface,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          StatusBadge(status = detail.status, isUpcoming = isUpcoming)
          Text(
            text = "$${detail.priceUsd}",
            style = MaterialTheme.typography.headlineSmall,
            color = OasisPrimary,
          )
        }

        Text(
          text = detail.treatmentTitle,
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            DetailMetaRow(label = "Category", value = detail.treatmentCategory)
            DetailMetaRow(label = "Specialist", value = detail.therapistName)
            DetailMetaRow(label = "Date & Time", value = "${detail.dateLabel} • ${detail.timeSlot}")
            DetailMetaRow(label = "Duration", value = "${detail.durationMinutes} min")
            if (detail.addOns.isNotEmpty()) {
              DetailMetaRow(label = "Enhancements", value = detail.addOns.joinToString(", "))
            }
            if (detail.specialRequests.isNotBlank()) {
              DetailMetaRow(label = "Notes", value = detail.specialRequests)
            }
          }
        }

        if (isUpcoming) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            OutlinedButton(
              onClick = {
                selectedDetailBooking = null
                bookingToReschedule = detail
              },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f),
            ) {
              Text(stringResource(R.string.action_reschedule))
            }
            Button(
              onClick = {
                selectedDetailBooking = null
                bookingToCancel = detail
              },
              shape = RoundedCornerShape(14.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.error,
                  contentColor = MaterialTheme.colorScheme.onError,
                ),
              modifier = Modifier.weight(1f),
            ) {
              Text(stringResource(R.string.action_cancel_ritual))
            }
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            if (detail.status != "cancelled") {
              OutlinedButton(
                onClick = {
                  selectedDetailBooking = null
                  bookingToReview = detail
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(50.dp),
              ) {
                Text(
                  if (reviewsByBookingId[detail.id] == null) "Leave Review" else "Edit Review"
                )
              }
            }
            Button(
              onClick = {
                selectedDetailBooking = null
                onRebookTreatment(detail.treatmentId)
              },
              shape = RoundedCornerShape(14.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = OasisPrimary,
                  contentColor = OasisOnPrimary,
                ),
              modifier = Modifier.weight(1f).height(50.dp),
            ) {
              Text(stringResource(R.string.appointments_book_again))
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }
    }
  }

  // Review & Feedback Modal Bottom Sheet for Completed Appointments
  bookingToReview?.let { target ->
    ModalBottomSheet(
      onDismissRequest = { if (!isSubmittingReview) bookingToReview = null },
      containerColor = MaterialTheme.colorScheme.surface,
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
      ) {
        ReviewComponent(
          booking = target,
          existingReview = reviewsByBookingId[target.id],
          isSubmitting = isSubmittingReview,
          onSubmitReview = { targetType, rating, comment ->
            onSubmitReview(target, targetType, rating, comment) {
              bookingToReview = null
            }
          },
        )
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // Cancel confirmation dialog
  bookingToCancel?.let { target ->
    AlertDialog(
      onDismissRequest = { bookingToCancel = null },
      title = {
        Text(
          text = "Cancel Appointment?",
          style = MaterialTheme.typography.headlineSmall,
        )
      },
      text = {
        Text(
          text =
            "Are you sure you want to cancel your ${target.treatmentTitle} appointment on ${target.dateLabel} at ${target.timeSlot}?",
          style = MaterialTheme.typography.bodyMedium,
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onCancelBooking(target.id)
            bookingToCancel = null
          },
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
          Text(stringResource(R.string.action_cancel_ritual))
        }
      },
      dismissButton = {
        TextButton(onClick = { bookingToCancel = null }) {
          Text("Keep Appointment")
        }
      },
    )
  }

  // Reschedule bottom sheet
  bookingToReschedule?.let { target ->
    val dateOptions = remember { buildUpcomingDates(7) }
    var newDateIdx by remember(target.id) { mutableIntStateOf(0) }
    var newTimeSlot by remember(target.id) { mutableStateOf(target.timeSlot) }
    var updatedNotes by remember(target.id) { mutableStateOf(target.specialRequests) }

    ModalBottomSheet(
      onDismissRequest = { bookingToReschedule = null },
      containerColor = MaterialTheme.colorScheme.surface,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text(
          text = "Reschedule Appointment",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = target.treatmentTitle,
          style = MaterialTheme.typography.bodyMedium,
          color = OasisPrimary,
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          itemsIndexed(dateOptions) { idx, opt ->
            val selected = idx == newDateIdx
            Surface(
              modifier =
                Modifier.clip(RoundedCornerShape(12.dp))
                  .clickable { newDateIdx = idx }
                  .minimumInteractiveComponentSize(),
              shape = RoundedCornerShape(12.dp),
              color =
                if (selected) OasisPrimary else MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  text = opt.dayShort,
                  style = MaterialTheme.typography.labelSmall,
                  color =
                    if (selected) OasisOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = opt.dayNum,
                  style = MaterialTheme.typography.titleMedium,
                  color = if (selected) OasisOnPrimary else MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SpaCatalog.timeSlots.forEach { slot ->
            val selected = slot == newTimeSlot
            Surface(
              modifier =
                Modifier.clip(RoundedCornerShape(999.dp))
                  .clickable { newTimeSlot = slot }
                  .minimumInteractiveComponentSize(),
              shape = RoundedCornerShape(999.dp),
              color =
                if (selected) OasisPrimary else MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
              Text(
                text = slot,
                style = MaterialTheme.typography.labelLarge,
                color =
                  if (selected) OasisOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              )
            }
          }
        }

        OutlinedTextField(
          value = updatedNotes,
          onValueChange = { if (it.length <= 300) updatedNotes = it },
          label = { Text("Preferences & Sensory Notes") },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(),
        )

        Button(
          onClick = {
            val chosen = dateOptions[newDateIdx]
            onRescheduleBooking(
              target.id,
              chosen.dateLabel,
              chosen.dateIso,
              newTimeSlot,
              updatedNotes,
            )
            bookingToReschedule = null
          },
          shape = RoundedCornerShape(16.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = OasisPrimary,
              contentColor = OasisOnPrimary,
            ),
          modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
          Text("Save New Time", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun AppointmentsOverviewCard(
  nextUpcoming: SpaBooking?,
  upcomingCount: Int,
  pastCount: Int,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.55f)),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier =
        Modifier.background(
            Brush.linearGradient(colors = listOf(WarmCream, SoftSageTint.copy(alpha = 0.55f)))
          )
          .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (nextUpcoming != null) "NEXT APPOINTMENT" else "VISIT HISTORY",
            style = MaterialTheme.typography.labelSmall,
            color = OasisPrimary,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = nextUpcoming?.treatmentTitle ?: "No upcoming appointments scheduled",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          if (nextUpcoming != null) {
            Text(
              text = "${nextUpcoming.dateLabel} at ${nextUpcoming.timeSlot} • ${nextUpcoming.therapistName}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        if (nextUpcoming != null) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = OasisPrimary,
            modifier = Modifier.size(28.dp),
          )
        }
      }

      HorizontalDivider(color = OasisOutlineVariant.copy(alpha = 0.4f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "$upcomingCount upcoming",
          style = MaterialTheme.typography.labelMedium,
          color = OasisPrimary,
        )
        Text(
          text = "$pastCount past visits",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun AppointmentTabChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .minimumInteractiveComponentSize(),
    shape = RoundedCornerShape(16.dp),
    color = if (selected) OasisPrimary else MaterialTheme.colorScheme.surfaceContainerLow,
    border =
      if (selected) null else BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.65f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) OasisOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) OasisOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun StatusBadge(status: String, isUpcoming: Boolean) {
  when {
    isUpcoming -> {
      Surface(shape = RoundedCornerShape(999.dp), color = SoftSageTint) {
        Text(
          text = "UPCOMING • CONFIRMED",
          style = MaterialTheme.typography.labelSmall,
          color = OasisPrimary,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
    }
    status == "cancelled" -> {
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.errorContainer,
      ) {
        Text(
          text = "CANCELLED",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onErrorContainer,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
    }
    else -> {
      GoldBadge(text = "COMPLETED")
    }
  }
}

@Composable
private fun CustomerAppointmentCard(
  booking: SpaBooking,
  isUpcoming: Boolean,
  existingReview: ServiceReview?,
  onOpenDetails: () -> Unit,
  onReschedule: () -> Unit,
  onCancel: () -> Unit,
  onLeaveReview: () -> Unit,
  onBookAgain: () -> Unit,
) {
  Surface(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onOpenDetails)
        .testTag("appointment_card_${booking.id}"),
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border =
      BorderStroke(
        width = 0.75.dp,
        color =
          if (isUpcoming) OasisPrimary.copy(alpha = 0.35f)
          else OasisOutlineVariant.copy(alpha = 0.5f),
      ),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        StatusBadge(status = booking.status, isUpcoming = isUpcoming)
        Text(
          text = "$${booking.priceUsd}",
          style = MaterialTheme.typography.headlineSmall,
          color = if (isUpcoming) OasisPrimary else RefinedGold,
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = booking.treatmentTitle,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = booking.treatmentCategory,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = OasisPrimary,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "${booking.dateLabel} at ${booking.timeSlot}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = OasisPrimary,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "Specialist: ${booking.therapistName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = OasisPrimary,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "${booking.durationMinutes} min session",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (booking.addOns.isNotEmpty()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = WarmCream.copy(alpha = 0.7f),
        ) {
          Text(
            text = "Add-ons: ${booking.addOns.joinToString(", ")}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().padding(10.dp),
          )
        }
      }

      HorizontalDivider(color = OasisOutlineVariant.copy(alpha = 0.4f))

      if (isUpcoming) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
            onClick = onReschedule,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, OasisPrimary.copy(alpha = 0.55f)),
            modifier = Modifier.weight(1f).testTag("reschedule_btn_${booking.id}"),
          ) {
            Text(
              text = stringResource(R.string.action_reschedule),
              style = MaterialTheme.typography.labelLarge,
              color = OasisPrimary,
            )
          }
          OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
            modifier = Modifier.weight(1f).testTag("cancel_btn_${booking.id}"),
          ) {
            Text(
              text = stringResource(R.string.action_cancel_ritual),
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.error,
            )
          }
        }
      } else {
        if (existingReview != null) {
          SubmittedReviewCard(
            review = existingReview,
            onEditClick = onLeaveReview,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          if (booking.status != "cancelled" && existingReview == null) {
            OutlinedButton(
              onClick = onLeaveReview,
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, RefinedGold.copy(alpha = 0.75f)),
              modifier = Modifier.weight(1f).testTag("review_btn_${booking.id}"),
            ) {
              Text(
                text = "Leave Review",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
          Button(
            onClick = onBookAgain,
            shape = RoundedCornerShape(14.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = OasisPrimary,
                contentColor = OasisOnPrimary,
              ),
            modifier = Modifier.weight(1f).testTag("book_again_btn_${booking.id}"),
          ) {
            Text(
              text = stringResource(R.string.appointments_book_again),
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyAppointmentsCard(
  isPastTab: Boolean,
  hasSearchFilter: Boolean,
  onClearFilter: () -> Unit,
  onBrowseTreatments: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("appointments_empty_card"),
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
  ) {
    Column(
      modifier = Modifier.padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.Spa,
        contentDescription = null,
        tint = OasisPrimary.copy(alpha = 0.7f),
        modifier = Modifier.size(44.dp),
      )
      Text(
        text =
          when {
            hasSearchFilter -> "No matching appointments"
            isPastTab -> "No past appointments yet"
            else -> "No upcoming appointments"
          },
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
      Text(
        text =
          when {
            hasSearchFilter -> "Try clearing your search filter to view all appointments."
            isPastTab -> "Completed and cancelled appointments will appear here for easy rebooking."
            else -> "Book your next SalonX hair, spa, or wellness session in seconds."
          },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(4.dp))
      if (hasSearchFilter) {
        OutlinedButton(onClick = onClearFilter, shape = RoundedCornerShape(14.dp)) {
          Text("Clear Filter")
        }
      } else if (!isPastTab) {
        Button(
          onClick = onBrowseTreatments,
          shape = RoundedCornerShape(16.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = OasisPrimary,
              contentColor = OasisOnPrimary,
            ),
          modifier = Modifier.testTag("empty_browse_treatments_button"),
        ) {
          Text(
            text = stringResource(R.string.action_browse_treatments),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
  }
}

@Composable
private fun DetailMetaRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.width(108.dp),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.End,
      modifier = Modifier.weight(1f),
    )
  }
}

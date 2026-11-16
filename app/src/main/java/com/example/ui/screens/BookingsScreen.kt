package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SpaBooking
import com.example.data.model.SpaCatalog
import com.example.ui.components.GoldBadge
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingsScreen(
  bookings: List<SpaBooking>,
  onRescheduleBooking: (
    bookingId: String,
    dateLabel: String,
    dateIso: String,
    timeSlot: String,
    specialRequests: String,
  ) -> Unit,
  onCancelBooking: (String) -> Unit,
  onBrowseTreatments: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showPast by remember { mutableStateOf(false) }
  var bookingToCancel by remember { mutableStateOf<SpaBooking?>(null) }
  var bookingToReschedule by remember { mutableStateOf<SpaBooking?>(null) }

  val filteredBookings =
    remember(bookings, showPast) {
      if (showPast) bookings.filter { it.status != "confirmed" }
      else bookings.filter { it.status == "confirmed" }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(
            text = "My Reservations",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val upcomingCount = bookings.count { it.status == "confirmed" }
            val pastCount = bookings.count { it.status != "confirmed" }
            FilterTabPill(
              label = "Upcoming ($upcomingCount)",
              selected = !showPast,
              onClick = { showPast = false },
              modifier = Modifier.testTag("tab_upcoming_bookings"),
            )
            FilterTabPill(
              label = "Past & Cancelled ($pastCount)",
              selected = showPast,
              onClick = { showPast = true },
              modifier = Modifier.testTag("tab_past_bookings"),
            )
          }
        }
      }

      if (filteredBookings.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
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
                tint = OasisPrimary.copy(alpha = 0.65f),
                modifier = Modifier.size(44.dp),
              )
              Text(
                text =
                  if (showPast) "No past or cancelled visits"
                  else "Your sanctuary calendar is open",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
              )
              Text(
                text =
                  if (showPast) "Completed and cancelled rituals will appear here for easy rebooking."
                  else "Reserve a botanical facial, warm stone massage, or half-day immersion.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
              )
              if (!showPast) {
                Spacer(modifier = Modifier.height(4.dp))
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
      } else {
        items(filteredBookings, key = { it.id }) { booking ->
          BookingCard(
            booking = booking,
            onReschedule = { bookingToReschedule = booking },
            onCancel = { bookingToCancel = booking },
          )
        }
      }
    }
  }

  // Cancel confirmation dialog
  bookingToCancel?.let { target ->
    AlertDialog(
      onDismissRequest = { bookingToCancel = null },
      title = {
        Text(
          text = "Cancel Reservation?",
          style = MaterialTheme.typography.headlineSmall,
        )
      },
      text = {
        Text(
          text =
            "Are you sure you want to release your ${target.treatmentTitle} suite on ${target.dateLabel} at ${target.timeSlot}?",
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
          Text("Cancel Ritual")
        }
      },
      dismissButton = {
        TextButton(onClick = { bookingToCancel = null }) {
          Text("Keep Reservation")
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
          text = "Reschedule Ritual",
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
          label = { Text("Sensory Notes") },
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
private fun FilterTabPill(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .clip(RoundedCornerShape(999.dp))
        .clickable(onClick = onClick)
        .minimumInteractiveComponentSize(),
    shape = RoundedCornerShape(999.dp),
    color = if (selected) OasisPrimary else MaterialTheme.colorScheme.surfaceContainerLow,
    border =
      if (selected) null else BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.7f)),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = if (selected) OasisOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
    )
  }
}

@Composable
private fun BookingCard(
  booking: SpaBooking,
  onReschedule: () -> Unit,
  onCancel: () -> Unit,
) {
  val isConfirmed = booking.status == "confirmed"
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("booking_card_${booking.id}"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.45f)),
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
        if (isConfirmed) {
          Surface(shape = RoundedCornerShape(999.dp), color = SoftSageTint) {
            Text(
              text = "CONFIRMED",
              style = MaterialTheme.typography.labelSmall,
              color = OasisPrimary,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
        } else {
          GoldBadge(text = booking.status.uppercase())
        }
        Text(
          text = "$${booking.priceUsd}",
          style = MaterialTheme.typography.headlineSmall,
          color = OasisPrimary,
        )
      }

      Text(
        text = booking.treatmentTitle,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )

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
            text = "Practitioner: ${booking.therapistName}",
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
            text = "${booking.durationMinutes} min total ritual",
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
            text = "Enhancements: ${booking.addOns.joinToString(", ")}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().padding(10.dp),
          )
        }
      }

      if (booking.specialRequests.isNotBlank()) {
        Text(
          text = "Note: \"${booking.specialRequests}\"",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      if (isConfirmed) {
        HorizontalDivider(color = OasisOutlineVariant.copy(alpha = 0.4f))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
            onClick = onReschedule,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, OasisPrimary.copy(alpha = 0.5f)),
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
      }
    }
  }
}

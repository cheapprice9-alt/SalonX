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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.model.SpaCatalog
import com.example.data.model.SpaTreatment
import com.example.ui.components.GoldBadge
import com.example.ui.components.OasisNetworkImage
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class SanctuaryDateOption(
  val dayShort: String,
  val dayNum: String,
  val dateLabel: String,
  val dateIso: String,
)

fun buildUpcomingDates(days: Int = 7): List<SanctuaryDateOption> {
  val dayShortFmt = SimpleDateFormat("EEE", Locale.US)
  val dayNumFmt = SimpleDateFormat("d", Locale.US)
  val labelFmt = SimpleDateFormat("EEE, MMM d", Locale.US)
  val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  return (0 until days).map { offset ->
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset + 1) }
    val date = cal.time
    SanctuaryDateOption(
      dayShort = dayShortFmt.format(date).uppercase(),
      dayNum = dayNumFmt.format(date),
      dateLabel = labelFmt.format(date),
      dateIso = isoFmt.format(date),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingFlowScreen(
  treatment: SpaTreatment,
  isSubmitting: Boolean,
  confirmedBookingId: String?,
  onConfirmBooking: (
    therapistId: String,
    therapistName: String,
    dateLabel: String,
    dateIso: String,
    timeSlot: String,
    selectedAddOnIds: Set<String>,
    specialRequests: String,
  ) -> Unit,
  onBack: () -> Unit,
  onViewBookings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val upcomingDates = remember { buildUpcomingDates(7) }
  val eligibleTherapists =
    remember(treatment) {
      SpaCatalog.therapists.filter { treatment.category in it.availableCategories }
        .ifEmpty { SpaCatalog.therapists }
    }

  var selectedTherapist by remember(treatment) { mutableStateOf(eligibleTherapists.first()) }
  var selectedDateIndex by remember { mutableIntStateOf(0) }
  var selectedTimeSlot by remember { mutableStateOf(SpaCatalog.timeSlots[2]) }
  var selectedAddOnIds by remember { mutableStateOf(setOf<String>()) }
  var specialRequests by remember { mutableStateOf("") }

  val chosenAddOns = remember(selectedAddOnIds) {
    SpaCatalog.addOns.filter { it.id in selectedAddOnIds }
  }
  val totalMinutes = treatment.durationMinutes + chosenAddOns.sumOf { it.durationExtraMinutes }
  val totalPrice = treatment.priceUsd + chosenAddOns.sumOf { it.priceUsd }

  if (confirmedBookingId != null) {
    Box(
      modifier =
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(24.dp),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 4.dp,
      ) {
        Column(
          modifier = Modifier.padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Box(
            modifier = Modifier.size(68.dp).clip(CircleShape).background(SoftSageTint),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = OasisPrimary,
              modifier = Modifier.size(38.dp),
            )
          }

          GoldBadge(text = "+75 WELLNESS POINTS EARNED", showStar = true)

          Text(
            text = "Your Sanctuary Suite is Reserved",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
          )

          Text(
            text =
              "${treatment.title} with ${selectedTherapist.name}\n${upcomingDates[selectedDateIndex].dateLabel} at $selectedTimeSlot",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )

          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = WarmCream.copy(alpha = 0.7f),
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "Total Duration: $totalMinutes min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "$$totalPrice",
                style = MaterialTheme.typography.titleMedium,
                color = OasisPrimary,
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Button(
            onClick = onViewBookings,
            shape = RoundedCornerShape(16.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = OasisPrimary,
                contentColor = OasisOnPrimary,
              ),
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("view_my_bookings_button"),
          ) {
            Text(
              text = stringResource(R.string.action_view_bookings),
              style = MaterialTheme.typography.labelLarge,
            )
          }

          OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OasisOutlineVariant),
            modifier = Modifier.fillMaxWidth().height(50.dp),
          ) {
            Text(
              text = "Return to Sanctuary",
              style = MaterialTheme.typography.labelLarge,
              color = OasisPrimary,
            )
          }
        }
      }
    }
    return
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.surface,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Reserve Your Ritual",
            style = MaterialTheme.typography.headlineSmall,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).testTag("booking_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.cd_back),
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
          ),
      )
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 6.dp,
      ) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .navigationBarsPadding()
              .padding(horizontal = 24.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "$totalMinutes MIN • ${upcomingDates[selectedDateIndex].dateLabel}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "$$totalPrice",
              style = MaterialTheme.typography.headlineMedium,
              color = OasisPrimary,
            )
          }
          Button(
            onClick = {
              val dateOpt = upcomingDates[selectedDateIndex]
              onConfirmBooking(
                selectedTherapist.id,
                selectedTherapist.name,
                dateOpt.dateLabel,
                dateOpt.dateIso,
                selectedTimeSlot,
                selectedAddOnIds,
                specialRequests,
              )
            },
            enabled = !isSubmitting,
            shape = RoundedCornerShape(16.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = OasisPrimary,
                contentColor = OasisOnPrimary,
              ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            modifier = Modifier.testTag("confirm_reservation_button"),
          ) {
            if (isSubmitting) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = OasisOnPrimary,
                strokeWidth = 2.dp,
              )
            } else {
              Text(
                text = stringResource(R.string.action_confirm_reservation),
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .widthIn(max = 760.dp)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        // Selected Treatment Summary Card
        Surface(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
          shape = RoundedCornerShape(16.dp),
          color = WarmCream.copy(alpha = 0.7f),
          border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Box(
              modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(SoftSageTint),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = null,
                tint = OasisPrimary,
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = treatment.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${treatment.durationMinutes} min base ritual • $${treatment.priceUsd}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        // Step 1: Select Therapist
        Column(
          modifier = Modifier.padding(horizontal = 24.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "1. Select Practitioner",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
          eligibleTherapists.forEach { therapist ->
            val isSelected = therapist.id == selectedTherapist.id
            Surface(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .clickable { selectedTherapist = therapist }
                  .testTag("select_therapist_${therapist.id}"),
              shape = RoundedCornerShape(16.dp),
              color =
                if (isSelected) SoftSageTint
                else MaterialTheme.colorScheme.surfaceContainerLowest,
              border =
                BorderStroke(
                  width = if (isSelected) 1.5.dp else 0.75.dp,
                  color = if (isSelected) OasisPrimary else OasisOutlineVariant.copy(alpha = 0.5f),
                ),
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                OasisNetworkImage(
                  imageUrl = therapist.avatarUrl,
                  contentDescription = therapist.name,
                  modifier = Modifier.size(48.dp).clip(CircleShape),
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = therapist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = therapist.specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                RadioButton(
                  selected = isSelected,
                  onClick = { selectedTherapist = therapist },
                  colors = RadioButtonDefaults.colors(selectedColor = OasisPrimary),
                )
              }
            }
          }
        }

        HorizontalDivider(
          modifier = Modifier.padding(horizontal = 24.dp),
          color = OasisOutlineVariant.copy(alpha = 0.45f),
        )

        // Step 2: Date & Time Slot
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(
            text = "2. Choose Date & Arrival Time",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp),
          )

          LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            itemsIndexed(upcomingDates) { index, dateOpt ->
              val isSelected = index == selectedDateIndex
              Surface(
                modifier =
                  Modifier.width(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { selectedDateIndex = index }
                    .minimumInteractiveComponentSize()
                    .testTag("date_option_$index"),
                shape = RoundedCornerShape(16.dp),
                color =
                  if (isSelected) OasisPrimary
                  else MaterialTheme.colorScheme.surfaceContainerLowest,
                border =
                  if (isSelected) null
                  else BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.6f)),
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 12.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = dateOpt.dayShort,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                      if (isSelected) OasisOnPrimary.copy(alpha = 0.85f)
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Text(
                    text = dateOpt.dayNum,
                    style = MaterialTheme.typography.titleLarge,
                    color =
                      if (isSelected) OasisOnPrimary else MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }

          FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            SpaCatalog.timeSlots.forEach { slot ->
              val isSelected = slot == selectedTimeSlot
              Surface(
                modifier =
                  Modifier.clip(RoundedCornerShape(999.dp))
                    .clickable { selectedTimeSlot = slot }
                    .minimumInteractiveComponentSize()
                    .testTag("timeslot_${slot.replace(" ", "_").replace(":", "")}"),
                shape = RoundedCornerShape(999.dp),
                color =
                  if (isSelected) OasisPrimary else MaterialTheme.colorScheme.surfaceContainerLow,
                border =
                  if (isSelected) null
                  else BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.7f)),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Filled.Check,
                      contentDescription = null,
                      tint = OasisOnPrimary,
                      modifier = Modifier.size(14.dp),
                    )
                  }
                  Text(
                    text = slot,
                    style = MaterialTheme.typography.labelLarge,
                    color =
                      if (isSelected) OasisOnPrimary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }

        HorizontalDivider(
          modifier = Modifier.padding(horizontal = 24.dp),
          color = OasisOutlineVariant.copy(alpha = 0.45f),
        )

        // Step 3: Ritual Enhancements & Notes
        Column(
          modifier = Modifier.padding(horizontal = 24.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "3. Ritual Enhancements (Optional)",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
          SpaCatalog.addOns.forEach { addOn ->
            val checked = addOn.id in selectedAddOnIds
            Surface(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .clickable {
                    selectedAddOnIds =
                      if (checked) selectedAddOnIds - addOn.id else selectedAddOnIds + addOn.id
                  }
                  .testTag("addon_${addOn.id}"),
              shape = RoundedCornerShape(16.dp),
              color =
                if (checked) SoftSageTint else MaterialTheme.colorScheme.surfaceContainerLowest,
              border =
                BorderStroke(
                  width = if (checked) 1.25.dp else 0.75.dp,
                  color = if (checked) OasisPrimary else OasisOutlineVariant.copy(alpha = 0.5f),
                ),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Checkbox(
                  checked = checked,
                  onCheckedChange = { isChecked ->
                    selectedAddOnIds =
                      if (isChecked) selectedAddOnIds + addOn.id else selectedAddOnIds - addOn.id
                  },
                  colors = CheckboxDefaults.colors(checkedColor = OasisPrimary),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = addOn.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = addOn.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "+$${addOn.priceUsd}",
                  style = MaterialTheme.typography.titleSmall,
                  color = OasisPrimary,
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = specialRequests,
            onValueChange = { if (it.length <= 300) specialRequests = it },
            label = { Text("Sensory & Room Preferences") },
            placeholder = { Text("e.g. Warmed table, unscented oil, chamomile tea…") },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(16.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedBorderColor = OasisPrimary,
                unfocusedBorderColor = OasisOutlineVariant.copy(alpha = 0.7f),
              ),
            modifier = Modifier.fillMaxWidth().testTag("special_requests_input"),
          )
        }
      }
    }
  }
}

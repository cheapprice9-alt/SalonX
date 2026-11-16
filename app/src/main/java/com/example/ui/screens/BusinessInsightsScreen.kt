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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SpaBooking
import com.example.data.model.UserProfile
import com.example.ui.components.GoldBadge
import com.example.ui.components.WeeklyBookingVolumeBarChart
import com.example.ui.components.WeeklyRevenueLineChart
import com.example.ui.insights.BusinessInsightsSummary
import com.example.ui.insights.InsightsWindow
import com.example.ui.insights.computeBusinessInsights
import com.example.ui.insights.pickDefaultInsightsWindow
import com.example.ui.theme.OasisError
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.OasisSecondary
import com.example.ui.theme.RefinedGold
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SoftGoldBg
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BusinessInsightsScreen(
  profile: UserProfile?,
  bookingsState: UiState<List<SpaBooking>>,
  onRetry: () -> Unit,
  onBrowseTreatments: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  todayIso: String = remember { currentTodayIso() },
) {
  val salonTitle =
    remember(profile?.aromatherapyNote) {
      val note = profile?.aromatherapyNote.orEmpty()
      if (note.startsWith("Salon Partner:")) {
        note.removePrefix("Salon Partner:").trim().ifEmpty { "SalonX Studio" }
      } else {
        "SalonX Owner Analytics"
      }
    }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.surface,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = stringResource(R.string.insights_title),
              style = MaterialTheme.typography.titleLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = salonTitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("insights_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.cd_back),
            )
          }
        },
        actions = {
          GoldBadge(
            text = "OWNER SUITE",
            showStar = true,
            modifier = Modifier.padding(end = 16.dp),
          )
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
          ),
      )
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      when (bookingsState) {
        is UiState.Loading -> {
          Box(
            modifier = Modifier.fillMaxSize().testTag("insights_loading_state"),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              CircularProgressIndicator(color = OasisPrimary)
              Text(
                text = "Syncing salon metrics from Firestore…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        is UiState.Error -> {
          Box(
            modifier = Modifier.fillMaxSize().padding(24.dp).testTag("insights_error_state"),
            contentAlignment = Alignment.Center,
          ) {
            Surface(
              modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
              shape = RoundedCornerShape(22.dp),
              color = MaterialTheme.colorScheme.surfaceContainerLowest,
              border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
            ) {
              Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.ErrorOutline,
                  contentDescription = null,
                  tint = OasisError,
                  modifier = Modifier.size(36.dp),
                )
                Text(
                  text = "Unable to load Business Insights",
                  style = MaterialTheme.typography.titleMedium,
                  textAlign = TextAlign.Center,
                )
                Text(
                  text = bookingsState.message,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                )
                Button(
                  onClick = onRetry,
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = OasisPrimary,
                      contentColor = OasisOnPrimary,
                    ),
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.testTag("insights_retry_button"),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.size(6.dp))
                  Text(stringResource(R.string.appointments_retry))
                }
              }
            }
          }
        }

        is UiState.Success -> {
          val bookings = bookingsState.data
          var hasUserPickedWindow by remember { mutableStateOf(false) }
          var selectedWindow by
            remember(todayIso) {
              mutableStateOf(pickDefaultInsightsWindow(bookings, todayIso))
            }
          androidx.compose.runtime.LaunchedEffect(bookings, todayIso) {
            if (!hasUserPickedWindow) {
              selectedWindow = pickDefaultInsightsWindow(bookings, todayIso)
            }
          }
          val summary =
            remember(bookings, selectedWindow, todayIso) {
              computeBusinessInsights(
                bookings = bookings,
                window = selectedWindow,
                todayIso = todayIso,
              )
            }

          var selectedDayIndex by
            remember(summary.dailyPoints) {
              val peakIdx =
                summary.dailyPoints
                  .withIndex()
                  .maxByOrNull { it.value.revenueUsd * 100 + it.value.totalBookings }
                  ?.index ?: 0
              mutableIntStateOf(peakIdx.coerceIn(0, (summary.dailyPoints.size - 1).coerceAtLeast(0)))
            }

          InsightsDashboardContent(
            summary = summary,
            hasAnyBookings = bookings.isNotEmpty(),
            selectedDayIndex = selectedDayIndex,
            onSelectWindow = { window ->
              hasUserPickedWindow = true
              selectedWindow = window
            },
            onSelectDay = { idx -> selectedDayIndex = idx },
            onBrowseTreatments = onBrowseTreatments,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InsightsDashboardContent(
  summary: BusinessInsightsSummary,
  hasAnyBookings: Boolean,
  selectedDayIndex: Int,
  onSelectWindow: (InsightsWindow) -> Unit,
  onSelectDay: (Int) -> Unit,
  onBrowseTreatments: () -> Unit,
) {
  val selectedDay = summary.dailyPoints.getOrNull(selectedDayIndex)

  LazyColumn(
    modifier =
      Modifier.fillMaxSize()
        .widthIn(max = 800.dp)
        .testTag("insights_lazy_column"),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    // Window Filter Chips
    item {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        InsightsWindow.entries.forEach { window ->
          val selected = window == summary.window
          Surface(
            modifier =
              Modifier.clip(RoundedCornerShape(999.dp))
                .clickable { onSelectWindow(window) }
                .minimumInteractiveComponentSize()
                .testTag("insights_window_${window.name.lowercase()}"),
            shape = RoundedCornerShape(999.dp),
            color = if (selected) OasisPrimary else MaterialTheme.colorScheme.surfaceContainerLow,
            border =
              if (selected) null
              else BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.6f)),
          ) {
            Text(
              text = window.label,
              style = MaterialTheme.typography.labelLarge,
              color =
                if (selected) OasisOnPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
          }
        }
      }
    }

    // Empty-state onboarding tip if zero bookings exist in Firestore
    if (!hasAnyBookings) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth().testTag("insights_empty_banner"),
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLowest,
          border = BorderStroke(0.75.dp, RefinedGold.copy(alpha = 0.45f)),
        ) {
          Column(
            modifier =
              Modifier.background(
                  Brush.linearGradient(colors = listOf(WarmCream, SoftSageTint.copy(alpha = 0.55f)))
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Storefront,
                contentDescription = null,
                tint = OasisPrimary,
              )
              Text(
                text = "Live Firestore Analytics Ready",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            Text(
              text =
                "No reservations are recorded yet. Create a booking to watch weekly revenue trends and booking volume update in real time.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
              onClick = onBrowseTreatments,
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, OasisPrimary),
              modifier = Modifier.testTag("insights_explore_treatments_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = null,
                tint = OasisPrimary,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.size(6.dp))
              Text(
                text = stringResource(R.string.action_browse_treatments),
                color = OasisPrimary,
              )
            }
          }
        }
      }
    }

    // 2x2 KPI Summary Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          InsightKpiCard(
            label = "Weekly Revenue",
            value = "$${summary.windowRevenueUsd}",
            subtitle =
              if (summary.peakDayLabel != null) "Peak: ${summary.peakDayLabel}"
              else "Active bookings total",
            icon = Icons.Filled.AttachMoney,
            containerColor = WarmCream,
            accentColor = OasisPrimary,
            modifier = Modifier.weight(1f).testTag("kpi_weekly_revenue"),
          )
          InsightKpiCard(
            label = "Booking Volume",
            value = "${summary.windowActiveBookings}",
            subtitle =
              if (summary.windowCancelledBookings > 0)
                "${summary.windowCancelledBookings} cancelled in window"
              else "Confirmed & completed",
            icon = Icons.Filled.CalendarMonth,
            containerColor = SoftSageTint,
            accentColor = OasisPrimary,
            modifier = Modifier.weight(1f).testTag("kpi_booking_volume"),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          InsightKpiCard(
            label = "Avg. Ticket",
            value = "$${summary.averageTicketUsd}",
            subtitle = "${summary.retentionRatePercent}% fulfillment rate",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            containerColor = SoftGoldBg,
            accentColor = OasisSecondary,
            modifier = Modifier.weight(1f).testTag("kpi_avg_ticket"),
          )
          InsightKpiCard(
            label = "All-Time Earned",
            value = "$${summary.totalAllTimeRevenueUsd}",
            subtitle = "${summary.totalAllTimeActiveBookings} total active visits",
            icon = Icons.Filled.Insights,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            accentColor = RefinedGold,
            modifier = Modifier.weight(1f).testTag("kpi_all_time_revenue"),
          )
        }
      }
    }

    // Interactive Day Inspection Strip
    if (selectedDay != null) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth().testTag("insights_selected_day_pill"),
          shape = RoundedCornerShape(16.dp),
          color = SoftSageTint.copy(alpha = 0.7f),
          border = BorderStroke(0.75.dp, OasisPrimary.copy(alpha = 0.25f)),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "SELECTED DAY • ${selectedDay.fullDateLabel.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = OasisPrimary,
              )
              Text(
                text =
                  "${selectedDay.activeBookings} active • ${selectedDay.cancelledBookings} cancelled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Text(
              text = "$${selectedDay.revenueUsd}",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = OasisPrimary,
            )
          }
        }
      }
    }

    // Weekly Revenue Trend Chart Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth().testTag("insights_revenue_chart_card"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
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
            Column {
              Text(
                text = "Weekly Revenue Trend",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Tap any day to inspect daily revenue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.TrendingUp,
              contentDescription = null,
              tint = OasisPrimary,
            )
          }
          WeeklyRevenueLineChart(
            points = summary.dailyPoints,
            selectedIndex = selectedDayIndex,
            onSelectDay = onSelectDay,
          )
        }
      }
    }

    // Weekly Booking Volume Bar Chart Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth().testTag("insights_volume_chart_card"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
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
            Column {
              Text(
                text = "Daily Booking Volume",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                LegendDot(color = OasisPrimary, label = "Active")
                LegendDot(color = OasisError.copy(alpha = 0.72f), label = "Cancelled")
              }
            }
            Icon(
              imageVector = Icons.Filled.BarChart,
              contentDescription = null,
              tint = OasisPrimary,
            )
          }
          WeeklyBookingVolumeBarChart(
            points = summary.dailyPoints,
            selectedIndex = selectedDayIndex,
            onSelectDay = onSelectDay,
          )
        }
      }
    }

    // Revenue by Treatment Category
    if (summary.categoryBreakdown.isNotEmpty()) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth().testTag("insights_category_breakdown_card"),
          shape = RoundedCornerShape(22.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLowest,
          border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = "Revenue by Ritual Category",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            summary.categoryBreakdown.forEach { cat ->
              val percent = (cat.shareFraction * 100).toInt()
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "${cat.category} (${cat.bookingCount})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "$${cat.revenueUsd} • $percent%",
                    style = MaterialTheme.typography.labelLarge,
                    color = OasisPrimary,
                  )
                }
                LinearProgressIndicator(
                  progress = { cat.shareFraction },
                  modifier =
                    Modifier.fillMaxWidth()
                      .height(7.dp)
                      .clip(RoundedCornerShape(999.dp)),
                  color = SageGreen,
                  trackColor = MaterialTheme.colorScheme.surfaceContainerLow,
                  strokeCap = StrokeCap.Round,
                )
              }
            }
          }
        }
      }
    }

    // Stylist / Therapist Leaderboard
    if (summary.therapistLeaderboard.isNotEmpty()) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth().testTag("insights_therapist_card"),
          shape = RoundedCornerShape(22.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLowest,
          border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = "Specialist Performance",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            summary.therapistLeaderboard.forEachIndexed { idx, therapist ->
              if (idx > 0) {
                HorizontalDivider(color = OasisOutlineVariant.copy(alpha = 0.35f))
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Box(
                    modifier =
                      Modifier.size(34.dp)
                        .clip(CircleShape)
                        .background(SoftGoldBg),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Person,
                      contentDescription = null,
                      tint = OasisSecondary,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                  Column {
                    Text(
                      text = therapist.therapistName,
                      style = MaterialTheme.typography.titleSmall,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    val unit = if (therapist.bookingCount == 1) "booking" else "bookings"
                    Text(
                      text = "${therapist.bookingCount} $unit",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
                Text(
                  text = "$${therapist.revenueUsd}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = OasisPrimary,
                )
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.navigationBarsPadding().height(12.dp))
    }
  }
}

@Composable
private fun InsightKpiCard(
  label: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  containerColor: Color,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.semantics(mergeDescendants = true) {},
    shape = RoundedCornerShape(20.dp),
    color = containerColor,
    border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.35f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Box(
          modifier =
            Modifier.size(30.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp),
          )
        }
      }
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun LegendDot(
  color: Color,
  label: String,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    Box(
      modifier = Modifier.size(8.dp).clip(CircleShape).background(color),
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

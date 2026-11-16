package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.SpaBooking
import com.example.data.model.UserProfile
import com.example.ui.screens.currentTodayIso
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.OasisSecondary
import com.example.ui.theme.RefinedGold
import com.example.ui.theme.SoftGoldBg
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream

const val POINTS_PER_COMPLETED_BOOKING = 75L
private const val GOLD_TIER_THRESHOLD = 500L
private const val PLATINUM_TIER_THRESHOLD = 1000L

data class LoyaltySummary(
  val totalPoints: Long,
  val completedBookingsCount: Int,
  val pointsFromCompletedBookings: Long,
  val upcomingBookingsCount: Int,
  val pendingPoints: Long,
  val currentTierLabel: String,
  val nextTierLabel: String,
  val nextTierTarget: Long,
  val progressFraction: Float,
)

fun isCompletedBooking(booking: SpaBooking, todayIso: String): Boolean {
  if (booking.status == "cancelled") return false
  if (booking.status == "completed") return true
  return booking.status == "confirmed" && booking.dateIso.isNotBlank() && booking.dateIso < todayIso
}

fun computeLoyaltySummary(
  profile: UserProfile?,
  bookings: List<SpaBooking>,
  todayIso: String = currentTodayIso(),
): LoyaltySummary {
  val completedCount = bookings.count { isCompletedBooking(it, todayIso) }
  val pointsFromCompleted = completedCount * POINTS_PER_COMPLETED_BOOKING
  val upcomingCount =
    bookings.count {
      it.status == "confirmed" && (it.dateIso.isBlank() || it.dateIso >= todayIso)
    }
  val pendingPoints = upcomingCount * POINTS_PER_COMPLETED_BOOKING
  val totalPoints = maxOf(profile?.wellnessPoints ?: 250L, pointsFromCompleted)

  val isGoldOrHigher = profile?.membershipTier == "GOLD_SANCTUARY" || totalPoints >= GOLD_TIER_THRESHOLD
  val currentTierLabel = if (isGoldOrHigher) "Gold Sanctuary" else "Sage Member"
  val nextTierLabel = if (isGoldOrHigher) "Platinum Oasis" else "Gold Sanctuary"
  val nextTierTarget = if (isGoldOrHigher) PLATINUM_TIER_THRESHOLD else GOLD_TIER_THRESHOLD
  val progressFraction = (totalPoints.toFloat() / nextTierTarget.toFloat()).coerceIn(0f, 1f)

  return LoyaltySummary(
    totalPoints = totalPoints,
    completedBookingsCount = completedCount,
    pointsFromCompletedBookings = pointsFromCompleted,
    upcomingBookingsCount = upcomingCount,
    pendingPoints = pendingPoints,
    currentTierLabel = currentTierLabel,
    nextTierLabel = nextTierLabel,
    nextTierTarget = nextTierTarget,
    progressFraction = progressFraction,
  )
}

@Composable
fun LoyaltyPointsTrackerCard(
  profile: UserProfile?,
  bookings: List<SpaBooking>,
  onViewHistory: () -> Unit,
  modifier: Modifier = Modifier,
  todayIso: String = remember { currentTodayIso() },
) {
  val summary =
    remember(profile, bookings, todayIso) {
      computeLoyaltySummary(profile = profile, bookings = bookings, todayIso = todayIso)
    }
  val pointsRemaining = (summary.nextTierTarget - summary.totalPoints).coerceAtLeast(0L)

  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .clickable(onClick = onViewHistory)
        .testTag("loyalty_points_tracker_card"),
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border = BorderStroke(0.85.dp, RefinedGold.copy(alpha = 0.45f)),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier =
        Modifier.background(
            Brush.linearGradient(
              colors = listOf(WarmCream, SoftGoldBg.copy(alpha = 0.55f), SoftSageTint.copy(alpha = 0.4f))
            )
          )
          .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Top Tier Header + Total Points Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier =
              Modifier.size(42.dp)
                .clip(CircleShape)
                .background(SoftGoldBg),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.AutoAwesome,
              contentDescription = null,
              tint = RefinedGold,
              modifier = Modifier.size(22.dp),
            )
          }
          Column {
            Text(
              text = "SALONX LOYALTY",
              style = MaterialTheme.typography.labelSmall,
              color = OasisSecondary,
            )
            Text(
              text = summary.currentTierLabel,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        GoldBadge(
          text = "${summary.totalPoints} PTS",
          showStar = true,
          modifier = Modifier.testTag("loyalty_total_points_badge"),
        )
      }

      // Earned from Completed Bookings Highlight Strip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f),
          border = BorderStroke(0.75.dp, OasisPrimary.copy(alpha = 0.25f)),
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = OasisPrimary,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = "Completed Visits",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Text(
              text = "+${summary.pointsFromCompletedBookings} pts",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = OasisPrimary,
              modifier = Modifier.testTag("loyalty_completed_points_text"),
            )
            val visitWord = if (summary.completedBookingsCount == 1) "visit" else "visits"
            Text(
              text = "${summary.completedBookingsCount} completed $visitWord",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f),
          border = BorderStroke(0.75.dp, RefinedGold.copy(alpha = 0.35f)),
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = OasisSecondary,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = "Upcoming Bonus",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Text(
              text = "+${summary.pendingPoints} pts",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = OasisSecondary,
              modifier = Modifier.testTag("loyalty_pending_points_text"),
            )
            Text(
              text = "+$POINTS_PER_COMPLETED_BOOKING pts per booking",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      // Tier Progress Indicator
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearProgressIndicator(
          progress = { summary.progressFraction },
          modifier =
            Modifier.fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(999.dp))
              .testTag("loyalty_tier_progress_bar"),
          color = OasisPrimary,
          trackColor = OasisOutlineVariant.copy(alpha = 0.35f),
          strokeCap = StrokeCap.Round,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "${summary.totalPoints} / ${summary.nextTierTarget} pts",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text =
              if (pointsRemaining == 0L) "${summary.nextTierLabel} unlocked"
              else "$pointsRemaining pts to ${summary.nextTierLabel}",
            style = MaterialTheme.typography.labelSmall,
            color = OasisPrimary,
          )
        }
      }

      HorizontalDivider(color = OasisOutlineVariant.copy(alpha = 0.35f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "View completed bookings & points history",
          style = MaterialTheme.typography.labelMedium,
          color = OasisPrimary,
        )
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Open appointments history",
          tint = OasisPrimary,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

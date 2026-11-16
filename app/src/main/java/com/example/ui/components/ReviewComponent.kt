package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ReviewTargetType
import com.example.data.model.ServiceReview
import com.example.data.model.SpaBooking
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.OasisSecondary
import com.example.ui.theme.RefinedGold
import com.example.ui.theme.SoftGoldBg
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream

private val QuickFeedbackPrompts =
  listOf(
    "Attentive specialist",
    "Calming ambience",
    "Visible results",
    "On-time suite",
    "Luxurious botanicals",
  )

fun ratingDescriptor(rating: Int): String =
  when (rating) {
    1 -> "Needs Care"
    2 -> "Fair"
    3 -> "Good"
    4 -> "Very Good"
    5 -> "Exceptional"
    else -> "Tap a star to rate"
  }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewComponent(
  booking: SpaBooking,
  existingReview: ServiceReview? = null,
  isSubmitting: Boolean = false,
  onSubmitReview: (
    targetType: ReviewTargetType,
    rating: Int,
    comment: String,
  ) -> Unit,
  modifier: Modifier = Modifier,
) {
  val initialTarget =
    remember(existingReview?.targetType) {
      ReviewTargetType.entries.firstOrNull { it.name == existingReview?.targetType }
        ?: ReviewTargetType.BOTH
    }
  var selectedTarget by remember(booking.id, existingReview?.id) { mutableStateOf(initialTarget) }
  var rating by remember(booking.id, existingReview?.id) {
    mutableIntStateOf((existingReview?.rating ?: 5L).toInt().coerceIn(1, 5))
  }
  var comment by remember(booking.id, existingReview?.id) {
    mutableStateOf(existingReview?.comment.orEmpty())
  }
  var validationError by remember(booking.id) { mutableStateOf<String?>(null) }

  Column(
    modifier = modifier.fillMaxWidth().testTag("review_component_container"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Appointment context banner
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = WarmCream,
      border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = if (existingReview == null) "RATE COMPLETED VISIT" else "UPDATE YOUR REVIEW",
          style = MaterialTheme.typography.labelSmall,
          color = OasisPrimary,
        )
        Text(
          text = booking.treatmentTitle,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Specialist: ${booking.therapistName} • ${booking.dateLabel}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Target selector: Link review to Service, Staff Member, or Both
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "Link Review To",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ReviewTargetType.entries.forEach { target ->
          val selected = target == selectedTarget
          val chipText =
            when (target) {
              ReviewTargetType.BOTH -> "Service & Staff"
              ReviewTargetType.SERVICE -> "Service: ${booking.treatmentTitle.take(18)}"
              ReviewTargetType.STAFF -> "Staff: ${booking.therapistName}"
            }
          Surface(
            modifier =
              Modifier.clip(RoundedCornerShape(999.dp))
                .clickable(enabled = !isSubmitting) { selectedTarget = target }
                .minimumInteractiveComponentSize()
                .testTag("review_target_${target.name.lowercase()}"),
            shape = RoundedCornerShape(999.dp),
            color = if (selected) OasisPrimary else MaterialTheme.colorScheme.surfaceContainerLow,
            border =
              if (selected) null
              else BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.7f)),
          ) {
            Text(
              text = chipText,
              style = MaterialTheme.typography.labelMedium,
              color = if (selected) OasisOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
          }
        }
      }
    }

    // Interactive 1-5 Star Rating Selector
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.45f)),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          (1..5).forEach { star ->
            val filled = star <= rating
            val scale by
              animateFloatAsState(
                targetValue = if (star == rating) 1.15f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "star_scale_$star",
              )
            IconButton(
              onClick = {
                rating = star
                validationError = null
              },
              enabled = !isSubmitting,
              modifier = Modifier.size(48.dp).testTag("review_star_$star"),
            ) {
              Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Rate $star out of 5 stars",
                tint = if (filled) RefinedGold else OasisOutlineVariant,
                modifier = Modifier.size(32.dp).scale(scale),
              )
            }
          }
        }
        Text(
          text = "$rating / 5 • ${ratingDescriptor(rating)}",
          style = MaterialTheme.typography.labelLarge,
          color = OasisSecondary,
          modifier = Modifier.testTag("review_rating_label"),
        )
      }
    }

    // Quick Feedback Prompt Chips
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "Highlights (Optional)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        QuickFeedbackPrompts.forEach { prompt ->
          val alreadyPresent = comment.contains(prompt, ignoreCase = true)
          Surface(
            modifier =
              Modifier.clip(RoundedCornerShape(999.dp))
                .clickable(enabled = !isSubmitting && !alreadyPresent) {
                  val next =
                    if (comment.isBlank()) prompt
                    else "${comment.trimEnd('.', ' ')}. $prompt"
                  if (next.length <= 500) {
                    comment = next
                    validationError = null
                  }
                }
                .minimumInteractiveComponentSize(),
            shape = RoundedCornerShape(999.dp),
            color = if (alreadyPresent) SoftSageTint else MaterialTheme.colorScheme.surfaceContainerLowest,
            border =
              BorderStroke(
                0.75.dp,
                if (alreadyPresent) OasisPrimary else OasisOutlineVariant.copy(alpha = 0.6f),
              ),
          ) {
            Text(
              text = if (alreadyPresent) "✓ $prompt" else "+ $prompt",
              style = MaterialTheme.typography.labelSmall,
              color = if (alreadyPresent) OasisPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
          }
        }
      }
    }

    // Written Feedback Input
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      OutlinedTextField(
        value = comment,
        onValueChange = {
          if (it.length <= 500) {
            comment = it
            validationError = null
          }
        },
        enabled = !isSubmitting,
        label = { Text("Written Feedback") },
        placeholder = {
          Text("Share how the ${booking.treatmentTitle} or ${booking.therapistName} made you feel…")
        },
        minLines = 3,
        maxLines = 5,
        isError = validationError != null,
        shape = RoundedCornerShape(16.dp),
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OasisPrimary,
            unfocusedBorderColor = OasisOutlineVariant.copy(alpha = 0.7f),
          ),
        modifier = Modifier.fillMaxWidth().testTag("review_comment_input"),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = validationError.orEmpty(),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
        )
        Text(
          text = "${comment.length} / 500",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    Button(
      onClick = {
        val cleanComment = comment.trim()
        if (cleanComment.isEmpty()) {
          validationError = "Please share brief written feedback before submitting."
          return@Button
        }
        validationError = null
        onSubmitReview(selectedTarget, rating, cleanComment)
      },
      enabled = !isSubmitting,
      shape = RoundedCornerShape(16.dp),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = OasisPrimary,
          contentColor = OasisOnPrimary,
        ),
      contentPadding = PaddingValues(vertical = 14.dp),
      modifier = Modifier.fillMaxWidth().height(52.dp).testTag("review_submit_button"),
    ) {
      if (isSubmitting) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = OasisOnPrimary,
          strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("Saving Review…")
      } else {
        Text(
          text = if (existingReview == null) "Submit Review" else "Update Review",
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }
}

@Composable
fun SubmittedReviewCard(
  review: ServiceReview,
  modifier: Modifier = Modifier,
  onEditClick: (() -> Unit)? = null,
) {
  val targetEnum =
    ReviewTargetType.entries.firstOrNull { it.name == review.targetType } ?: ReviewTargetType.BOTH
  Surface(
    modifier = modifier.fillMaxWidth().testTag("submitted_review_card_${review.bookingId}"),
    shape = RoundedCornerShape(16.dp),
    color = SoftGoldBg.copy(alpha = 0.55f),
    border = BorderStroke(0.75.dp, RefinedGold.copy(alpha = 0.45f)),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          (1..5).forEach { idx ->
            Icon(
              imageVector = if (idx <= review.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
              contentDescription = null,
              tint = if (idx <= review.rating) RefinedGold else OasisOutlineVariant,
              modifier = Modifier.size(16.dp),
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${review.rating}.0",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = OasisSecondary,
          )
        }

        if (onEditClick != null) {
          TextButton(
            onClick = onEditClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.testTag("edit_review_btn_${review.bookingId}"),
          ) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = null,
              tint = OasisPrimary,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Edit",
              style = MaterialTheme.typography.labelMedium,
              color = OasisPrimary,
            )
          }
        }
      }

      Text(
        text = review.comment,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Box(
          modifier = Modifier.size(20.dp).clip(CircleShape).background(SoftSageTint),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector =
              if (targetEnum == ReviewTargetType.STAFF) Icons.Filled.Person else Icons.Filled.Spa,
            contentDescription = null,
            tint = OasisPrimary,
            modifier = Modifier.size(12.dp),
          )
        }
        val linkText =
          when (targetEnum) {
            ReviewTargetType.SERVICE -> "Service: ${review.treatmentTitle}"
            ReviewTargetType.STAFF -> "Specialist: ${review.therapistName}"
            ReviewTargetType.BOTH -> "${review.treatmentTitle} • ${review.therapistName}"
          }
        Text(
          text = linkText,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

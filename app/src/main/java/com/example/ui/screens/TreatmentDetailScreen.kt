package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ServiceReview
import com.example.data.model.SpaTreatment
import com.example.ui.components.GoldBadge
import com.example.ui.components.OasisNetworkImage
import com.example.ui.components.SubmittedReviewCard
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TreatmentDetailScreen(
  treatment: SpaTreatment,
  isFavorite: Boolean,
  onToggleFavorite: () -> Unit,
  onBack: () -> Unit,
  onReserveClick: () -> Unit,
  modifier: Modifier = Modifier,
  treatmentReviews: List<ServiceReview> = emptyList(),
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.surface,
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
              text = "RITUAL INVESTMENT",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "$${treatment.priceUsd}",
                style = MaterialTheme.typography.headlineMedium,
                color = OasisPrimary,
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "/ ${treatment.durationMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
              )
            }
          }
          Button(
            onClick = onReserveClick,
            shape = RoundedCornerShape(16.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = OasisPrimary,
                contentColor = OasisOnPrimary,
              ),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
            modifier = Modifier.testTag("reserve_ritual_button"),
          ) {
            Text(
              text = stringResource(R.string.action_reserve_ritual),
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .widthIn(max = 760.dp)
      ) {
        // Edge-to-edge hero photography
        Box(modifier = Modifier.fillMaxWidth().height(290.dp)) {
          OasisNetworkImage(
            imageUrl = treatment.imageUrl,
            contentDescription = treatment.title,
            modifier = Modifier.fillMaxSize(),
          )
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors =
                      listOf(
                        Color.Black.copy(alpha = 0.28f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.3f),
                      )
                  )
                )
          )

          Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Surface(
              shape = CircleShape,
              color = WarmCream.copy(alpha = 0.92f),
              shadowElevation = 2.dp,
            ) {
              IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp).testTag("detail_back_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = stringResource(R.string.cd_back),
                  tint = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
            Surface(
              shape = CircleShape,
              color = WarmCream.copy(alpha = 0.92f),
              shadowElevation = 2.dp,
            ) {
              IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(48.dp).testTag("detail_favorite_button"),
              ) {
                Icon(
                  imageVector =
                    if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                  contentDescription = stringResource(R.string.cd_favorite),
                  tint = if (isFavorite) OasisPrimary else MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }

        Column(
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = SoftSageTint,
            ) {
              Text(
                text = treatment.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OasisPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
              )
            }
            GoldBadge(
              text = "${treatment.rating} (${treatment.reviewCount} reviews)",
              showStar = true,
            )
            if (treatment.isVipSignature) {
              GoldBadge(text = "VIP TIER")
            }
          }

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = treatment.title,
              style = MaterialTheme.typography.headlineLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = treatment.subtitle,
              style = MaterialTheme.typography.titleMedium,
              color = OasisPrimary,
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
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "${treatment.durationMinutes} minutes • Includes herbal welcome tea & lounge access",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          HorizontalDivider(color = OasisOutlineVariant.copy(alpha = 0.45f))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "The Ritual",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = treatment.description,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Botanical Essence",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              treatment.botanicalNotes.forEach { note ->
                Surface(
                  shape = RoundedCornerShape(999.dp),
                  color = WarmCream,
                  border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.6f)),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Spa,
                      contentDescription = null,
                      tint = OasisPrimary,
                      modifier = Modifier.size(14.dp),
                    )
                    Text(
                      text = note,
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                  }
                }
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.45f)),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = "Included in This Session",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              treatment.highlights.forEach { highlight ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = OasisPrimary,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = highlight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }

          if (treatmentReviews.isNotEmpty()) {
            Column(
              modifier = Modifier.fillMaxWidth().testTag("treatment_reviews_section"),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "Your Visit Reviews (${treatmentReviews.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              treatmentReviews.forEach { review ->
                SubmittedReviewCard(review = review)
              }
            }
          }
        }
      }
    }
  }
}

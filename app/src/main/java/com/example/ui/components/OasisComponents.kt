package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.SpaTherapist
import com.example.data.model.SpaTreatment
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.OasisPrimaryContainer
import com.example.ui.theme.OasisSecondary
import com.example.ui.theme.RefinedGold
import com.example.ui.theme.SoftGoldBg
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream

@Composable
fun OasisNetworkImage(
  imageUrl: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
) {
  var isFallbackVisible by remember(imageUrl) { mutableStateOf(false) }

  Box(
    modifier =
      modifier.background(
        Brush.linearGradient(
          colors = listOf(WarmCream, SoftSageTint, OasisPrimaryContainer.copy(alpha = 0.45f))
        )
      ),
    contentAlignment = Alignment.Center,
  ) {
    if (isFallbackVisible) {
      Icon(
        imageVector = Icons.Filled.Spa,
        contentDescription = null,
        tint = OasisPrimary.copy(alpha = 0.45f),
        modifier = Modifier.size(42.dp),
      )
    }
    AsyncImage(
      model =
        ImageRequest.Builder(LocalContext.current)
          .data(imageUrl)
          .crossfade(true)
          .build(),
      contentDescription = contentDescription,
      contentScale = ContentScale.Crop,
      onError = { isFallbackVisible = true },
      onSuccess = { isFallbackVisible = false },
      modifier = Modifier.fillMaxSize(),
    )
  }
}

@Composable
fun GoldBadge(
  text: String,
  modifier: Modifier = Modifier,
  showStar: Boolean = false,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(999.dp),
    color = SoftGoldBg,
    border = BorderStroke(0.75.dp, RefinedGold.copy(alpha = 0.65f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      if (showStar) {
        Icon(
          imageVector = Icons.Filled.Star,
          contentDescription = null,
          tint = RefinedGold,
          modifier = Modifier.size(13.dp),
        )
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = OasisSecondary,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
fun OasisSectionHeader(
  title: String,
  subtitle: String? = null,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    if (actionText != null && onActionClick != null) {
      Text(
        text = actionText,
        style = MaterialTheme.typography.labelLarge,
        color = OasisPrimary,
        modifier =
          Modifier.clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onActionClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 8.dp, vertical = 6.dp),
      )
    }
  }
}

@Composable
fun OasisCategoryChips(
  categories: List<String>,
  selectedCategory: String,
  onSelectCategory: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyRow(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = 24.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(categories, key = { it }) { category ->
      val isSelected = category == selectedCategory
      Surface(
        shape = RoundedCornerShape(999.dp),
        color =
          if (isSelected) OasisPrimary else MaterialTheme.colorScheme.surfaceContainerLow,
        border =
          if (isSelected) null
          else BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.8f)),
        modifier =
          Modifier.clip(RoundedCornerShape(999.dp))
            .clickable { onSelectCategory(category) }
            .minimumInteractiveComponentSize()
            .testTag("category_chip_${category.lowercase().replace(" ", "_")}"),
      ) {
        Box(
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = category,
            style = MaterialTheme.typography.labelLarge,
            color =
              if (isSelected) MaterialTheme.colorScheme.onPrimary
              else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
fun OasisTreatmentCard(
  treatment: SpaTreatment,
  isFavorite: Boolean,
  onToggleFavorite: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .testTag("treatment_card_${treatment.id}"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.45f)),
    shadowElevation = 2.dp,
  ) {
    Column {
      Box(modifier = Modifier.fillMaxWidth().height(176.dp)) {
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
                  colors = listOf(Color.Black.copy(alpha = 0.14f), Color.Transparent, Color.Black.copy(alpha = 0.25f))
                )
              )
        )

        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = WarmCream.copy(alpha = 0.94f),
            ) {
              Text(
                text = treatment.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OasisPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
            if (treatment.isVipSignature) {
              GoldBadge(
                text = if (treatment.isPackageBundle) "PACKAGE" else "VIP SIGNATURE",
                showStar = true,
              )
            }
          }

          Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.9f),
          ) {
            IconButton(
              onClick = onToggleFavorite,
              modifier = Modifier.size(48.dp).testTag("fav_btn_${treatment.id}"),
            ) {
              Icon(
                imageVector =
                  if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(R.string.cd_favorite),
                tint = if (isFavorite) OasisPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }
      }

      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = treatment.title,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = treatment.subtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "$${treatment.priceUsd}",
            style = MaterialTheme.typography.headlineSmall,
            color = OasisPrimary,
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Schedule,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(15.dp),
            )
            Text(
              text = "${treatment.durationMinutes} min",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "•",
              style = MaterialTheme.typography.bodySmall,
              color = OasisOutlineVariant,
            )
            Text(
              text = treatment.botanicalNotes.take(2).joinToString(" & "),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          GoldBadge(
            text = "${treatment.rating} (${treatment.reviewCount})",
            showStar = true,
          )
        }
      }
    }
  }
}

@Composable
fun OasisTherapistCard(
  therapist: SpaTherapist,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .width(232.dp)
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .testTag("therapist_card_${therapist.id}"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.45f)),
    shadowElevation = 2.dp,
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OasisNetworkImage(
          imageUrl = therapist.avatarUrl,
          contentDescription = therapist.name,
          modifier = Modifier.size(56.dp).clip(CircleShape),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = therapist.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = therapist.title,
            style = MaterialTheme.typography.bodySmall,
            color = OasisPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = therapist.specialty,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = therapist.bio,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "${therapist.yearsExperience} yrs experience",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GoldBadge(text = "${therapist.rating}", showStar = true)
      }
    }
  }
}

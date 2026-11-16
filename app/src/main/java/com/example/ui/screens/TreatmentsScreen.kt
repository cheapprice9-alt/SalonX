package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SpaCatalog
import com.example.data.model.SpaTreatment
import com.example.data.model.UserProfile
import com.example.ui.components.OasisCategoryChips
import com.example.ui.components.OasisTreatmentCard
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary

@Composable
fun TreatmentsScreen(
  profile: UserProfile?,
  selectedCategory: String,
  searchQuery: String,
  onSelectCategory: (String) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onOpenTreatment: (SpaTreatment) -> Unit,
  onOpenAssistant: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val favorites = remember(profile) { profile?.favoriteTreatmentIds?.toSet() ?: emptySet() }

  val filteredTreatments =
    remember(selectedCategory, searchQuery) {
      SpaCatalog.treatments.filter { item ->
        val matchesCategory =
          selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
        val q = searchQuery.trim()
        val matchesQuery =
          q.isEmpty() ||
            item.title.contains(q, ignoreCase = true) ||
            item.subtitle.contains(q, ignoreCase = true) ||
            item.botanicalNotes.any { it.contains(q, ignoreCase = true) }
        matchesCategory && matchesQuery
      }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      item {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
          Text(
            text = "Treatment Menu",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Botanical facials, restorative bodywork & sanctuary bundles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
              Text(
                text = "Search rituals, notes (e.g. Sage, Basalt)…",
                style = MaterialTheme.typography.bodyMedium,
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.cd_search),
                tint = OasisPrimary,
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                  Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_clear_search),
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedBorderColor = OasisPrimary,
                unfocusedBorderColor = OasisOutlineVariant.copy(alpha = 0.7f),
              ),
            modifier = Modifier.fillMaxWidth().testTag("treatments_search_input"),
          )

          Spacer(modifier = Modifier.height(12.dp))
          Surface(
            modifier =
              Modifier.fillMaxWidth()
                .clickable(onClick = onOpenAssistant)
                .testTag("treatments_ask_assistant_button"),
            shape = RoundedCornerShape(14.dp),
            color = com.example.ui.theme.SoftSageTint,
            border = BorderStroke(0.75.dp, com.example.ui.theme.OasisPrimaryContainer.copy(alpha = 0.6f)),
          ) {
            androidx.compose.foundation.layout.Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = OasisPrimary,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = "Not sure? Ask Smart Booking Assistant",
                style = MaterialTheme.typography.labelLarge,
                color = OasisPrimary,
                modifier = Modifier.weight(1f),
              )
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open Smart Booking Assistant",
                tint = OasisPrimary,
                modifier = Modifier.size(16.dp),
              )
            }
          }
        }
      }

      item {
        OasisCategoryChips(
          categories = SpaCatalog.categories,
          selectedCategory = selectedCategory,
          onSelectCategory = onSelectCategory,
        )
      }

      if (filteredTreatments.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
          ) {
            Column(
              modifier = Modifier.padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = null,
                tint = OasisPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(40.dp),
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No matching rituals found",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Try clearing your search or selecting another sanctuary category.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
              )
            }
          }
        }
      } else {
        items(filteredTreatments, key = { it.id }) { treatment ->
          OasisTreatmentCard(
            treatment = treatment,
            isFavorite = treatment.id in favorites,
            onToggleFavorite = { onToggleFavorite(treatment.id) },
            onClick = { onOpenTreatment(treatment) },
            modifier = Modifier.padding(horizontal = 24.dp),
          )
        }
      }
    }
  }
}

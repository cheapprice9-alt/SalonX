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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SpaCatalog
import com.example.data.model.SpaTreatment
import com.example.data.model.UserProfile
import com.example.ui.components.GoldBadge
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.RefinedGold
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
  profile: UserProfile?,
  onSavePreferences: (displayName: String, preferredPressure: String, aromatherapyNote: String) -> Unit,
  onOpenTreatment: (SpaTreatment) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
  onOpenBusinessInsights: () -> Unit = {},
  onOpenReferFriend: () -> Unit = {},
) {
  var displayName by remember(profile?.displayName) {
    mutableStateOf(profile?.displayName ?: "Sanctuary Guest")
  }
  var preferredPressure by remember(profile?.preferredPressure) {
    mutableStateOf(profile?.preferredPressure ?: "Medium")
  }
  var aromatherapyNote by remember(profile?.aromatherapyNote) {
    mutableStateOf(profile?.aromatherapyNote ?: "Eucalyptus & Bergamot")
  }

  val favoriteTreatments = remember(profile?.favoriteTreatmentIds) {
    val ids = profile?.favoriteTreatmentIds?.toSet() ?: emptySet()
    SpaCatalog.treatments.filter { it.id in ids }
  }

  val points = (profile?.wellnessPoints ?: 250L).toInt()
  val isGold = profile?.membershipTier == "GOLD_SANCTUARY" || points >= 500
  val progress = (points / 500f).coerceIn(0f, 1f)

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .verticalScroll(rememberScrollState())
          .widthIn(max = 760.dp)
          .padding(horizontal = 24.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      Text(
        text = "SalonX Profile",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )

      // Member Sanctuary Card
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 3.dp,
      ) {
        Column(
          modifier =
            Modifier.background(
                Brush.linearGradient(colors = listOf(WarmCream, SoftSageTint.copy(alpha = 0.65f)))
              )
              .padding(22.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(OasisPrimary),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Filled.Spa,
                  contentDescription = null,
                  tint = OasisOnPrimary,
                )
              }
              Column {
                Text(
                  text = profile?.displayName ?: "Sanctuary Guest",
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                if (!profile?.email.isNullOrBlank()) {
                  Text(
                    text = profile?.email.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
            GoldBadge(
              text = if (isGold) "GOLD SANCTUARY" else "SAGE MEMBER",
              showStar = true,
            )
          }

          HorizontalDivider(color = OasisOutlineVariant.copy(alpha = 0.45f))

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "Wellness Points",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = if (isGold) "$points pts • Elite Perks Unlocked" else "$points / 500 pts to Gold",
                style = MaterialTheme.typography.labelMedium,
                color = OasisPrimary,
              )
            }
            LinearProgressIndicator(
              progress = { progress },
              color = RefinedGold,
              trackColor = MaterialTheme.colorScheme.surfaceContainerLowest,
              modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp)),
            )
          }
        }
      }

      // Salon Owner Business Insights Card
      val isPartner = profile?.aromatherapyNote?.startsWith("Salon Partner:") == true
      Surface(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenBusinessInsights)
            .minimumInteractiveComponentSize()
            .testTag("open_business_insights_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.85.dp, RefinedGold.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
      ) {
        Row(
          modifier =
            Modifier.background(
                Brush.horizontalGradient(
                  colors = listOf(SoftSageTint.copy(alpha = 0.55f), WarmCream)
                )
              )
              .padding(horizontal = 18.dp, vertical = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(OasisPrimary),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.Insights,
              contentDescription = null,
              tint = OasisOnPrimary,
              modifier = Modifier.size(22.dp),
            )
          }
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = if (isPartner) "Salon Partner • Business Insights" else "Salon Owner • Business Insights",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Weekly revenue trends, booking volume & specialist metrics",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open Business Insights",
            tint = OasisPrimary,
          )
        }
      }

      // Refer a Friend Card
      Surface(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenReferFriend)
            .minimumInteractiveComponentSize()
            .testTag("profile_refer_friend_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.85.dp, OasisPrimary.copy(alpha = 0.4f)),
        shadowElevation = 2.dp,
      ) {
        Row(
          modifier =
            Modifier.background(
                Brush.horizontalGradient(
                  colors = listOf(com.example.ui.theme.SoftGoldBg.copy(alpha = 0.5f), WarmCream)
                )
              )
              .padding(horizontal = 18.dp, vertical = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(OasisPrimary),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.CardGiftcard,
              contentDescription = null,
              tint = OasisOnPrimary,
              modifier = Modifier.size(22.dp),
            )
          }
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = "Refer a Friend • +150 pts",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            val codeHint =
              profile?.referralCode?.takeIf { it.isNotBlank() }?.let { "Share code $it" }
                ?: "Invite friends & earn loyalty points"
            Text(
              text = codeHint,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open Refer a Friend",
            tint = OasisPrimary,
          )
        }
      }

      // Sensory Preferences Editor
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Text(
            text = "Sensory & Suite Preferences",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )

          OutlinedTextField(
            value = displayName,
            onValueChange = { if (it.length <= 80) displayName = it },
            label = { Text("Preferred Greeting Name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OasisPrimary,
                unfocusedBorderColor = OasisOutlineVariant.copy(alpha = 0.7f),
              ),
            modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Preferred Massage Pressure",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              SpaCatalog.pressureLevels.forEach { level ->
                val selected = level == preferredPressure
                Surface(
                  modifier =
                    Modifier.clip(RoundedCornerShape(999.dp))
                      .clickable { preferredPressure = level }
                      .minimumInteractiveComponentSize()
                      .testTag("pressure_${level.lowercase()}"),
                  shape = RoundedCornerShape(999.dp),
                  color =
                    if (selected) OasisPrimary else MaterialTheme.colorScheme.surfaceContainerLow,
                  border =
                    if (selected) null
                    else BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.7f)),
                ) {
                  Text(
                    text = level,
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

          OutlinedTextField(
            value = aromatherapyNote,
            onValueChange = { if (it.length <= 200) aromatherapyNote = it },
            label = { Text("Aromatherapy & Herbal Tea Note") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OasisPrimary,
                unfocusedBorderColor = OasisOutlineVariant.copy(alpha = 0.7f),
              ),
            modifier = Modifier.fillMaxWidth().testTag("profile_aroma_input"),
          )

          Button(
            onClick = { onSavePreferences(displayName, preferredPressure, aromatherapyNote) },
            shape = RoundedCornerShape(16.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = OasisPrimary,
                contentColor = OasisOnPrimary,
              ),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier.fillMaxWidth().testTag("save_preferences_button"),
          ) {
            Text(
              text = stringResource(R.string.action_save_preferences),
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }

      // Saved Favorite Rituals
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Saved Rituals (${favoriteTreatments.size})",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        if (favoriteTreatments.isEmpty()) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
          ) {
            Text(
              text = "Tap the heart icon on any treatment to curate your personal sanctuary wishlist.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(18.dp),
            )
          }
        } else {
          favoriteTreatments.forEach { item ->
            Surface(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .clickable { onOpenTreatment(item) },
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surfaceContainerLowest,
              border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.45f)),
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Favorite,
                  contentDescription = null,
                  tint = OasisPrimary,
                  modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "${item.durationMinutes} min • $${item.priceUsd}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }

      // Sign Out button
      OutlinedButton(
        onClick = onSignOut,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OasisOutlineVariant),
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("sign_out_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Logout,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
          text = stringResource(R.string.action_sign_out),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

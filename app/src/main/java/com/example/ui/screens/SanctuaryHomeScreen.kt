package com.example.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SpaBooking
import com.example.data.model.SpaCatalog
import com.example.data.model.SpaTreatment
import com.example.data.model.UserProfile
import com.example.ui.components.GoldBadge
import com.example.ui.components.LoyaltyPointsTrackerCard
import com.example.ui.components.OasisCategoryChips
import com.example.ui.components.OasisNetworkImage
import com.example.ui.components.OasisSectionHeader
import com.example.ui.components.OasisTherapistCard
import com.example.ui.components.OasisTreatmentCard
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.OasisPrimaryContainer
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream
import java.util.Calendar

@Composable
fun SanctuaryHomeScreen(
  profile: UserProfile?,
  bookings: List<SpaBooking>,
  selectedCategory: String,
  onSelectCategory: (String) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onOpenTreatment: (SpaTreatment) -> Unit,
  onExploreAllTreatments: () -> Unit,
  onOpenBookings: () -> Unit,
  onOpenAssistant: (String?) -> Unit = {},
  onOpenReferFriend: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val firstName =
    remember(profile?.displayName) {
      profile?.displayName?.trim()?.substringBefore(" ")?.takeIf { it.isNotEmpty() } ?: "Guest"
    }
  val greeting = remember {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    when {
      hour < 12 -> "Good morning"
      hour < 17 -> "Good afternoon"
      else -> "Good evening"
    }
  }

  val nextUpcoming = remember(bookings) { bookings.firstOrNull { it.status == "confirmed" } }
  val favorites = remember(profile) { profile?.favoriteTreatmentIds?.toSet() ?: emptySet() }
  val featuredPackage = remember {
    SpaCatalog.treatments.first { it.id == "oasis_sanctuary_half_day" }
  }
  val curatedTreatments =
    remember(selectedCategory) {
      if (selectedCategory == "All") {
        SpaCatalog.treatments.take(4)
      } else {
        SpaCatalog.treatments.filter { it.category == selectedCategory }
      }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      // Header Greeting & Member Tier Pill
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "SALONX",
              style = MaterialTheme.typography.labelMedium,
              color = OasisPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$greeting, $firstName",
              style = MaterialTheme.typography.headlineLarge,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          val tierLabel =
            if (profile?.membershipTier == "GOLD_SANCTUARY") "GOLD • ${profile.wellnessPoints} pts"
            else "SAGE • ${profile?.wellnessPoints ?: 250} pts"
          GoldBadge(text = tierLabel, showStar = true)
        }
      }

      // Upcoming Reservation Banner (if exists)
      if (nextUpcoming != null) {
        item {
          Surface(
            modifier =
              Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenBookings)
                .testTag("upcoming_booking_banner"),
            shape = RoundedCornerShape(16.dp),
            color = SoftSageTint,
            border = BorderStroke(0.75.dp, OasisPrimaryContainer.copy(alpha = 0.7f)),
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(OasisPrimary),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Filled.CalendarToday,
                  contentDescription = null,
                  tint = OasisOnPrimary,
                  modifier = Modifier.size(20.dp),
                )
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "NEXT SANCTUARY VISIT",
                  style = MaterialTheme.typography.labelSmall,
                  color = OasisPrimary,
                )
                Text(
                  text = nextUpcoming.treatmentTitle,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${nextUpcoming.dateLabel} • ${nextUpcoming.timeSlot} with ${nextUpcoming.therapistName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = OasisPrimary,
              )
            }
          }
        }
      }

      // Loyalty Points Tracker Card (points earned from completed bookings)
      item {
        LoyaltyPointsTrackerCard(
          profile = profile,
          bookings = bookings,
          onViewHistory = onOpenBookings,
          modifier = Modifier.padding(horizontal = 24.dp),
        )
      }

      // Smart Booking Assistant Card
      item {
        Surface(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 24.dp)
              .clip(RoundedCornerShape(18.dp))
              .clickable { onOpenAssistant(null) }
              .testTag("home_smart_assistant_card"),
          shape = RoundedCornerShape(18.dp),
          color = WarmCream,
          border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.6f)),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Box(
              modifier = Modifier.size(44.dp).clip(CircleShape).background(SoftSageTint),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = OasisPrimary,
                modifier = Modifier.size(22.dp),
              )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "SMART BOOKING ASSISTANT",
                style = MaterialTheme.typography.labelSmall,
                color = OasisPrimary,
              )
              Text(
                text = "Find a ritual by mood or skin type",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Chat with our Gemini concierge for tailored picks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Open Smart Booking Assistant",
              tint = OasisPrimary,
            )
          }
        }
      }

      // Refer a Friend Card
      item {
        Surface(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 24.dp)
              .clip(RoundedCornerShape(18.dp))
              .clickable(onClick = onOpenReferFriend)
              .testTag("home_refer_friend_card"),
          shape = RoundedCornerShape(18.dp),
          color = com.example.ui.theme.SoftGoldBg.copy(alpha = 0.55f),
          border = BorderStroke(0.75.dp, com.example.ui.theme.RefinedGold.copy(alpha = 0.45f)),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
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
                text = "REFER A FRIEND • +150 PTS",
                style = MaterialTheme.typography.labelSmall,
                color = com.example.ui.theme.OasisSecondary,
              )
              Text(
                text = "Share your invitation code",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              val codePreview = profile?.referralCode?.takeIf { it.isNotBlank() } ?: "SALONX code ready"
              Text(
                text = "Your code: $codePreview",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Open Refer a Friend",
              tint = OasisPrimary,
            )
          }
        }
      }

      // Asymmetric Signature Sanctuary Hero Feature
      item {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
          Surface(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { onOpenTreatment(featuredPackage) }
                .testTag("hero_package_card"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.45f)),
            shadowElevation = 3.dp,
          ) {
            Column {
              Box(modifier = Modifier.fillMaxWidth().height(210.dp)) {
                OasisNetworkImage(
                  imageUrl = featuredPackage.imageUrl,
                  contentDescription = featuredPackage.title,
                  modifier = Modifier.fillMaxSize(),
                )
                Box(
                  modifier =
                    Modifier.fillMaxSize()
                      .background(
                        Brush.verticalGradient(
                          colors =
                            listOf(
                              Color.Transparent,
                              Color(0xFF1B1C1C).copy(alpha = 0.55f),
                            )
                        )
                      )
                )
                GoldBadge(
                  text = "SIGNATURE PACKAGE",
                  showStar = true,
                  modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                )
                Column(
                  modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
                ) {
                  Text(
                    text = featuredPackage.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                  )
                  Text(
                    text = "${featuredPackage.durationMinutes} min immersion • $${featuredPackage.priceUsd}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth().background(WarmCream.copy(alpha = 0.6f)).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = featuredPackage.subtitle,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.weight(1f).padding(end = 12.dp),
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
                Button(
                  onClick = { onOpenTreatment(featuredPackage) },
                  shape = RoundedCornerShape(16.dp),
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = OasisPrimary,
                      contentColor = OasisOnPrimary,
                    ),
                  contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                  modifier = Modifier.testTag("hero_explore_button"),
                ) {
                  Text(
                    text = stringResource(R.string.action_explore_ritual),
                    style = MaterialTheme.typography.labelLarge,
                  )
                }
              }
            }
          }

          // Floating subtle botanical seal breaking the top-right edge
          Surface(
            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-14).dp, y = 14.dp).size(42.dp),
            shape = CircleShape,
            color = WarmCream.copy(alpha = 0.92f),
            shadowElevation = 2.dp,
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = null,
                tint = OasisPrimary,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }
      }

      // Curated Rituals Section + Category Filter Chips
      item {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          OasisSectionHeader(
            title = "Curated Rituals",
            subtitle = "Unhurried holistic treatments tailored to your rhythm",
            actionText = "View All",
            onActionClick = onExploreAllTreatments,
            modifier = Modifier.padding(horizontal = 24.dp),
          )
          OasisCategoryChips(
            categories = SpaCatalog.categories,
            selectedCategory = selectedCategory,
            onSelectCategory = onSelectCategory,
          )
        }
      }

      items(curatedTreatments, key = { it.id }) { treatment ->
        OasisTreatmentCard(
          treatment = treatment,
          isFavorite = treatment.id in favorites,
          onToggleFavorite = { onToggleFavorite(treatment.id) },
          onClick = { onOpenTreatment(treatment) },
          modifier = Modifier.padding(horizontal = 24.dp),
        )
      }

      // Master Therapists Carousel
      item {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          OasisSectionHeader(
            title = "Master Practitioners",
            subtitle = "Dedicated artisans in holistic aesthetics & bodywork",
            modifier = Modifier.padding(horizontal = 24.dp),
          )
          LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            items(SpaCatalog.therapists, key = { it.id }) { therapist ->
              OasisTherapistCard(
                therapist = therapist,
                onClick = {
                  val matching =
                    SpaCatalog.treatments.firstOrNull {
                      it.category in therapist.availableCategories
                    } ?: SpaCatalog.treatments.first()
                  onOpenTreatment(matching)
                },
              )
            }
          }
        }
      }
    }
  }
}

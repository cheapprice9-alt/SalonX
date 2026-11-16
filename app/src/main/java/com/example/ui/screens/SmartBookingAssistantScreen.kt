package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.AssistantChatMessage
import com.example.data.ai.AssistantSender
import com.example.data.ai.RecommendedRitual
import com.example.data.model.UserProfile
import com.example.ui.components.GoldBadge
import com.example.ui.components.OasisNetworkImage
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.OasisPrimaryContainer
import com.example.ui.theme.OasisSecondary
import com.example.ui.theme.RefinedGold
import com.example.ui.theme.SoftGoldBg
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream
import com.example.ui.viewmodel.AssistantUiState

private data class MoodSkinPreset(
  val label: String,
  val prompt: String,
)

private val MoodSkinPresets =
  listOf(
    MoodSkinPreset(
      label = "Stressed & tense",
      prompt = "I'm feeling stressed with tight shoulders and need something deeply grounding.",
    ),
    MoodSkinPreset(
      label = "Dry, dull skin",
      prompt = "My skin feels dry and dull this week—which facial or ritual restores hydration and glow?",
    ),
    MoodSkinPreset(
      label = "Sensitive skin",
      prompt = "I have sensitive, redness-prone skin. What calming treatment and specialist do you suggest?",
    ),
    MoodSkinPreset(
      label = "Evening unwind",
      prompt = "I want an unhurried evening ritual to sleep better and completely unplug.",
    ),
    MoodSkinPreset(
      label = "Special occasion",
      prompt = "I have a milestone celebration tomorrow and want radiant, lifted skin and relaxation.",
    ),
  )

@Composable
fun SmartBookingAssistantScreen(
  messages: List<AssistantChatMessage>,
  uiState: AssistantUiState,
  profile: UserProfile?,
  onSendMessage: (String) -> Unit,
  onRetry: () -> Unit,
  onResetConversation: () -> Unit,
  onOpenTreatment: (String) -> Unit,
  onBookTreatment: (String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var draft by rememberSaveable { mutableStateOf("") }
  val listState = rememberLazyListState()
  val isLoading = uiState is AssistantUiState.Loading

  LaunchedEffect(messages.size, uiState) {
    val totalItems = messages.size + if (messages.isEmpty()) 1 else 0
    if (totalItems > 0) {
      listState.animateScrollToItem((totalItems - 1).coerceAtLeast(0))
    }
  }

  Column(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Editorial Concierge Top Bar
    Surface(
      color = MaterialTheme.colorScheme.surfaceContainerLowest,
      tonalElevation = 1.dp,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .widthIn(max = 760.dp)
              .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).testTag("assistant_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.cd_back),
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }

          Row(
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier.size(38.dp).clip(CircleShape).background(SoftSageTint),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = OasisPrimary,
                modifier = Modifier.size(20.dp),
              )
            }
            Column {
              Text(
                text = stringResource(R.string.assistant_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = stringResource(R.string.assistant_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          if (messages.isNotEmpty()) {
            TextButton(
              onClick = onResetConversation,
              enabled = !isLoading,
              modifier = Modifier.testTag("assistant_reset_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.assistant_new_chat),
                tint = OasisPrimary,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Reset",
                style = MaterialTheme.typography.labelMedium,
                color = OasisPrimary,
              )
            }
          }
        }

        // Quick Mood & Skin Type Chip Ribbon
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .widthIn(max = 760.dp)
              .horizontalScroll(rememberScrollState())
              .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          MoodSkinPresets.forEach { preset ->
            Surface(
              modifier =
                Modifier.clip(RoundedCornerShape(999.dp))
                  .clickable(enabled = !isLoading) { onSendMessage(preset.prompt) }
                  .minimumInteractiveComponentSize()
                  .testTag("assistant_preset_${preset.label.lowercase().replace(" ", "_")}"),
              shape = RoundedCornerShape(999.dp),
              color = WarmCream,
              border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.65f)),
            ) {
              Text(
                text = preset.label,
                style = MaterialTheme.typography.labelMedium,
                color = OasisSecondary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              )
            }
          }
        }
        HorizontalDivider(color = OasisOutlineVariant.copy(alpha = 0.3f))
      }
    }

    // Chat Stream
    LazyColumn(
      state = listState,
      modifier = Modifier.weight(1f).fillMaxWidth().widthIn(max = 760.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      if (profile != null) {
        item(key = "profile_context") {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = SoftGoldBg.copy(alpha = 0.5f),
            border = BorderStroke(0.75.dp, RefinedGold.copy(alpha = 0.35f)),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = null,
                tint = RefinedGold,
                modifier = Modifier.size(16.dp),
              )
              val aroma = profile.aromatherapyNote?.takeIf { it.isNotBlank() } ?: "Eucalyptus & Bergamot"
              Text(
                text = "Personalizing for ${profile.preferredPressure} pressure • $aroma",
                style = MaterialTheme.typography.labelMedium,
                color = OasisSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }

      if (messages.isEmpty()) {
        item(key = "welcome_card") {
          WelcomeConciergeCard(
            onSelectPrompt = { prompt ->
              if (!isLoading) onSendMessage(prompt)
            }
          )
        }
      }

      items(messages, key = { it.id }) { msg ->
        val isLatestAssistant =
          msg.sender == AssistantSender.ASSISTANT && messages.lastOrNull()?.id == msg.id
        if (msg.sender == AssistantSender.USER) {
          UserMessageBubble(message = msg)
        } else {
          AssistantMessageBubble(
            message = msg,
            showFollowUps = isLatestAssistant && !isLoading,
            onFollowUpClick = { followUp -> onSendMessage(followUp) },
            onOpenTreatment = onOpenTreatment,
            onBookTreatment = onBookTreatment,
          )
        }
      }

      if (isLoading) {
        item(key = "loading_indicator") {
          AssistantThinkingBubble()
        }
      }

      if (uiState is AssistantUiState.Error) {
        item(key = "error_banner") {
          AssistantErrorCard(
            message = uiState.message,
            onRetry = onRetry,
          )
        }
      }
    }

    // Bottom Message Composer
    Surface(
      color = MaterialTheme.colorScheme.surfaceContainerLowest,
      tonalElevation = 4.dp,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        HorizontalDivider(color = OasisOutlineVariant.copy(alpha = 0.3f))
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .widthIn(max = 760.dp)
              .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = draft,
            onValueChange = { if (it.length <= 400) draft = it },
            enabled = !isLoading,
            placeholder = {
              Text(
                text = stringResource(R.string.assistant_input_hint),
                style = MaterialTheme.typography.bodyMedium,
              )
            },
            maxLines = 4,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions =
              KeyboardActions(
                onSend = {
                  val clean = draft.trim()
                  if (clean.isNotEmpty() && !isLoading) {
                    draft = ""
                    onSendMessage(clean)
                  }
                }
              ),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OasisPrimary,
                unfocusedBorderColor = OasisOutlineVariant.copy(alpha = 0.7f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
              ),
            modifier = Modifier.weight(1f).testTag("assistant_input_field"),
          )

          Surface(
            modifier =
              Modifier.size(52.dp)
                .clip(CircleShape)
                .clickable(enabled = !isLoading && draft.isNotBlank()) {
                  val clean = draft.trim()
                  if (clean.isNotEmpty()) {
                    draft = ""
                    onSendMessage(clean)
                  }
                }
                .testTag("assistant_send_button"),
            shape = CircleShape,
            color =
              if (!isLoading && draft.isNotBlank()) OasisPrimary
              else OasisOutlineVariant.copy(alpha = 0.4f),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.assistant_send),
                tint =
                  if (!isLoading && draft.isNotBlank()) OasisOnPrimary
                  else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun WelcomeConciergeCard(
  onSelectPrompt: (String) -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("assistant_welcome_card"),
    shape = RoundedCornerShape(22.dp),
    color = WarmCream,
    border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.55f)),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier.size(40.dp).clip(CircleShape).background(SoftSageTint),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = OasisPrimary,
            modifier = Modifier.size(20.dp),
          )
        }
        Column {
          Text(
            text = "BOTANICAL AI CONCIERGE",
            style = MaterialTheme.typography.labelSmall,
            color = OasisPrimary,
          )
          Text(
            text = "How are your skin & spirit feeling today?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      Text(
        text =
          "Share your mood, tension points, or skin type (dry, sensitive, congested, or needing lift) and I'll curate matching SalonX rituals, specialists, and enhancements you can book in one tap.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      val starterQuestions =
        listOf(
          "I have dry, sensitive skin and feel overwhelmed after travel.",
          "Suggest a 90-minute ritual to melt upper-back stress and calm my mind.",
          "What gives an instant glass-skin glow before an evening event?",
        )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        starterQuestions.forEachIndexed { idx, prompt ->
          Surface(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSelectPrompt(prompt) }
                .testTag("assistant_starter_$idx"),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(0.75.dp, OasisPrimaryContainer.copy(alpha = 0.65f)),
          ) {
            Text(
              text = "\"$prompt\"",
              style = MaterialTheme.typography.bodyMedium,
              color = OasisPrimary,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun UserMessageBubble(message: AssistantChatMessage) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End,
  ) {
    Surface(
      modifier = Modifier.widthIn(max = 320.dp).testTag("assistant_user_bubble_${message.id}"),
      shape = RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp),
      color = OasisPrimary,
    ) {
      Text(
        text = message.text,
        style = MaterialTheme.typography.bodyMedium,
        color = OasisOnPrimary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssistantMessageBubble(
  message: AssistantChatMessage,
  showFollowUps: Boolean,
  onFollowUpClick: (String) -> Unit,
  onOpenTreatment: (String) -> Unit,
  onBookTreatment: (String) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth().testTag("assistant_ai_bubble_${message.id}"),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(6.dp, 20.dp, 20.dp, 20.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLowest,
      border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
      ) {
        Box(
          modifier = Modifier.size(28.dp).clip(CircleShape).background(SoftSageTint),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = OasisPrimary,
            modifier = Modifier.size(15.dp),
          )
        }
        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f),
        )
      }
    }

    message.recommendations.forEach { rec ->
      RecommendedRitualCard(
        recommendation = rec,
        onOpenTreatment = { onOpenTreatment(rec.treatment.id) },
        onBookTreatment = { onBookTreatment(rec.treatment.id) },
      )
    }

    if (showFollowUps && message.followUpPrompts.isNotEmpty()) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        message.followUpPrompts.forEach { prompt ->
          Surface(
            modifier =
              Modifier.clip(RoundedCornerShape(999.dp))
                .clickable { onFollowUpClick(prompt) }
                .minimumInteractiveComponentSize(),
            shape = RoundedCornerShape(999.dp),
            color = SoftSageTint,
            border = BorderStroke(0.75.dp, OasisPrimaryContainer.copy(alpha = 0.65f)),
          ) {
            Text(
              text = prompt,
              style = MaterialTheme.typography.labelMedium,
              color = OasisPrimary,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecommendedRitualCard(
  recommendation: RecommendedRitual,
  onOpenTreatment: () -> Unit,
  onBookTreatment: () -> Unit,
) {
  val treatment = recommendation.treatment
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("assistant_rec_card_${treatment.id}"),
    shape = RoundedCornerShape(18.dp),
    color = WarmCream,
    border = BorderStroke(0.75.dp, RefinedGold.copy(alpha = 0.45f)),
    shadowElevation = 1.dp,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OasisNetworkImage(
          imageUrl = treatment.imageUrl,
          contentDescription = treatment.title,
          modifier = Modifier.size(68.dp).clip(RoundedCornerShape(14.dp)),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = treatment.category.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = OasisPrimary,
            )
            GoldBadge(text = "$${treatment.priceUsd}", showStar = false)
          }
          Text(
            text = treatment.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Schedule,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = "${treatment.durationMinutes} min • ★ ${treatment.rating}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Text(
        text = recommendation.matchReason,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
      )

      if (recommendation.suggestedTherapist != null || recommendation.suggestedAddOn != null) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          recommendation.suggestedTherapist?.let { therapist ->
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = SoftSageTint,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Person,
                  contentDescription = null,
                  tint = OasisPrimary,
                  modifier = Modifier.size(13.dp),
                )
                Text(
                  text = therapist.name,
                  style = MaterialTheme.typography.labelSmall,
                  color = OasisPrimary,
                )
              }
            }
          }
          recommendation.suggestedAddOn?.let { addOn ->
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = SoftGoldBg,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.AddCircleOutline,
                  contentDescription = null,
                  tint = OasisSecondary,
                  modifier = Modifier.size(13.dp),
                )
                Text(
                  text = "+ ${addOn.name} ($${addOn.priceUsd})",
                  style = MaterialTheme.typography.labelSmall,
                  color = OasisSecondary,
                )
              }
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedButton(
          onClick = onOpenTreatment,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, OasisPrimary),
          modifier = Modifier.weight(1f).height(48.dp).testTag("assistant_detail_${treatment.id}"),
        ) {
          Text(
            text = "Details",
            style = MaterialTheme.typography.labelLarge,
            color = OasisPrimary,
          )
        }
        Button(
          onClick = onBookTreatment,
          shape = RoundedCornerShape(14.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = OasisPrimary,
              contentColor = OasisOnPrimary,
            ),
          modifier = Modifier.weight(1.3f).height(48.dp).testTag("assistant_book_${treatment.id}"),
        ) {
          Text(
            text = "Book Ritual",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

@Composable
private fun AssistantThinkingBubble() {
  Surface(
    modifier = Modifier.testTag("assistant_loading_bubble"),
    shape = RoundedCornerShape(18.dp),
    color = SoftSageTint,
    border = BorderStroke(0.75.dp, OasisPrimaryContainer.copy(alpha = 0.65f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(18.dp),
        color = OasisPrimary,
        strokeWidth = 2.dp,
      )
      Text(
        text = "Curating personalized rituals…",
        style = MaterialTheme.typography.bodyMedium,
        color = OasisPrimary,
      )
    }
  }
}

@Composable
private fun AssistantErrorCard(
  message: String,
  onRetry: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("assistant_error_card"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.ErrorOutline,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(22.dp),
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = "Concierge unavailable",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
          text = message,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onErrorContainer,
        )
      }
      OutlinedButton(
        onClick = onRetry,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        modifier = Modifier.testTag("assistant_retry_button"),
      ) {
        Text(
          text = "Retry",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

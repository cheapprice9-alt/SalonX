package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ReferralCodeRecord
import com.example.data.model.ReferralRedemption
import com.example.data.model.UserProfile
import com.example.data.repository.SereneOasisRepository
import com.example.ui.components.GoldBadge
import com.example.ui.theme.OasisOnPrimary
import com.example.ui.theme.OasisOutlineVariant
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.OasisSecondary
import com.example.ui.theme.RefinedGold
import com.example.ui.theme.SoftGoldBg
import com.example.ui.theme.SoftSageTint
import com.example.ui.theme.WarmCream
import com.example.ui.viewmodel.UiState

@Composable
fun ReferFriendScreen(
  profile: UserProfile?,
  referralCodeRecord: ReferralCodeRecord?,
  referralHistoryState: UiState<List<ReferralRedemption>>,
  isRedeeming: Boolean,
  onRedeemCode: (String, onSuccess: () -> Unit) -> Unit,
  onCodeCopied: (String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  var inputCode by rememberSaveable { mutableStateOf("") }
  var localError by rememberSaveable { mutableStateOf<String?>(null) }

  val myCode =
    referralCodeRecord?.code?.takeIf { it.isNotBlank() }
      ?: profile?.referralCode?.takeIf { it.isNotBlank() }
      ?: "Generating…"
  val hasValidCode = myCode.startsWith("SALONX-")

  val successfulCount = referralCodeRecord?.successfulReferrals ?: 0L
  val pointsEarned =
    maxOf(
      referralCodeRecord?.pointsEarned ?: 0L,
      successfulCount * SereneOasisRepository.REFERRER_REWARD_POINTS,
    )
  val alreadyRedeemedCode = profile?.redeemedReferralCode?.takeIf { it.isNotBlank() }

  Column(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Top App Bar
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .statusBarsPadding()
          .widthIn(max = 760.dp)
          .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.size(48.dp).testTag("refer_back_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(R.string.cd_back),
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Refer a Friend",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Earn ${SereneOasisRepository.REFERRER_REWARD_POINTS} pts per invited friend",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      GoldBadge(text = "${profile?.wellnessPoints ?: 250} PTS", showStar = true)
    }

    LazyColumn(
      modifier =
        Modifier.weight(1f)
          .fillMaxWidth()
          .widthIn(max = 760.dp)
          .navigationBarsPadding()
          .testTag("refer_lazy_column"),
      contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      // Hero Invite Code Card
      item(key = "hero_code_card") {
        Surface(
          modifier = Modifier.fillMaxWidth().testTag("refer_code_hero_card"),
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLowest,
          border = BorderStroke(0.9.dp, RefinedGold.copy(alpha = 0.55f)),
          shadowElevation = 2.dp,
        ) {
          Column(
            modifier =
              Modifier.background(
                  Brush.linearGradient(
                    colors = listOf(WarmCream, SoftGoldBg.copy(alpha = 0.5f), SoftSageTint.copy(alpha = 0.4f))
                  )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(OasisPrimary),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Filled.CardGiftcard,
                  contentDescription = null,
                  tint = OasisOnPrimary,
                  modifier = Modifier.size(22.dp),
                )
              }
              Column {
                Text(
                  text = "SHARE THE SANCTUARY",
                  style = MaterialTheme.typography.labelSmall,
                  color = OasisPrimary,
                )
                Text(
                  text = "Give +100 pts, Earn +150 pts",
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }

            Text(
              text =
                "Share your personal SalonX code with friends. When a new member redeems it, they receive ${SereneOasisRepository.REFEREE_WELCOME_POINTS} welcome bonus points and you earn ${SereneOasisRepository.REFERRER_REWARD_POINTS} loyalty points.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Code Display Box
            Surface(
              modifier = Modifier.fillMaxWidth().testTag("refer_code_display"),
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surfaceContainerLowest,
              border = BorderStroke(1.dp, OasisPrimary.copy(alpha = 0.45f)),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = "YOUR REFERRAL CODE",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = myCode,
                  style =
                    MaterialTheme.typography.headlineSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 2.sp,
                    ),
                  color = OasisPrimary,
                  textAlign = TextAlign.Center,
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = {
                  if (hasValidCode) {
                    clipboardManager.setText(AnnotatedString(myCode))
                    onCodeCopied(myCode)
                  }
                },
                enabled = hasValidCode,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, OasisPrimary),
                modifier = Modifier.weight(1f).height(48.dp).testTag("refer_copy_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.ContentCopy,
                  contentDescription = null,
                  tint = OasisPrimary,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Copy Code",
                  style = MaterialTheme.typography.labelLarge,
                  color = OasisPrimary,
                )
              }

              Button(
                onClick = {
                  if (hasValidCode) {
                    val shareText =
                      "Join me at SalonX for botanical spa & salon rituals! Use my referral code $myCode when you join to claim +${SereneOasisRepository.REFEREE_WELCOME_POINTS} bonus wellness points."
                    val sendIntent =
                      Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Your SalonX Invitation ($myCode)")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                      }
                    context.startActivity(Intent.createChooser(sendIntent, "Share SalonX Referral Code"))
                  }
                },
                enabled = hasValidCode,
                shape = RoundedCornerShape(14.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = OasisPrimary,
                    contentColor = OasisOnPrimary,
                  ),
                modifier = Modifier.weight(1f).height(48.dp).testTag("refer_share_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Share,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Share Invite",
                  style = MaterialTheme.typography.labelLarge,
                )
              }
            }
          }
        }
      }

      // Stats Summary Row
      item(key = "stats_row") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Surface(
            modifier = Modifier.weight(1f).testTag("refer_stat_count"),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.5f)),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.GroupAdd,
                  contentDescription = null,
                  tint = OasisPrimary,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "Friends Joined",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = successfulCount.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }

          Surface(
            modifier = Modifier.weight(1f).testTag("refer_stat_points"),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(0.75.dp, RefinedGold.copy(alpha = 0.45f)),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Stars,
                  contentDescription = null,
                  tint = RefinedGold,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "Points Earned",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = "+$pointsEarned pts",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OasisSecondary,
              )
            }
          }
        }
      }

      // Redeem a Friend's Code Section
      item(key = "redeem_section") {
        Surface(
          modifier = Modifier.fillMaxWidth().testTag("refer_redeem_card"),
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLowest,
          border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.55f)),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = "Have a Friend's Referral Code?",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )

            if (alreadyRedeemedCode != null) {
              Surface(
                modifier = Modifier.fillMaxWidth().testTag("refer_already_redeemed_badge"),
                shape = RoundedCornerShape(14.dp),
                color = SoftSageTint,
                border = BorderStroke(0.75.dp, OasisPrimary.copy(alpha = 0.3f)),
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = OasisPrimary,
                  )
                  Column {
                    Text(
                      text = "Referral Bonus Claimed ($alreadyRedeemedCode)",
                      style = MaterialTheme.typography.labelLarge,
                      color = OasisPrimary,
                    )
                    Text(
                      text = "+${SereneOasisRepository.REFEREE_WELCOME_POINTS} welcome wellness points added to your balance.",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            } else {
              Text(
                text = "Enter a friend's SALONX code below to unlock +${SereneOasisRepository.REFEREE_WELCOME_POINTS} bonus wellness points.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              OutlinedTextField(
                value = inputCode,
                onValueChange = {
                  inputCode = it.uppercase().take(16)
                  localError = null
                },
                enabled = !isRedeeming,
                singleLine = true,
                label = { Text("Friend's Code") },
                placeholder = { Text("SALONX-K9M2P4") },
                isError = localError != null,
                shape = RoundedCornerShape(14.dp),
                colors =
                  OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OasisPrimary,
                    unfocusedBorderColor = OasisOutlineVariant.copy(alpha = 0.7f),
                  ),
                modifier = Modifier.fillMaxWidth().testTag("refer_redeem_input"),
              )
              if (localError != null) {
                Text(
                  text = localError.orEmpty(),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
                )
              }
              Button(
                onClick = {
                  val clean = inputCode.trim().uppercase()
                  if (!Regex("^SALONX-[A-Z0-9]{6}$").matches(clean)) {
                    localError = "Enter a valid code formatted like SALONX-A1B2C3."
                    return@Button
                  }
                  if (clean == myCode) {
                    localError = "You cannot redeem your own referral code."
                    return@Button
                  }
                  localError = null
                  onRedeemCode(clean) { inputCode = "" }
                },
                enabled = !isRedeeming && inputCode.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = OasisPrimary,
                    contentColor = OasisOnPrimary,
                  ),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("refer_apply_button"),
              ) {
                if (isRedeeming) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = OasisOnPrimary,
                    strokeWidth = 2.dp,
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Applying Code…")
                } else {
                  Text("Apply Referral Code", style = MaterialTheme.typography.labelLarge)
                }
              }
            }
          }
        }
      }

      // Referral History List
      item(key = "history_header") {
        Text(
          text = "Successful Referrals",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      when (referralHistoryState) {
        is UiState.Loading -> {
          item(key = "history_loading") {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = OasisPrimary, modifier = Modifier.size(24.dp))
            }
          }
        }
        is UiState.Error -> {
          item(key = "history_error") {
            Text(
              text = referralHistoryState.message,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
            )
          }
        }
        is UiState.Success -> {
          if (referralHistoryState.data.isEmpty()) {
            item(key = "history_empty") {
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.4f)),
              ) {
                Text(
                  text = "No friends have redeemed your code yet. Share your code above to start earning +${SereneOasisRepository.REFERRER_REWARD_POINTS} pts per invite.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(16.dp),
                )
              }
            }
          } else {
            items(referralHistoryState.data, key = { it.refereeUid.ifEmpty { it.id } }) { item ->
              Surface(
                modifier =
                  Modifier.fillMaxWidth().testTag("referral_history_item_${item.refereeUid}"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(0.75.dp, OasisOutlineVariant.copy(alpha = 0.45f)),
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(SoftSageTint),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Filled.CheckCircle,
                      contentDescription = null,
                      tint = OasisPrimary,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.refereeName,
                      style = MaterialTheme.typography.titleSmall,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = "Joined with ${item.referralCode}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  GoldBadge(text = "+${item.rewardPointsReferrer} pts", showStar = false)
                }
              }
            }
          }
        }
      }
    }
  }
}

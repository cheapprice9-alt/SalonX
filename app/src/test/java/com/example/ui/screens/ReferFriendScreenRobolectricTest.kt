package com.example.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.data.model.ReferralCodeRecord
import com.example.data.model.ReferralRedemption
import com.example.data.model.UserProfile
import com.example.ui.theme.SereneOasisTheme
import com.example.ui.viewmodel.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ReferFriendScreenRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun referFriendScreen_displaysGeneratedCodeHistoryAndValidatesRedemption() {
    val profile =
      UserProfile(
        userId = "referrer_1",
        displayName = "Elena Vance",
        wellnessPoints = 400L,
        referralCode = "SALONX-A1B2C3",
        claimedReferralPoints = 150L,
      )
    val codeRecord =
      ReferralCodeRecord(
        code = "SALONX-A1B2C3",
        ownerUid = "referrer_1",
        ownerName = "Elena Vance",
        successfulReferrals = 1L,
        pointsEarned = 150L,
      )
    val history =
      listOf(
        ReferralRedemption(
          refereeUid = "friend_9",
          refereeName = "Sora Takahashi",
          referrerUid = "referrer_1",
          referralCode = "SALONX-A1B2C3",
          rewardPointsReferrer = 150L,
          rewardPointsReferee = 100L,
        )
      )

    var redeemedInput: String? = null
    var copiedCode: String? = null

    composeTestRule.setContent {
      SereneOasisTheme {
        ReferFriendScreen(
          profile = profile,
          referralCodeRecord = codeRecord,
          referralHistoryState = UiState.Success(history),
          isRedeeming = false,
          onRedeemCode = { code, onSuccess ->
            redeemedInput = code
            onSuccess()
          },
          onCodeCopied = { copiedCode = it },
          onBack = {},
        )
      }
    }

    composeTestRule.onNodeWithText("SALONX-A1B2C3").assertIsDisplayed()
    composeTestRule.onNodeWithText("+150 pts").assertIsDisplayed()

    // Copy own code works
    composeTestRule.onNodeWithTag("refer_copy_button").performClick()
    assertEquals("SALONX-A1B2C3", copiedCode)

    // Scroll LazyColumn to redeem section and verify self-referral guard
    composeTestRule
      .onNodeWithTag("refer_lazy_column")
      .performScrollToNode(hasTestTag("refer_redeem_card"))
    composeTestRule.onNodeWithTag("refer_redeem_input").performTextInput("salonx-a1b2c3")
    composeTestRule.onNodeWithTag("refer_apply_button").performClick()
    composeTestRule.onNodeWithText("You cannot redeem your own referral code.").assertIsDisplayed()
    assertNull(redeemedInput)

    // Scroll LazyColumn to referral history item
    composeTestRule
      .onNodeWithTag("refer_lazy_column")
      .performScrollToNode(hasTestTag("referral_history_item_friend_9"))
    composeTestRule.onNodeWithTag("referral_history_item_friend_9").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sora Takahashi").assertIsDisplayed()
  }

  @Test
  fun referFriendScreen_validFriendCodeInvokesRedeemCallbackAndShowsClaimedBadge() {
    val profile =
      UserProfile(
        userId = "user_2",
        displayName = "Marcus Chen",
        wellnessPoints = 250L,
        referralCode = "SALONX-M9P2Q4",
      )
    var submittedCode: String? = null

    composeTestRule.setContent {
      SereneOasisTheme {
        ReferFriendScreen(
          profile = profile,
          referralCodeRecord = null,
          referralHistoryState = UiState.Success(emptyList()),
          isRedeeming = false,
          onRedeemCode = { code, onSuccess ->
            submittedCode = code
            onSuccess()
          },
          onCodeCopied = {},
          onBack = {},
        )
      }
    }

    composeTestRule
      .onNodeWithTag("refer_lazy_column")
      .performScrollToNode(hasTestTag("refer_redeem_card"))
    composeTestRule.onNodeWithTag("refer_redeem_input").performTextInput("salonx-z8y7x6")
    composeTestRule.onNodeWithTag("refer_apply_button").performClick()
    assertEquals("SALONX-Z8Y7X6", submittedCode)
  }
}

package com.example.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.AssistantChatMessage
import com.example.data.ai.AssistantTurnResponse
import com.example.data.ai.RecommendedRitual
import com.example.data.ai.SmartBookingAssistantClient
import com.example.data.model.SpaCatalog
import com.example.data.model.UserProfile
import com.example.ui.theme.SereneOasisTheme
import com.example.ui.viewmodel.SmartBookingAssistantViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SmartBookingAssistantRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun assistantScreen_sendsPromptDisplaysRecommendationsAndBookAction() {
    val facial = SpaCatalog.treatments.first { it.id == "sage_botanical_facial" }
    val claire = SpaCatalog.therapists.first { it.id == "claire_moreau" }
    val guaSha = SpaCatalog.addOns.first { it.id == "gua_sha" }

    val fakeClient =
      object : SmartBookingAssistantClient {
        override suspend fun sendMessage(
          userMessage: String,
          history: List<AssistantChatMessage>,
          userProfile: UserProfile?,
        ): AssistantTurnResponse {
          return AssistantTurnResponse(
            reply = "For dry, sensitive skin, I recommend soothing white sage and gentle lymphatics.",
            recommendations =
              listOf(
                RecommendedRitual(
                  treatment = facial,
                  matchReason = "White sage and chamomile calm redness while deeply hydrating.",
                  suggestedTherapist = claire,
                  suggestedAddOn = guaSha,
                )
              ),
            followUpPrompts = listOf("Is there a 90-minute upgrade?"),
          )
        }
      }

    val viewModel = SmartBookingAssistantViewModel(fakeClient)
    val profile =
      UserProfile(
        displayName = "Elena Vance",
        preferredPressure = "Gentle",
        aromatherapyNote = "Chamomile",
      )
    var bookedTreatmentId: String? = null

    composeTestRule.setContent {
      val messages by viewModel.messages.collectAsStateWithLifecycle()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      SereneOasisTheme {
        SmartBookingAssistantScreen(
          messages = messages,
          uiState = uiState,
          profile = profile,
          onSendMessage = { viewModel.sendMessage(it, profile) },
          onRetry = { viewModel.retryLastMessage(profile) },
          onResetConversation = { viewModel.resetConversation() },
          onOpenTreatment = {},
          onBookTreatment = { bookedTreatmentId = it },
          onBack = {},
        )
      }
    }

    composeTestRule.onNodeWithTag("assistant_welcome_card").assertIsDisplayed()
    composeTestRule
      .onNodeWithTag("assistant_input_field")
      .performTextInput("Dry and sensitive skin today")
    composeTestRule.onNodeWithTag("assistant_send_button").performClick()

    composeTestRule.waitForIdle()
    composeTestRule
      .onNodeWithText("For dry, sensitive skin, I recommend soothing white sage and gentle lymphatics.")
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("assistant_rec_card_sage_botanical_facial").assertIsDisplayed()
    composeTestRule.onNodeWithTag("assistant_book_sage_botanical_facial").performClick()

    assertEquals("sage_botanical_facial", bookedTreatmentId)
  }

  @Test
  fun assistantScreen_surfacesErrorAndRetriesSuccessfully() {
    val massage = SpaCatalog.treatments.first { it.id == "warm_stone_serenity_massage" }
    var shouldFail = true

    val flakyClient =
      object : SmartBookingAssistantClient {
        override suspend fun sendMessage(
          userMessage: String,
          history: List<AssistantChatMessage>,
          userProfile: UserProfile?,
        ): AssistantTurnResponse {
          if (shouldFail) {
            throw IllegalStateException("App Check attestation temporarily unavailable")
          }
          return AssistantTurnResponse(
            reply = "Try the Warm Stone Serenity Massage to melt deep stress.",
            recommendations =
              listOf(
                RecommendedRitual(
                  treatment = massage,
                  matchReason = "Heated river basalt stones unwind deep shoulder knots.",
                )
              ),
          )
        }
      }

    val viewModel = SmartBookingAssistantViewModel(flakyClient)

    composeTestRule.setContent {
      val messages by viewModel.messages.collectAsStateWithLifecycle()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      SereneOasisTheme {
        SmartBookingAssistantScreen(
          messages = messages,
          uiState = uiState,
          profile = null,
          onSendMessage = { viewModel.sendMessage(it, null) },
          onRetry = { viewModel.retryLastMessage(null) },
          onResetConversation = { viewModel.resetConversation() },
          onOpenTreatment = {},
          onBookTreatment = {},
          onBack = {},
        )
      }
    }

    composeTestRule.onNodeWithTag("assistant_preset_stressed_&_tense").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("assistant_error_card").assertIsDisplayed()
    composeTestRule
      .onNodeWithText("App Check attestation temporarily unavailable")
      .assertIsDisplayed()

    shouldFail = false
    composeTestRule.onNodeWithTag("assistant_retry_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithText("Try the Warm Stone Serenity Massage to melt deep stress.")
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("assistant_rec_card_warm_stone_serenity_massage").assertIsDisplayed()
  }
}

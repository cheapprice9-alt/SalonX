package com.example.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.data.model.SpaBooking
import com.example.ui.theme.SereneOasisTheme
import com.example.ui.viewmodel.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CustomerAppointmentsScreenRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun loadingState_showsSpinner() {
    composeTestRule.setContent {
      SereneOasisTheme {
        CustomerAppointmentsScreen(
          bookingsState = UiState.Loading,
          onRetry = {},
          onRescheduleBooking = { _, _, _, _, _ -> },
          onCancelBooking = {},
          onRebookTreatment = {},
          onBrowseTreatments = {},
        )
      }
    }

    composeTestRule.onNodeWithTag("appointments_loading").assertIsDisplayed()
  }

  @Test
  fun errorState_showsRetryButtonAndInvokesCallback() {
    var retried = false
    composeTestRule.setContent {
      SereneOasisTheme {
        CustomerAppointmentsScreen(
          bookingsState = UiState.Error("Firestore connection unavailable"),
          onRetry = { retried = true },
          onRescheduleBooking = { _, _, _, _, _ -> },
          onCancelBooking = {},
          onRebookTreatment = {},
          onBrowseTreatments = {},
        )
      }
    }

    composeTestRule.onNodeWithTag("appointments_error").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appointments_retry_button").performClick()
    assertTrue(retried)
  }

  @Test
  fun successState_splitsUpcomingAndPastAppointmentsAndSupportsRebook() {
    val upcoming =
      SpaBooking(
        id = "bk_future",
        userId = "user_1",
        treatmentId = "oasis_botanical_facial",
        treatmentTitle = "Botanical Glow Facial",
        treatmentCategory = "Facials",
        therapistId = "therapist_elena",
        therapistName = "Elena Rostova",
        dateLabel = "Mon, Nov 16",
        dateIso = "2026-11-16",
        timeSlot = "11:00 AM",
        durationMinutes = 60L,
        priceUsd = 150L,
        status = "confirmed",
      )
    val pastByDate =
      SpaBooking(
        id = "bk_past_date",
        userId = "user_1",
        treatmentId = "oasis_warm_stone",
        treatmentTitle = "Warm Basalt Massage",
        treatmentCategory = "Massages",
        therapistId = "therapist_marcus",
        therapistName = "Marcus Vance",
        dateLabel = "Tue, Nov 10",
        dateIso = "2026-11-10",
        timeSlot = "02:00 PM",
        durationMinutes = 75L,
        priceUsd = 165L,
        status = "confirmed",
      )
    val cancelled =
      SpaBooking(
        id = "bk_cancelled",
        userId = "user_1",
        treatmentId = "oasis_silk_wrap",
        treatmentTitle = "Silk Body Polish",
        treatmentCategory = "Body Rituals",
        therapistId = "therapist_sora",
        therapistName = "Sora Takahashi",
        dateLabel = "Wed, Nov 18",
        dateIso = "2026-11-18",
        timeSlot = "04:00 PM",
        durationMinutes = 60L,
        priceUsd = 135L,
        status = "cancelled",
      )

    var rebookedId: String? = null
    composeTestRule.setContent {
      SereneOasisTheme {
        CustomerAppointmentsScreen(
          bookingsState = UiState.Success(listOf(upcoming, pastByDate, cancelled)),
          onRetry = {},
          onRescheduleBooking = { _, _, _, _, _ -> },
          onCancelBooking = {},
          onRebookTreatment = { rebookedId = it },
          onBrowseTreatments = {},
          todayIso = "2026-11-14",
        )
      }
    }

    composeTestRule.onNodeWithText("Upcoming (1)").assertIsDisplayed()
    composeTestRule.onNodeWithText("Past (2)").assertIsDisplayed()
    composeTestRule
      .onNodeWithTag("appointments_lazy_column")
      .performScrollToNode(hasTestTag("appointment_card_bk_future"))
    composeTestRule.onNodeWithTag("appointment_card_bk_future").assertIsDisplayed()

    composeTestRule.onNodeWithTag("tab_past_appointments").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
      .onNodeWithTag("appointments_lazy_column")
      .performScrollToNode(hasTestTag("appointment_card_bk_cancelled"))
    composeTestRule.onNodeWithTag("appointment_card_bk_cancelled").assertIsDisplayed()

    composeTestRule
      .onNodeWithTag("appointments_lazy_column")
      .performScrollToNode(hasTestTag("book_again_btn_bk_past_date"))
    composeTestRule.onNodeWithTag("appointment_card_bk_past_date").assertIsDisplayed()
    composeTestRule.onNodeWithTag("book_again_btn_bk_past_date").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals("oasis_warm_stone", rebookedId)
  }
}

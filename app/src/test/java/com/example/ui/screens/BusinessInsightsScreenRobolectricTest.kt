package com.example.ui.screens

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.data.model.SpaBooking
import com.example.data.model.UserProfile
import com.example.ui.insights.InsightsWindow
import com.example.ui.insights.computeBusinessInsights
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
class BusinessInsightsScreenRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun computeBusinessInsights_aggregatesWeeklyRevenueVolumeAndCategories() {
    val bookings =
      listOf(
        SpaBooking(
          id = "b1",
          treatmentTitle = "Botanical Serenity Massage",
          treatmentCategory = "Massage",
          therapistName = "Elena Vance",
          dateIso = "2026-11-10",
          priceUsd = 200L,
          status = "completed",
        ),
        SpaBooking(
          id = "b2",
          treatmentTitle = "Lumiere Renewal Facial",
          treatmentCategory = "Facials",
          therapistName = "Marcus Thorne",
          dateIso = "2026-11-12",
          priceUsd = 160L,
          status = "confirmed",
        ),
        SpaBooking(
          id = "b3",
          treatmentTitle = "Cancelled Ritual",
          treatmentCategory = "Hydrotherapy",
          therapistName = "Marcus Thorne",
          dateIso = "2026-11-12",
          priceUsd = 140L,
          status = "cancelled",
        ),
      )

    val summary =
      computeBusinessInsights(
        bookings = bookings,
        window = InsightsWindow.LAST_7_DAYS,
        todayIso = "2026-11-14",
      )

    assertEquals(7, summary.dailyPoints.size)
    assertEquals(360L, summary.windowRevenueUsd)
    assertEquals(2, summary.windowActiveBookings)
    assertEquals(1, summary.windowCancelledBookings)
    assertEquals(180L, summary.averageTicketUsd)
    assertEquals(66, summary.retentionRatePercent)
    assertEquals(200L, summary.peakDayRevenueUsd)
    assertEquals(2, summary.categoryBreakdown.size)
    assertEquals("Massage", summary.categoryBreakdown.first().category)
  }

  @Test
  fun insightsScreen_errorStateInvokesRetryAndBackButtonWorks() {
    var retried = false
    var backed = false

    composeTestRule.setContent {
      SereneOasisTheme {
        BusinessInsightsScreen(
          profile = UserProfile(displayName = "Kabir"),
          bookingsState = UiState.Error("Offline error"),
          onRetry = { retried = true },
          onBrowseTreatments = {},
          onBack = { backed = true },
          todayIso = "2026-11-14",
        )
      }
    }

    composeTestRule.onNodeWithTag("insights_error_state").assertIsDisplayed()
    composeTestRule.onNodeWithTag("insights_retry_button").performClick()
    assertTrue(retried)

    composeTestRule.onNodeWithTag("insights_back_button").performClick()
    assertTrue(backed)
  }

  @Test
  fun insightsScreen_displaysChartsKpisAndHandlesWindowSwitching() {
    val bookings =
      listOf(
        SpaBooking(
          id = "b_past",
          treatmentTitle = "Deep Botanical Massage",
          treatmentCategory = "Massage",
          therapistName = "Elena Vance",
          dateIso = "2026-11-11",
          priceUsd = 240L,
          status = "completed",
        ),
        SpaBooking(
          id = "b_future",
          treatmentTitle = "Gold Radiance Facial",
          treatmentCategory = "Facials",
          therapistName = "Aria Chen",
          dateIso = "2026-11-16",
          priceUsd = 310L,
          status = "confirmed",
        ),
      )

    composeTestRule.setContent {
      SereneOasisTheme {
        BusinessInsightsScreen(
          profile =
            UserProfile(
              displayName = "Kabir",
              aromatherapyNote = "Salon Partner: SalonX Atelier • Mumbai",
            ),
          bookingsState = UiState.Success(bookings),
          onRetry = {},
          onBrowseTreatments = {},
          onBack = {},
          todayIso = "2026-11-14",
        )
      }
    }

    composeTestRule.onNodeWithTag("kpi_weekly_revenue").assertIsDisplayed().assert(hasText("$240"))
    composeTestRule.onNodeWithTag("insights_revenue_chart_card").assertIsDisplayed()

    composeTestRule
      .onNodeWithTag("insights_lazy_column")
      .performScrollToNode(hasTestTag("insights_volume_chart_card"))
    composeTestRule.onNodeWithTag("insights_volume_chart_card").assertIsDisplayed()

    // Scroll back to top and switch to Next 7 Days
    composeTestRule
      .onNodeWithTag("insights_lazy_column")
      .performScrollToNode(hasTestTag("insights_window_next_7_days"))
    composeTestRule.onNodeWithTag("insights_window_next_7_days").performClick()
    composeTestRule.onNodeWithTag("kpi_weekly_revenue").assertIsDisplayed().assert(hasText("$310"))
  }

  @Test
  fun insightsScreen_emptyStateDisplaysOnboardingCta() {
    var browsed = false

    composeTestRule.setContent {
      SereneOasisTheme {
        BusinessInsightsScreen(
          profile = UserProfile(displayName = "Owner"),
          bookingsState = UiState.Success(emptyList()),
          onRetry = {},
          onBrowseTreatments = { browsed = true },
          onBack = {},
          todayIso = "2026-11-14",
        )
      }
    }

    composeTestRule.onNodeWithTag("insights_empty_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("insights_explore_treatments_button").performClick()
    assertTrue(browsed)
  }
}

package com.example.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.SpaBooking
import com.example.data.model.UserProfile
import com.example.ui.theme.SereneOasisTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class LoyaltyPointsTrackerRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun computeLoyaltySummary_calculatesCompletedVsUpcomingPointsAccurately() {
    val profile = UserProfile(wellnessPoints = 400L, membershipTier = "SAGE_MEMBER")
    val bookings =
      listOf(
        // Completed explicitly
        SpaBooking(id = "b1", status = "completed", dateIso = "2026-11-12"),
        // Completed by past date
        SpaBooking(id = "b2", status = "confirmed", dateIso = "2026-11-08"),
        // Upcoming confirmed
        SpaBooking(id = "b3", status = "confirmed", dateIso = "2026-11-20"),
        // Cancelled (excluded)
        SpaBooking(id = "b4", status = "cancelled", dateIso = "2026-11-11"),
      )

    val summary = computeLoyaltySummary(profile, bookings, todayIso = "2026-11-14")

    assertEquals(2, summary.completedBookingsCount)
    assertEquals(150L, summary.pointsFromCompletedBookings)
    assertEquals(1, summary.upcomingBookingsCount)
    assertEquals(75L, summary.pendingPoints)
    assertEquals(400L, summary.totalPoints)
    assertEquals("Sage Member", summary.currentTierLabel)
    assertEquals("Gold Sanctuary", summary.nextTierLabel)
  }

  @Test
  fun loyaltyCard_displaysCompletedBookingPointsAndInvokesHistoryClick() {
    val profile = UserProfile(wellnessPoints = 550L, membershipTier = "GOLD_SANCTUARY")
    val bookings =
      listOf(
        SpaBooking(id = "b1", status = "completed", dateIso = "2026-11-05"),
        SpaBooking(id = "b2", status = "completed", dateIso = "2026-11-10"),
        SpaBooking(id = "b3", status = "confirmed", dateIso = "2026-11-22"),
      )
    var clicked = false

    composeTestRule.setContent {
      SereneOasisTheme {
        LoyaltyPointsTrackerCard(
          profile = profile,
          bookings = bookings,
          onViewHistory = { clicked = true },
          todayIso = "2026-11-14",
        )
      }
    }

    composeTestRule.onNodeWithTag("loyalty_points_tracker_card").assertIsDisplayed()
    composeTestRule.onNodeWithText("Gold Sanctuary").assertIsDisplayed()
    composeTestRule.onNodeWithText("550 PTS").assertIsDisplayed()
    composeTestRule.onNodeWithText("+150 pts").assertIsDisplayed()
    composeTestRule.onNodeWithText("2 completed visits").assertIsDisplayed()
    composeTestRule.onNodeWithText("+75 pts").assertIsDisplayed()

    composeTestRule.onNodeWithTag("loyalty_points_tracker_card").performClick()
    assertTrue(clicked)
  }
}

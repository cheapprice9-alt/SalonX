package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.model.ReviewTargetType
import com.example.data.model.ServiceReview
import com.example.data.model.SpaBooking
import com.example.ui.theme.SereneOasisTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ReviewComponentRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun reviewComponent_validatesEmptyFeedbackAndSubmitsSelectedTargetAndRating() {
    val booking =
      SpaBooking(
        id = "bk_review_1",
        treatmentId = "sage_botanical_facial",
        treatmentTitle = "Sage Botanical Renewal Facial",
        therapistId = "claire_moreau",
        therapistName = "Claire Moreau",
        dateLabel = "Mon, Nov 9",
        status = "completed",
      )

    var submittedTarget: ReviewTargetType? = null
    var submittedRating: Int? = null
    var submittedComment: String? = null

    composeTestRule.setContent {
      SereneOasisTheme {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          ReviewComponent(
            booking = booking,
            onSubmitReview = { target, rating, comment ->
              submittedTarget = target
              submittedRating = rating
              submittedComment = comment
            },
          )
        }
      }
    }

    // Attempting submit with empty feedback shows validation message
    composeTestRule.onNodeWithTag("review_submit_button").performScrollTo().performClick()
    assertNull(submittedRating)
    composeTestRule
      .onNodeWithText("Please share brief written feedback before submitting.")
      .assertIsDisplayed()

    // Pick staff-only target and 4 stars, then enter feedback
    composeTestRule.onNodeWithTag("review_target_staff").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("review_star_4").performScrollTo().performClick()
    composeTestRule.onNodeWithText("4 / 5 • Very Good").assertIsDisplayed()

    composeTestRule
      .onNodeWithTag("review_comment_input")
      .performScrollTo()
      .performTextInput("Claire Moreau was wonderfully gentle and attentive.")
    composeTestRule.onNodeWithTag("review_submit_button").performScrollTo().performClick()

    assertEquals(ReviewTargetType.STAFF, submittedTarget)
    assertEquals(4, submittedRating)
    assertEquals("Claire Moreau was wonderfully gentle and attentive.", submittedComment)
  }

  @Test
  fun submittedReviewCard_rendersRatingFeedbackAndTriggersEdit() {
    val review =
      ServiceReview(
        id = "bk_done_1",
        bookingId = "bk_done_1",
        targetType = ReviewTargetType.BOTH.name,
        treatmentId = "golden_hammam_ritual",
        treatmentTitle = "Golden Hammam Ritual",
        therapistId = "nadia_khoury",
        therapistName = "Nadia Khoury",
        rating = 5L,
        comment = "Deeply restorative ritual from start to finish.",
      )
    var editClicked = false

    composeTestRule.setContent {
      SereneOasisTheme {
        SubmittedReviewCard(
          review = review,
          onEditClick = { editClicked = true },
        )
      }
    }

    composeTestRule.onNodeWithTag("submitted_review_card_bk_done_1").assertIsDisplayed()
    composeTestRule
      .onNodeWithText("Deeply restorative ritual from start to finish.")
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("edit_review_btn_bk_done_1").performClick()
    assertEquals(true, editClicked)
  }
}

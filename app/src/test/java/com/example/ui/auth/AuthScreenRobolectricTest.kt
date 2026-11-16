package com.example.ui.auth

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.SereneOasisTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthScreenRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(
        context,
        FirebaseOptions.Builder()
          .setApplicationId("1:1234567890:android:abcdef")
          .setApiKey("test-api-key")
          .setProjectId("test-project")
          .build(),
      )
    }
  }

  @Test
  fun authScreen_displaysSalonXHeroAndButtons() {
    composeTestRule.setContent {
      SereneOasisTheme {
        AuthScreen(onAuthSuccess = {})
      }
    }

    composeTestRule.onNodeWithText("SALONX").assertIsDisplayed()
    composeTestRule.onNodeWithText("Your Beauty,\nOur Expertise.").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("get_started_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("google_sign_in_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("register_owner_button").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun authScreen_opensGuestAndOwnerRegistrationSheets() {
    composeTestRule.setContent {
      SereneOasisTheme {
        AuthScreen(onAuthSuccess = {})
      }
    }

    composeTestRule.onNodeWithTag("get_started_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Create Your SalonX Profile").assertIsDisplayed()
    composeTestRule.onNodeWithTag("register_name_input").assertIsDisplayed()
  }

  @Test
  fun authScreen_opensOwnerOnboardingSheet() {
    composeTestRule.setContent {
      SereneOasisTheme {
        AuthScreen(onAuthSuccess = {})
      }
    }

    composeTestRule.onNodeWithTag("register_owner_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("SalonX Partner Onboarding").assertIsDisplayed()
    composeTestRule.onNodeWithTag("owner_salon_name_input").assertIsDisplayed()
  }
}

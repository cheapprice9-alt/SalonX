package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.model.SpaCatalog
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.viewmodel.RegistrationPreferences

// Luxury SalonX Dark Palette matching reference screen
private val ObsidianBg = Color(0xFF0E0908)
private val ObsidianCard = Color(0xFF171110)
private val ObsidianFieldBg = Color(0xFF211816)
private val MedallionBlack = Color(0xFF050404)
private val GoldBright = Color(0xFFF3CF60)
private val GoldRich = Color(0xFFD5A22E)
private val RosePink = Color(0xFFE54C78)
private val RoseSoft = Color(0xFFF07898)
private val WarmWhite = Color(0xFFFAF7F4)
private val MutedTaupe = Color(0xFFA89F99)
private val GoldButtonText = Color(0xFF1A1206)

private enum class OnboardingSheetType {
  GUEST_REGISTER,
  OWNER_REGISTER,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
  onSetPendingRegistration: (RegistrationPreferences?) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var activeSheet by remember { mutableStateOf<OnboardingSheetType?>(null) }

  // Guest registration fields
  var guestName by remember { mutableStateOf("") }
  var guestPressure by remember { mutableStateOf("Medium") }
  var guestAroma by remember { mutableStateOf("Eucalyptus & Bergamot") }
  var guestReferralCode by remember { mutableStateOf("") }

  // Salon owner registration fields
  var salonName by remember { mutableStateOf("") }
  var ownerName by remember { mutableStateOf("") }
  var salonCity by remember { mutableStateOf("") }
  var sheetError by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = coroutineScope,
    )
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(ObsidianBg)
        .drawBehind {
          // Warm golden ambient glow top-left & behind center emblem
          drawCircle(
            brush =
              Brush.radialGradient(
                colors = listOf(GoldRich.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(size.width * 0.42f, size.height * 0.34f),
                radius = size.width * 0.72f,
              ),
            radius = size.width * 0.72f,
            center = Offset(size.width * 0.42f, size.height * 0.34f),
          )
          // Subtle rose-crimson glow behind medallion & bottom right
          drawCircle(
            brush =
              Brush.radialGradient(
                colors = listOf(RosePink.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.72f, size.height * 0.44f),
                radius = size.width * 0.65f,
              ),
            radius = size.width * 0.65f,
            center = Offset(size.width * 0.72f, size.height * 0.44f),
          )
          drawCircle(
            brush =
              Brush.radialGradient(
                colors = listOf(GoldRich.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.85f),
                radius = size.width * 0.75f,
              ),
            radius = size.width * 0.75f,
            center = Offset(size.width * 0.5f, size.height * 0.85f),
          )
        }
        .safeDrawingPadding()
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 18.dp)
          .widthIn(max = 520.dp)
          .align(Alignment.Center),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top-left Brand Header (Badge + SALONX + STYLE • BEAUTY • GLAMOUR)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MedallionBlack,
          border =
            BorderStroke(
              1.dp,
              Brush.linearGradient(colors = listOf(GoldBright.copy(alpha = 0.7f), RosePink.copy(alpha = 0.6f))),
            ),
          modifier = Modifier.size(48.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            SalonXMiniRing(size = 38.dp)
          }
        }
        Column {
          Text(
            text = stringResource(R.string.auth_brand_name),
            style =
              TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                letterSpacing = 2.4.sp,
              ),
            color = GoldBright,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = stringResource(R.string.auth_brand_tagline),
            style =
              TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 1.8.sp,
              ),
            color = MutedTaupe,
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Centered Luxury SalonX Medallion Logo Card
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        SalonXHeroMedallion()
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Headline, Subtitle & Primary CTAs
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = stringResource(R.string.auth_hero_headline),
          style =
            TextStyle(
              fontFamily = InterFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 38.sp,
              lineHeight = 44.sp,
              letterSpacing = (-0.5).sp,
            ),
          color = WarmWhite,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = stringResource(R.string.auth_hero_subtitle),
          style =
            TextStyle(
              fontFamily = InterFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 15.sp,
              lineHeight = 23.sp,
            ),
          color = MutedTaupe,
        )

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(14.dp))
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF3A151B),
            border = BorderStroke(0.75.dp, RosePink.copy(alpha = 0.6f)),
          ) {
            Text(
              text = errorMessage.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFFFD6DF),
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Primary Gold Gradient Button: Get Started
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(56.dp)
              .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = GoldRich.copy(alpha = 0.45f),
                spotColor = GoldBright.copy(alpha = 0.45f),
              )
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.horizontalGradient(
                  colors = listOf(GoldBright, Color(0xFFE7BA42), GoldRich)
                )
              )
              .clickable(enabled = !isLoading) {
                errorMessage = null
                sheetError = null
                activeSheet = OnboardingSheetType.GUEST_REGISTER
              }
              .testTag("get_started_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(R.string.auth_get_started),
            style =
              TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
              ),
            color = GoldButtonText,
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Secondary Gold-Outlined Button: Sign In
        OutlinedButton(
          onClick = {
            errorMessage = null
            isLoading = true
            onSetPendingRegistration(null)
            onGoogleSignInClicked(
              context = context,
              credentialManager = credentialManager,
              onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
              },
              onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
              },
              scope = coroutineScope,
              onAuthCancelled = { isLoading = false },
            )
          },
          enabled = !isLoading,
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.5.dp, GoldRich),
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = Color(0xFF140F0D),
              contentColor = GoldBright,
            ),
          contentPadding = PaddingValues(vertical = 14.dp),
          modifier = Modifier.fillMaxWidth().height(56.dp).testTag("google_sign_in_button"),
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = GoldBright,
              strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = stringResource(R.string.auth_entering_sanctuary),
              style =
                TextStyle(
                  fontFamily = InterFontFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 16.sp,
                ),
            )
          } else {
            Text(
              text = stringResource(R.string.auth_sign_in),
              style =
                TextStyle(
                  fontFamily = InterFontFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 16.sp,
                ),
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Footer: Own a salon? Register as Salon Owner
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .clickable(enabled = !isLoading) {
                errorMessage = null
                sheetError = null
                activeSheet = OnboardingSheetType.OWNER_REGISTER
              }
              .minimumInteractiveComponentSize()
              .padding(vertical = 6.dp)
              .testTag("register_owner_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text =
              buildAnnotatedString {
                withStyle(SpanStyle(color = MutedTaupe)) {
                  append(stringResource(R.string.auth_own_salon_prompt))
                }
                withStyle(
                  SpanStyle(
                    color = RosePink,
                    fontWeight = FontWeight.SemiBold,
                  )
                ) {
                  append(stringResource(R.string.auth_register_salon_owner))
                }
              },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }

  // Registration Bottom Sheet for "Get Started" or "Register as Salon Owner"
  activeSheet?.let { sheetType ->
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { if (!isLoading) activeSheet = null },
      sheetState = sheetState,
      containerColor = ObsidianCard,
      contentColor = WarmWhite,
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        if (sheetType == OnboardingSheetType.GUEST_REGISTER) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Spa,
              contentDescription = null,
              tint = GoldBright,
            )
            Text(
              text = "Create Your SalonX Profile",
              style = MaterialTheme.typography.headlineSmall,
              color = WarmWhite,
            )
          }
          Text(
            text = "Personalize your guest experience and earn 250 welcome wellness points upon joining with Google.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedTaupe,
          )

          DarkLuxuryTextField(
            value = guestName,
            onValueChange = { if (it.length <= 80) guestName = it },
            label = "Full Name",
            placeholder = "e.g. Aarav Mehta",
            leadingIcon = {
              Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = GoldBright)
            },
            modifier = Modifier.fillMaxWidth().testTag("register_name_input"),
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Preferred Ritual Pressure",
              style = MaterialTheme.typography.titleSmall,
              color = WarmWhite,
            )
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              SpaCatalog.pressureLevels.forEach { level ->
                val selected = level == guestPressure
                Surface(
                  modifier =
                    Modifier.clip(RoundedCornerShape(999.dp))
                      .clickable { guestPressure = level }
                      .minimumInteractiveComponentSize()
                      .testTag("register_pressure_${level.lowercase()}"),
                  shape = RoundedCornerShape(999.dp),
                  color = if (selected) GoldBright else ObsidianFieldBg,
                  border =
                    if (selected) null
                    else BorderStroke(0.75.dp, GoldRich.copy(alpha = 0.45f)),
                ) {
                  Text(
                    text = level,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) GoldButtonText else WarmWhite,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                  )
                }
              }
            }
          }

          DarkLuxuryTextField(
            value = guestAroma,
            onValueChange = { if (it.length <= 200) guestAroma = it },
            label = "Signature Scent & Beverage Note",
            placeholder = "Eucalyptus & Bergamot",
            modifier = Modifier.fillMaxWidth().testTag("register_aroma_input"),
          )

          DarkLuxuryTextField(
            value = guestReferralCode,
            onValueChange = { guestReferralCode = it.uppercase().take(16) },
            label = "Friend's Referral Code (Optional • +100 Bonus Pts)",
            placeholder = "SALONX-K9M2P4",
            modifier = Modifier.fillMaxWidth().testTag("register_referral_code_input"),
          )

          if (sheetError != null) {
            Text(
              text = sheetError.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              color = RoseSoft,
            )
          }

          Button(
            onClick = {
              val cleanName = guestName.trim()
              if (cleanName.isEmpty()) {
                sheetError = "Please enter your name to create your SalonX profile."
                return@Button
              }
              sheetError = null
              isLoading = true
              onSetPendingRegistration(
                RegistrationPreferences(
                  displayName = cleanName,
                  preferredPressure = guestPressure,
                  aromatherapyNote = guestAroma.trim().ifEmpty { "Eucalyptus & Bergamot" },
                  referralCode = guestReferralCode.trim().uppercase().takeIf { it.isNotEmpty() },
                )
              )
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  activeSheet = null
                  onAuthSuccess()
                },
                onAuthError = { msg ->
                  isLoading = false
                  sheetError = msg
                },
                scope = coroutineScope,
                onAuthCancelled = { isLoading = false },
              )
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = GoldBright,
                contentColor = GoldButtonText,
              ),
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("google_register_button"),
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = GoldButtonText,
                strokeWidth = 2.dp,
              )
            } else {
              Text(
                text = stringResource(R.string.auth_register_google),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        } else {
          // Salon Owner Partner Registration
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Storefront,
              contentDescription = null,
              tint = RosePink,
            )
            Text(
              text = "SalonX Partner Onboarding",
              style = MaterialTheme.typography.headlineSmall,
              color = WarmWhite,
            )
          }
          Text(
            text = "Register your salon or studio with SalonX to manage suites and bookings with your Google account.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedTaupe,
          )

          DarkLuxuryTextField(
            value = salonName,
            onValueChange = { if (it.length <= 60) salonName = it },
            label = "Salon / Studio Name",
            placeholder = "e.g. SalonX Atelier",
            modifier = Modifier.fillMaxWidth().testTag("owner_salon_name_input"),
          )
          DarkLuxuryTextField(
            value = ownerName,
            onValueChange = { if (it.length <= 60) ownerName = it },
            label = "Owner Full Name",
            placeholder = "e.g. Kabir Sharma",
            modifier = Modifier.fillMaxWidth().testTag("owner_name_input"),
          )
          DarkLuxuryTextField(
            value = salonCity,
            onValueChange = { if (it.length <= 60) salonCity = it },
            label = "City",
            placeholder = "e.g. Mumbai",
            modifier = Modifier.fillMaxWidth().testTag("owner_city_input"),
          )

          if (sheetError != null) {
            Text(
              text = sheetError.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              color = RoseSoft,
            )
          }

          Button(
            onClick = {
              val cleanSalon = salonName.trim()
              val cleanOwner = ownerName.trim()
              if (cleanSalon.isEmpty() || cleanOwner.isEmpty()) {
                sheetError = "Please enter your salon name and owner name."
                return@Button
              }
              sheetError = null
              isLoading = true
              val citySuffix = salonCity.trim().takeIf { it.isNotEmpty() }?.let { " • $it" } ?: ""
              onSetPendingRegistration(
                RegistrationPreferences(
                  displayName = cleanOwner,
                  preferredPressure = "Medium",
                  aromatherapyNote = "Salon Partner: $cleanSalon$citySuffix",
                )
              )
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  activeSheet = null
                  onAuthSuccess()
                },
                onAuthError = { msg ->
                  isLoading = false
                  sheetError = msg
                },
                scope = coroutineScope,
                onAuthCancelled = { isLoading = false },
              )
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = GoldBright,
                contentColor = GoldButtonText,
              ),
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("google_owner_register_button"),
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = GoldButtonText,
                strokeWidth = 2.dp,
              )
            } else {
              Text(
                text = "Register Partner with Google",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }
    }
  }
}

@Composable
private fun DarkLuxuryTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  placeholder: String,
  modifier: Modifier = Modifier,
  leadingIcon: @Composable (() -> Unit)? = null,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    placeholder = { Text(placeholder, color = MutedTaupe.copy(alpha = 0.65f)) },
    leadingIcon = leadingIcon,
    singleLine = true,
    shape = RoundedCornerShape(14.dp),
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedContainerColor = ObsidianFieldBg,
        unfocusedContainerColor = ObsidianFieldBg,
        focusedTextColor = WarmWhite,
        unfocusedTextColor = WarmWhite,
        focusedBorderColor = GoldBright,
        unfocusedBorderColor = GoldRich.copy(alpha = 0.4f),
        focusedLabelColor = GoldBright,
        unfocusedLabelColor = MutedTaupe,
        cursorColor = GoldBright,
      ),
    modifier = modifier,
  )
}

@Composable
private fun SalonXMiniRing(size: Dp) {
  Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawCircle(
        brush = Brush.sweepGradient(colors = listOf(GoldBright, RosePink, GoldRich, GoldBright)),
        radius = this.size.minDimension / 2.25f,
        style = Stroke(width = 2.dp.toPx()),
      )
    }
    Text(
      text = "SX",
      style =
        TextStyle(
          fontFamily = PlayfairDisplayFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          brush = Brush.linearGradient(colors = listOf(GoldBright, RoseSoft)),
        ),
    )
  }
}

@Composable
private fun SalonXHeroMedallion() {
  Surface(
    modifier =
      Modifier.size(216.dp)
        .shadow(
          elevation = 24.dp,
          shape = RoundedCornerShape(12.dp),
          ambientColor = RosePink.copy(alpha = 0.25f),
          spotColor = GoldBright.copy(alpha = 0.25f),
        ),
    shape = RoundedCornerShape(8.dp),
    color = MedallionBlack,
  ) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.minDimension / 2f - 4.dp.toPx()

        // Outer metallic gold-to-rose ring
        drawCircle(
          brush =
            Brush.linearGradient(
              colors = listOf(GoldBright, GoldRich, RosePink, RoseSoft, GoldBright),
              start = Offset.Zero,
              end = Offset(size.width, size.height),
            ),
          radius = outerR,
          center = center,
          style = Stroke(width = 2.6.dp.toPx()),
        )

        // Inner hairline ring
        drawCircle(
          brush =
            Brush.linearGradient(
              colors = listOf(GoldRich.copy(alpha = 0.5f), RosePink.copy(alpha = 0.5f))
            ),
          radius = outerR - 5.dp.toPx(),
          center = center,
          style = Stroke(width = 0.8.dp.toPx()),
        )

        // Subtle ornamental flourish lines flanking bottom badge
        val yLine = center.y + outerR * 0.58f
        drawLine(
          color = GoldRich.copy(alpha = 0.7f),
          start = Offset(center.x - outerR * 0.55f, yLine),
          end = Offset(center.x - 20.dp.toPx(), yLine),
          strokeWidth = 1.dp.toPx(),
          cap = StrokeCap.Round,
        )
        drawLine(
          color = RosePink.copy(alpha = 0.7f),
          start = Offset(center.x + 20.dp.toPx(), yLine),
          end = Offset(center.x + outerR * 0.55f, yLine),
          strokeWidth = 1.dp.toPx(),
          cap = StrokeCap.Round,
        )

        // Subtle star sparkles around ring
        fun drawSparkle(pos: Offset, radius: Float, tint: Color) {
          val p =
            Path().apply {
              moveTo(pos.x, pos.y - radius)
              lineTo(pos.x + radius * 0.28f, pos.y - radius * 0.28f)
              lineTo(pos.x + radius, pos.y)
              lineTo(pos.x + radius * 0.28f, pos.y + radius * 0.28f)
              lineTo(pos.x, pos.y + radius)
              lineTo(pos.x - radius * 0.28f, pos.y + radius * 0.28f)
              lineTo(pos.x - radius, pos.y)
              lineTo(pos.x - radius * 0.28f, pos.y - radius * 0.28f)
              close()
            }
          drawPath(p, color = tint)
        }
        drawSparkle(Offset(center.x + outerR * 0.78f, center.y - outerR * 0.62f), 4.5.dp.toPx(), GoldBright)
        drawSparkle(Offset(center.x - outerR * 0.82f, center.y - outerR * 0.45f), 3.5.dp.toPx(), RoseSoft)
        drawSparkle(Offset(center.x + outerR * 0.72f, center.y + outerR * 0.68f), 4.dp.toPx(), RosePink)
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 24.dp),
      ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Stylized SX Luxury Monogram
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Text(
            text = "S",
            style =
              TextStyle(
                fontFamily = PlayfairDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 54.sp,
                brush = Brush.verticalGradient(colors = listOf(GoldBright, GoldRich)),
              ),
          )
          Text(
            text = "X",
            style =
              TextStyle(
                fontFamily = PlayfairDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 54.sp,
                brush = Brush.verticalGradient(colors = listOf(RoseSoft, RosePink)),
              ),
          )
        }

        Text(
          text = "SALONX™",
          style =
            TextStyle(
              fontFamily = PlayfairDisplayFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 18.sp,
              letterSpacing = 3.2.sp,
              brush = Brush.horizontalGradient(colors = listOf(GoldBright, RoseSoft)),
            ),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "STYLE  •  BEAUTY  •  GLAMOUR",
          style =
            TextStyle(
              fontFamily = InterFontFamily,
              fontWeight = FontWeight.Medium,
              fontSize = 7.5.sp,
              letterSpacing = 1.4.sp,
            ),
          color = WarmWhite.copy(alpha = 0.88f),
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Circular Scissors Badge at bottom of medallion
        Surface(
          shape = CircleShape,
          color = MedallionBlack,
          border = BorderStroke(1.dp, GoldRich.copy(alpha = 0.8f)),
          modifier = Modifier.size(26.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.ContentCut,
              contentDescription = null,
              tint = GoldBright,
              modifier = Modifier.size(13.dp),
            )
          }
        }
      }
    }
  }
}

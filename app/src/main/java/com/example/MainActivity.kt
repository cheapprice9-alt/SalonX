package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.SpaCatalog
import com.example.data.remote.configureAppCheck
import com.example.data.repository.SereneOasisRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutFromSanctuary
import com.example.ui.screens.BookingFlowScreen
import com.example.ui.screens.BusinessInsightsScreen
import com.example.ui.screens.CustomerAppointmentsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReferFriendScreen
import com.example.ui.screens.SanctuaryHomeScreen
import com.example.ui.screens.SmartBookingAssistantScreen
import com.example.ui.screens.TreatmentDetailScreen
import com.example.ui.screens.TreatmentsScreen
import com.example.ui.theme.OasisPrimary
import com.example.ui.theme.SereneOasisTheme
import com.example.ui.theme.SoftSageTint
import com.example.ui.viewmodel.RegistrationPreferences
import com.example.ui.viewmodel.SanctuaryViewModel
import com.example.ui.viewmodel.SmartBookingAssistantViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    configureAppCheck(this, intent)
    enableEdgeToEdge()
    setContent {
      SereneOasisTheme {
        AppNavigation()
      }
    }
  }
}

private enum class SanctuaryTab(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  SANCTUARY("sanctuary", R.string.nav_sanctuary, Icons.Filled.Spa, Icons.Outlined.Spa),
  TREATMENTS("treatments", R.string.nav_treatments, Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
  BOOKINGS("bookings", R.string.nav_bookings, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
  PROFILE("profile", R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person),
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  val currentUser by
    auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  var pendingRegistration by remember { mutableStateOf<RegistrationPreferences?>(null) }
  val user = currentUser

  if (user == null) {
    AuthScreen(
      onAuthSuccess = {},
      onSetPendingRegistration = { pendingRegistration = it },
    )
  } else {
    key(user.uid) {
      MainSanctuaryScaffold(
        currentUserId = user.uid,
        initialDisplayName = user.displayName,
        initialEmail = user.email,
        registrationPreferences = pendingRegistration,
      )
    }
  }
}

@Composable
private fun MainSanctuaryScaffold(
  currentUserId: String,
  initialDisplayName: String?,
  initialEmail: String?,
  registrationPreferences: RegistrationPreferences?,
) {
  val viewModel: SanctuaryViewModel =
    viewModel(
      key = currentUserId,
      factory =
        viewModelFactory {
          initializer {
            val app =
              checkNotNull(this[APPLICATION_KEY]) {
                "APPLICATION_KEY missing from CreationExtras"
              }
            val databaseId = app.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            SanctuaryViewModel(
              repository = SereneOasisRepository(db),
              currentUserId = currentUserId,
              initialDisplayName = initialDisplayName,
              initialEmail = initialEmail,
              registrationPreferences = registrationPreferences,
            )
          }
        },
    )

  val profileState by viewModel.profileState.collectAsStateWithLifecycle()
  val bookingsState by viewModel.bookingsState.collectAsStateWithLifecycle()
  val reviewsState by viewModel.reviewsState.collectAsStateWithLifecycle()
  val referralCodeRecord by viewModel.referralCodeState.collectAsStateWithLifecycle()
  val referralHistoryState by viewModel.referralHistoryState.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val isSubmittingBooking by viewModel.isSubmittingBooking.collectAsStateWithLifecycle()
  val isSubmittingReview by viewModel.isSubmittingReview.collectAsStateWithLifecycle()
  val isRedeemingReferral by viewModel.isRedeemingReferral.collectAsStateWithLifecycle()
  val lastCreatedBookingId by viewModel.lastCreatedBookingId.collectAsStateWithLifecycle()
  val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

  val profile = (profileState as? UiState.Success)?.data
  val bookings = (bookingsState as? UiState.Success)?.data ?: emptyList()
  val reviews = (reviewsState as? UiState.Success)?.data ?: emptyList()

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val showTopLevelChrome = SanctuaryTab.entries.any { it.route == currentRoute }

  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  LaunchedEffect(actionMessage) {
    val msg = actionMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearActionMessage()
    }
  }

  val navigateToTab: (SanctuaryTab) -> Unit = { tab ->
    navController.navigate(tab.route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = MaterialTheme.colorScheme.surface,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      bottomBar = {
        if (showTopLevelChrome && !useNavigationRail) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            tonalElevation = 2.dp,
          ) {
            SanctuaryTab.entries.forEach { tab ->
              val selected = currentRoute == tab.route
              val label = stringResource(tab.labelRes)
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(tab) },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = label,
                  )
                },
                label = {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                  )
                },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = OasisPrimary,
                    selectedTextColor = OasisPrimary,
                    indicatorColor = SoftSageTint,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
                modifier = Modifier.testTag("nav_tab_${tab.route}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (showTopLevelChrome && useNavigationRail) {
          NavigationRail(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier.statusBarsPadding(),
          ) {
            SanctuaryTab.entries.forEach { tab ->
              val selected = currentRoute == tab.route
              val label = stringResource(tab.labelRes)
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(tab) },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = label,
                  )
                },
                label = {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                  )
                },
                colors =
                  NavigationRailItemDefaults.colors(
                    selectedIconColor = OasisPrimary,
                    selectedTextColor = OasisPrimary,
                    indicatorColor = SoftSageTint,
                  ),
                modifier = Modifier.testTag("rail_tab_${tab.route}"),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          if (profileState is UiState.Loading && profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = OasisPrimary)
            }
          } else {
            NavHost(
              navController = navController,
              startDestination = SanctuaryTab.SANCTUARY.route,
              modifier = Modifier.fillMaxSize(),
            ) {
              composable(SanctuaryTab.SANCTUARY.route) {
                SanctuaryHomeScreen(
                  profile = profile,
                  bookings = bookings,
                  selectedCategory = selectedCategory,
                  onSelectCategory = viewModel::selectCategory,
                  onToggleFavorite = viewModel::toggleFavorite,
                  onOpenTreatment = { treatment ->
                    navController.navigate("treatment/${treatment.id}")
                  },
                  onExploreAllTreatments = { navigateToTab(SanctuaryTab.TREATMENTS) },
                  onOpenBookings = { navigateToTab(SanctuaryTab.BOOKINGS) },
                  onOpenAssistant = { initialPrompt ->
                    val encoded = Uri.encode(initialPrompt.orEmpty())
                    navController.navigate("assistant?prompt=$encoded")
                  },
                  onOpenReferFriend = {
                    navController.navigate("refer_friend")
                  },
                  modifier = Modifier.statusBarsPadding(),
                )
              }

              composable(SanctuaryTab.TREATMENTS.route) {
                TreatmentsScreen(
                  profile = profile,
                  selectedCategory = selectedCategory,
                  searchQuery = searchQuery,
                  onSelectCategory = viewModel::selectCategory,
                  onSearchQueryChange = viewModel::updateSearchQuery,
                  onToggleFavorite = viewModel::toggleFavorite,
                  onOpenTreatment = { treatment ->
                    navController.navigate("treatment/${treatment.id}")
                  },
                  onOpenAssistant = {
                    navController.navigate("assistant?prompt=")
                  },
                  modifier = Modifier.statusBarsPadding(),
                )
              }

              composable(SanctuaryTab.BOOKINGS.route) {
                CustomerAppointmentsScreen(
                  bookingsState = bookingsState,
                  onRetry = viewModel::retryBookings,
                  onRescheduleBooking = viewModel::rescheduleBooking,
                  onCancelBooking = viewModel::cancelBooking,
                  onRebookTreatment = { treatmentId ->
                    viewModel.clearLastCreatedBooking()
                    navController.navigate("booking/$treatmentId")
                  },
                  onBrowseTreatments = { navigateToTab(SanctuaryTab.TREATMENTS) },
                  reviews = reviews,
                  isSubmittingReview = isSubmittingReview,
                  onSubmitReview = viewModel::submitReview,
                  modifier = Modifier.statusBarsPadding(),
                )
              }

              composable(SanctuaryTab.PROFILE.route) {
                ProfileScreen(
                  profile = profile,
                  onSavePreferences = viewModel::savePreferences,
                  onOpenTreatment = { treatment ->
                    navController.navigate("treatment/${treatment.id}")
                  },
                  onSignOut = {
                    signOutFromSanctuary(
                      credentialManager = credentialManager,
                      scope = coroutineScope,
                    )
                  },
                  onOpenBusinessInsights = {
                    navController.navigate("business_insights")
                  },
                  onOpenReferFriend = {
                    navController.navigate("refer_friend")
                  },
                  modifier = Modifier.statusBarsPadding(),
                )
              }

              composable("refer_friend") {
                ReferFriendScreen(
                  profile = profile,
                  referralCodeRecord = referralCodeRecord,
                  referralHistoryState = referralHistoryState,
                  isRedeeming = isRedeemingReferral,
                  onRedeemCode = viewModel::redeemReferralCode,
                  onCodeCopied = { copiedCode ->
                    coroutineScope.launch {
                      snackbarHostState.showSnackbar("Copied $copiedCode to clipboard.")
                    }
                  },
                  onBack = { navController.popBackStack() },
                )
              }

              composable("business_insights") {
                BusinessInsightsScreen(
                  profile = profile,
                  bookingsState = bookingsState,
                  onRetry = viewModel::retryBookings,
                  onBrowseTreatments = {
                    navController.popBackStack(
                      route = SanctuaryTab.SANCTUARY.route,
                      inclusive = false,
                    )
                    navigateToTab(SanctuaryTab.TREATMENTS)
                  },
                  onBack = { navController.popBackStack() },
                )
              }

              composable(
                route = "assistant?prompt={prompt}",
                arguments =
                  listOf(
                    navArgument("prompt") {
                      type = NavType.StringType
                      defaultValue = ""
                    }
                  ),
              ) { entry ->
                val initialPrompt = entry.arguments?.getString("prompt")
                val assistantViewModel: SmartBookingAssistantViewModel = viewModel()
                val assistantMessages by assistantViewModel.messages.collectAsStateWithLifecycle()
                val assistantUiState by assistantViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(initialPrompt) {
                  assistantViewModel.submitInitialPromptIfNeeded(initialPrompt, profile)
                }

                SmartBookingAssistantScreen(
                  messages = assistantMessages,
                  uiState = assistantUiState,
                  profile = profile,
                  onSendMessage = { prompt -> assistantViewModel.sendMessage(prompt, profile) },
                  onRetry = { assistantViewModel.retryLastMessage(profile) },
                  onResetConversation = { assistantViewModel.resetConversation() },
                  onOpenTreatment = { treatmentId ->
                    navController.navigate("treatment/$treatmentId")
                  },
                  onBookTreatment = { treatmentId ->
                    viewModel.clearLastCreatedBooking()
                    navController.navigate("booking/$treatmentId")
                  },
                  onBack = { navController.popBackStack() },
                )
              }

              composable(
                route = "treatment/{treatmentId}",
                arguments = listOf(navArgument("treatmentId") { type = NavType.StringType }),
              ) { entry ->
                val treatmentId = entry.arguments?.getString("treatmentId")
                val treatment =
                  SpaCatalog.treatments.firstOrNull { it.id == treatmentId }
                    ?: SpaCatalog.treatments.first()
                val isFav =
                  profile?.favoriteTreatmentIds?.contains(treatment.id) == true

                TreatmentDetailScreen(
                  treatment = treatment,
                  isFavorite = isFav,
                  treatmentReviews = reviews.filter { it.treatmentId == treatment.id },
                  onToggleFavorite = { viewModel.toggleFavorite(treatment.id) },
                  onBack = { navController.popBackStack() },
                  onReserveClick = {
                    viewModel.clearLastCreatedBooking()
                    navController.navigate("booking/${treatment.id}")
                  },
                )
              }

              composable(
                route = "booking/{treatmentId}",
                arguments = listOf(navArgument("treatmentId") { type = NavType.StringType }),
              ) { entry ->
                val treatmentId = entry.arguments?.getString("treatmentId")
                val treatment =
                  SpaCatalog.treatments.firstOrNull { it.id == treatmentId }
                    ?: SpaCatalog.treatments.first()

                BookingFlowScreen(
                  treatment = treatment,
                  isSubmitting = isSubmittingBooking,
                  confirmedBookingId = lastCreatedBookingId,
                  onConfirmBooking = {
                    therapistId,
                    therapistName,
                    dateLabel,
                    dateIso,
                    timeSlot,
                    addOns,
                    notes ->
                    viewModel.submitBooking(
                      treatment = treatment,
                      therapistId = therapistId,
                      therapistName = therapistName,
                      dateLabel = dateLabel,
                      dateIso = dateIso,
                      timeSlot = timeSlot,
                      selectedAddOnIds = addOns,
                      specialRequests = notes,
                    )
                  },
                  onBack = {
                    viewModel.clearLastCreatedBooking()
                    navController.popBackStack()
                  },
                  onViewBookings = {
                    viewModel.clearLastCreatedBooking()
                    navController.popBackStack(
                      route = SanctuaryTab.SANCTUARY.route,
                      inclusive = false,
                    )
                    navigateToTab(SanctuaryTab.BOOKINGS)
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}

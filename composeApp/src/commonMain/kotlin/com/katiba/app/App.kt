package com.katiba.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.katiba.app.data.AppPreferences
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.auth.CivicDataInputScreen
import com.katiba.app.ui.auth.ForgotPasswordScreen
import com.katiba.app.ui.auth.LoginScreen
import com.katiba.app.ui.auth.OTPVerificationScreen
import com.katiba.app.ui.auth.ResetPasswordScreen
import com.katiba.app.ui.auth.SignUpScreen
import com.katiba.app.ui.constitution.ClauseGridScreen
import com.katiba.app.ui.constitution.ConstitutionScreen
import com.katiba.app.ui.constitution.PreambleScreen
import com.katiba.app.ui.constitution.ReadingScreen
import com.katiba.app.ui.constitution.SchedulesScreen
import com.katiba.app.ui.constitution.ScheduleDetailScreen
import com.katiba.app.ui.home.*
import com.katiba.app.ui.navigation.*
import com.katiba.app.ui.onboarding.OnboardingScreen
import com.katiba.app.ui.plans.LessonScreen
import com.katiba.app.ui.plans.PlansScreen
import com.katiba.app.ui.profile.ProfileScreen
import com.katiba.app.ui.profile.SettingsScreen
import com.katiba.app.ui.profile.EditProfileScreen
import com.katiba.app.ui.profile.PasswordSecurityScreen
import com.katiba.app.ui.profile.UpdateResidenceScreen
import com.katiba.app.ui.profile.NationalIDScreen
import com.katiba.app.ui.profile.AppearanceScreen
import com.katiba.app.ui.profile.FontSizeScreen
import com.katiba.app.ui.profile.LanguageScreen
import com.katiba.app.ui.profile.AboutKatibaScreen
import com.katiba.app.ui.profile.SendFeedbackScreen
import com.katiba.app.ui.theme.KatibaTheme
import katiba.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

import com.katiba.app.data.repository.AuthRepositoryImpl
import com.katiba.app.data.service.GoogleSignInService

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App(
    googleSignInService: GoogleSignInService? = null // Nullable for platforms where not yet implemented
) {
    // Initialize Auth Repository
    val authRepository = remember { AuthRepositoryImpl() }
    val coroutineScope = rememberCoroutineScope()

    // Load constitution data on first composition
    var isConstitutionLoaded by remember { mutableStateOf(ConstitutionRepository.isLoaded()) }

    LaunchedEffect(Unit) {
        if (!ConstitutionRepository.isLoaded()) {
            withContext(Dispatchers.Default) {
                try {
                    val jsonBytes = Res.readBytes("files/constitution_of_kenya.json")
                    val jsonString = jsonBytes.decodeToString()
                    ConstitutionRepository.loadFromJson(jsonString)
                    isConstitutionLoaded = true
                } catch (e: Exception) {
                    // Log error but continue - app can work with sample data
                    println("Failed to load constitution: ${e.message}")
                    isConstitutionLoaded = true // Continue anyway
                }
            }
        }
    }

    KatibaTheme {
        if (!isConstitutionLoaded) {
            // Show loading indicator while constitution data loads
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AppContent(
                authRepository = authRepository,
                googleSignInService = googleSignInService,
                coroutineScope = coroutineScope
            )
        }
    }
}

@Composable
private fun AppContent(
    authRepository: AuthRepositoryImpl,
    googleSignInService: GoogleSignInService?,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
        // Determine initial route based on onboarding completion
        val initialRoute: NavKey = if (AppPreferences.hasCompletedOnboarding) {
            LoginRoute // Skip onboarding, go to login
        } else {
            OnboardingRoute // First time user, show onboarding
        }
        
        // Navigation 3 Style: You own the backstack as a mutable list
        val backStack = rememberNavBackStack(initialRoute)

        // Helper to get current route from backstack
        val currentKey = backStack.lastOrNull()

        // Handle system back button/swipe navigation
        // When on Home screen (only HomeRoute in backstack), don't handle back - let system exit app
        val shouldHandleBack = !(backStack.size == 1 && currentKey is HomeRoute)
        BackHandler(enabled = shouldHandleBack) {
            if (backStack.size <= 1) {
                // On the last screen (not Home) — navigate to Home
                backStack.clear()
                backStack.add(HomeRoute)
            } else {
                backStack.removeLast()
            }
        }

        // Determine current tab based on the current key
        val currentTab = remember(currentKey) {
            when (currentKey) {
                is HomeRoute,
                is ClauseDetailRoute,
                is AIDescriptionRoute,
                is TipsRoute,
                is MzalendoRoute -> BottomNavTab.HOME

                is ConstitutionRoute,
                is ChapterListRoute,
                is ClauseGridRoute,
                is ReadingRoute -> BottomNavTab.KATIBA

                is PlansRoute,
                is LessonRoute -> BottomNavTab.PLANS

                is ProfileRoute,
                is SettingsRoute,
                is NotificationsRoute -> BottomNavTab.PROFILE

                else -> BottomNavTab.HOME
            }
        }

        // Observe authentication state
        val currentUser by authRepository.currentUser.collectAsState(initial = null)
        
        // Determine if bottom bar should be shown
        // Only show bottom bar when user is authenticated
        val showBottomBar = remember(currentKey, currentUser) {
            currentUser != null && when (currentKey) {
                is HomeRoute -> true
                is ConstitutionRoute -> true
                is PlansRoute -> true
                is ProfileRoute -> true
                else -> false
            }
        }
        
        // Redirect to login if user is not authenticated and tries to access protected routes
        LaunchedEffect(currentUser, currentKey) {
            if (currentUser == null) {
                val isProtectedRoute = when (currentKey) {
                    is HomeRoute,
                    is ConstitutionRoute,
                    is PlansRoute,
                    is ProfileRoute,
                    is ClauseDetailRoute,
                    is AIDescriptionRoute,
                    is TipsRoute,
                    is MzalendoRoute,
                    is NotificationsRoute,
                    is ChapterListRoute,
                    is ClauseGridRoute,
                    is ReadingRoute,
                    is LessonRoute,
                    is SettingsRoute,
                    is EditProfileRoute,
                    is PasswordSecurityRoute,
                    is UpdateResidenceRoute,
                    is NationalIDRoute,
                    is AppearanceRoute,
                    is FontSizeRoute,
                    is LanguageRoute,
                    is AboutKatibaRoute,
                    is SendFeedbackRoute -> true
                    else -> false
                }
                
                if (isProtectedRoute) {
                    // Redirect to login
                    backStack.clear()
                    if (AppPreferences.hasCompletedOnboarding) {
                        backStack.add(LoginRoute)
                    } else {
                        backStack.add(OnboardingRoute)
                    }
                }
            }
        }

        // Check if we're on an auth/onboarding screen (no back navigation to these after login)
        val isAuthScreen = remember(currentKey) {
            currentKey is OnboardingRoute ||
            currentKey is LoginRoute ||
            currentKey is SignUpRoute ||
            currentKey is ForgotPasswordRoute ||
            currentKey is OTPVerificationRoute ||
            currentKey is CivicDataInputRoute ||
            currentKey is ResetPasswordRoute
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.White,
            bottomBar = {
                if (showBottomBar) {
                    KatibaBottomNavigation(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            // Navigation 3 Style: Clear back to root and add new tab
                            while (backStack.size > 1) {
                                backStack.removeLast()
                            }
                            // If current root isn't the selected tab, replace it
                            if (backStack.lastOrNull() != tab.route) {
                                if (backStack.isNotEmpty()) {
                                    backStack.removeLast()
                                }
                                backStack.add(tab.route)
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            // Navigation 3 Style: NavDisplay replaces NavHost
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(paddingValues),
                entryDecorators = listOf(
                    rememberSavedStateNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    // Onboarding
                    entry<OnboardingRoute> {
                        OnboardingScreen(
                            onGetStarted = {
                                // Mark onboarding as completed
                                AppPreferences.hasCompletedOnboarding = true
                                backStack.add(LoginRoute)
                            }
                        )
                    }

                    // Auth screens
                    entry<LoginRoute> {
                        LoginScreen(
                            authRepository = authRepository,
                            googleSignInService = googleSignInService,
                            onLoginSuccess = {
                                // Clear backstack and go to Home
                                backStack.clear()
                                backStack.add(HomeRoute)
                            },
                            onNavigateToSignUp = {
                                backStack.add(SignUpRoute)
                            },
                            onNavigateToForgotPassword = {
                                backStack.add(ForgotPasswordRoute)
                            }
                        )
                    }

                    entry<ForgotPasswordRoute> {
                        ForgotPasswordScreen(
                            onBackToLogin = {
                                backStack.removeLast()
                            },
                            onPasswordResetSuccess = {
                                // Navigate back to login after successful password reset
                                backStack.removeLast()
                            }
                        )
                    }

                    entry<SignUpRoute> {
                        SignUpScreen(
                            authRepository = authRepository,
                            googleSignInService = googleSignInService,
                            onSignUpSuccess = { userId, email ->
                                // Navigate to OTP verification
                                backStack.add(OTPVerificationRoute(userId, email, "email_verification"))
                            },
                            onGoogleSignUpSuccess = {
                                // Google accounts are already verified, skip to civic data
                                backStack.add(CivicDataInputRoute)
                            },
                            onNavigateToLogin = {
                                backStack.removeLast()
                            }
                        )
                    }
                    
                    entry<OTPVerificationRoute> { key ->
                        OTPVerificationScreen(
                            userId = key.userId,
                            email = key.email,
                            purpose = key.purpose,
                            authRepository = authRepository,
                            onVerificationSuccess = { resetToken ->
                                if (key.purpose == "email_verification") {
                                    // Go to civic data input after email verification
                                    backStack.add(CivicDataInputRoute)
                                } else {
                                    // Go to reset password after password reset OTP
                                    backStack.add(ResetPasswordRoute(resetToken!!))
                                }
                            },
                            onBackClick = {
                                backStack.removeLast()
                            }
                        )
                    }
                    
                    entry<CivicDataInputRoute> {
                        CivicDataInputScreen(
                            onComplete = {
                                // Clear backstack and go to Home
                                backStack.clear()
                                backStack.add(HomeRoute)
                            },
                            onSkip = {
                                // Skip civic data and go to Home
                                backStack.clear()
                                backStack.add(HomeRoute)
                            }
                        )
                    }
                    
                    entry<ResetPasswordRoute> { key ->
                        ResetPasswordScreen(
                            resetToken = key.resetToken,
                            onResetSuccess = {
                                // Navigate back to login
                                // Clear auth flow stack
                                while (backStack.size > 1 && backStack.lastOrNull() !is LoginRoute) {
                                    backStack.removeLast()
                                }
                                if (backStack.lastOrNull() !is LoginRoute) {
                                    backStack.add(LoginRoute)
                                }
                            },
                            onBackClick = {
                                backStack.removeLast()
                            }
                        )
                    }

                    // Home Tab
                    entry<HomeRoute> {
                        HomeScreen(
                            onClauseCardClick = {
                                backStack.add(ClauseDetailRoute("today"))
                            },
                            onAIDescriptionCardClick = {
                                backStack.add(AIDescriptionRoute("today"))
                            },
                            onTipsCardClick = {
                                backStack.add(TipsRoute("today"))
                            },
                            onNotificationsClick = {
                                backStack.add(NotificationsRoute)
                            },
                            onMzalendoClick = {
                                backStack.add(MzalendoRoute)
                            },
                            onResumeLesson = { lessonId ->
                                backStack.add(LessonRoute(lessonId))
                            }
                        )
                    }

                    entry<MzalendoRoute> {
                        MzalendoScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<NotificationsRoute> {
                        NotificationsScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<ClauseDetailRoute> { key ->
                        ClauseDetailScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<AIDescriptionRoute> { key ->
                        AIDescriptionScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<TipsRoute> { key ->
                        TipsScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    // Constitution Tab
                    entry<ConstitutionRoute> {
                        ConstitutionScreen(
                            onChapterClick = { chapterNumber ->
                                backStack.add(ClauseGridRoute(chapterNumber))
                            },
                            onPreambleClick = {
                                backStack.add(PreambleRoute)
                            },
                            onSchedulesClick = {
                                backStack.add(SchedulesRoute)
                            }
                        )
                    }

                    entry<PreambleRoute> {
                        PreambleScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<ClauseGridRoute> { key ->
                        ClauseGridScreen(
                            chapterNumber = key.chapterNumber,
                            onBackClick = { backStack.removeLast() },
                            onArticleClick = { chapterNum, articleNum ->
                                backStack.add(ReadingRoute(chapterNum, articleNum))
                            }
                        )
                    }

                    entry<ReadingRoute> { key ->
                        ReadingScreen(
                            chapterNumber = key.chapterNumber,
                            articleNumber = key.articleNumber,
                            onBackClick = { backStack.removeLast() },
                            onNavigateToArticle = { chapterNum, articleNum ->
                                // Replace current reading route with new one
                                backStack.removeLast()
                                backStack.add(ReadingRoute(chapterNum, articleNum))
                            }
                        )
                    }

                    // Plans Tab
                    entry<PlansRoute> {
                        PlansScreen(
                            onLessonClick = { lessonId ->
                                backStack.add(LessonRoute(lessonId))
                            }
                        )
                    }

                    entry<LessonRoute> { key ->
                        LessonScreen(
                            lessonId = key.lessonId,
                            onBackClick = { backStack.removeLast() },
                            onCompleteLesson = {
                                // Return to plans after completion
                                backStack.removeLast()
                            }
                        )
                    }

                    entry<SchedulesRoute> {
                        SchedulesScreen(
                            onScheduleClick = { scheduleNumber ->
                                backStack.add(ScheduleDetailRoute(scheduleNumber))
                            },
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<ScheduleDetailRoute> { key ->
                        ScheduleDetailScreen(
                            scheduleNumber = key.scheduleNumber,
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    // Plans Tab
                    entry<ProfileRoute> {
                        ProfileScreen(
                            authRepository = authRepository,
                            googleSignInService = googleSignInService,
                            onSettingsClick = {
                                backStack.add(SettingsRoute)
                            },
                            onResumeLesson = { lessonId ->
                                backStack.add(LessonRoute(lessonId))
                            }
                        )
                    }

                    entry<SettingsRoute> {
                        SettingsScreen(
                            onBackClick = { backStack.removeLast() },
                            onSignOut = {
                                coroutineScope.launch {
                                    authRepository.signOut()
                                    // Clear entire backstack and navigate to LoginRoute
                                    backStack.clear()
                                    backStack.add(LoginRoute)
                                }
                            },
                            onEditProfile = { backStack.add(EditProfileRoute) },
                            onPasswordSecurity = { backStack.add(PasswordSecurityRoute) },
                            onUpdateResidence = { backStack.add(UpdateResidenceRoute) },
                            onNationalID = { backStack.add(NationalIDRoute) },
                            onAppearance = { backStack.add(AppearanceRoute) },
                            onFontSize = { backStack.add(FontSizeRoute) },
                            onLanguage = { backStack.add(LanguageRoute) },
                            onAboutKatiba = { backStack.add(AboutKatibaRoute) },
                            onSendFeedback = { backStack.add(SendFeedbackRoute) }
                        )
                    }

                    // Settings Edit Screens
                    entry<EditProfileRoute> {
                        EditProfileScreen(
                            onBackClick = { backStack.removeLast() },
                            onSave = { _, _ -> backStack.removeLast() }
                        )
                    }

                    entry<PasswordSecurityRoute> {
                        PasswordSecurityScreen(
                            onBackClick = { backStack.removeLast() },
                            onChangePassword = { _, _ -> backStack.removeLast() }
                        )
                    }

                    entry<UpdateResidenceRoute> {
                        UpdateResidenceScreen(
                            onBackClick = { backStack.removeLast() },
                            onSave = { _, _, _ -> backStack.removeLast() }
                        )
                    }

                    entry<NationalIDRoute> {
                        NationalIDScreen(
                            onBackClick = { backStack.removeLast() },
                            onSave = { _ -> backStack.removeLast() }
                        )
                    }

                    entry<AppearanceRoute> {
                        AppearanceScreen(
                            onBackClick = { backStack.removeLast() },
                            onThemeChange = { _ -> }
                        )
                    }

                    entry<FontSizeRoute> {
                        FontSizeScreen(
                            onBackClick = { backStack.removeLast() },
                            onFontSizeChange = { _ -> }
                        )
                    }

                    entry<LanguageRoute> {
                        LanguageScreen(
                            onBackClick = { backStack.removeLast() },
                            onLanguageChange = { _ -> }
                        )
                    }

                    entry<AboutKatibaRoute> {
                        AboutKatibaScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<SendFeedbackRoute> {
                        SendFeedbackScreen(
                            onBackClick = { backStack.removeLast() },
                            onSubmit = { _ -> backStack.removeLast() }
                        )
                    }
                }
            )
        }
}

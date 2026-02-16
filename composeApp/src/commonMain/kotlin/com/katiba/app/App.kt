package com.katiba.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.katiba.app.ui.theme.KatibaTheme
import katiba.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
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
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
        // Navigation 3 Style: You own the backstack as a mutable list
        val backStack = rememberNavBackStack(OnboardingRoute)

        // Helper to get current route from backstack
        val currentKey = backStack.lastOrNull()

        // Handle system back button/swipe navigation
        BackHandler(enabled = backStack.size >= 1) {
            if (backStack.size <= 1) {
                // On the last screen — navigate to Home instead of exiting
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

        // Determine if bottom bar should be shown
        val showBottomBar = remember(currentKey) {
            currentKey is HomeRoute ||
            currentKey is ConstitutionRoute ||
            currentKey is PlansRoute ||
            currentKey is ProfileRoute
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
                                backStack.add(LoginRoute)
                            }
                        )
                    }

                    // Auth screens
                    entry<LoginRoute> {
                        LoginScreen(
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
                            onSignUpSuccess = { userId, email ->
                                // Navigate to OTP verification
                                backStack.add(OTPVerificationRoute(userId, email, "email_verification"))
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

                    entry<ReadingRoute> { key ->
                        ReadingScreen(
                            chapterNumber = key.chapterNumber,
                            articleNumber = key.articleNumber,
                            onBackClick = { backStack.removeLast() },
                            onNavigateToArticle = { chapter, article ->
                                backStack.add(ReadingRoute(chapter, article))
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
                            onSettingsClick = {
                                backStack.add(SettingsRoute)
                            }
                        )
                    }

                    entry<SettingsRoute> {
                        SettingsScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }
                }
            )
        }
}

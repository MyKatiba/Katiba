package com.katiba.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.katiba.app.ui.constitution.ClauseGridScreen
import com.katiba.app.ui.constitution.ConstitutionScreen
import com.katiba.app.ui.constitution.ReadingScreen
import com.katiba.app.ui.home.*
import com.katiba.app.ui.navigation.*
import com.katiba.app.ui.plans.LessonScreen
import com.katiba.app.ui.plans.PlansScreen
import com.katiba.app.ui.profile.ProfileScreen
import com.katiba.app.ui.profile.SettingsScreen
import com.katiba.app.ui.theme.KatibaTheme

@Composable
fun App() {
    KatibaTheme {
        // Navigation 3 Style: You own the backstack as a mutable list
        val backStack = rememberNavBackStack(HomeRoute)

        // Helper to get current route from backstack
        val currentKey = backStack.lastOrNull()

        // Determine current tab based on the current key
        val currentTab = remember(currentKey) {
            when (currentKey) {
                is HomeRoute,
                is ClauseDetailRoute,
                is AIDescriptionRoute,
                is TipsRoute -> BottomNavTab.HOME

                is ConstitutionRoute,
                is ChapterListRoute,
                is ClauseGridRoute,
                is ReadingRoute -> BottomNavTab.KATIBA

                is PlansRoute,
                is LessonRoute -> BottomNavTab.PLANS

                is ProfileRoute,
                is SettingsRoute -> BottomNavTab.PROFILE

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
                            }
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
                            }
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
                            onCompleteLesson = { backStack.removeLast() }
                        )
                    }

                    // Profile Tab
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
}

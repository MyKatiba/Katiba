package com.katiba.app.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.katiba.app.data.model.Badge
import com.katiba.app.data.model.UserProfile
import com.katiba.app.data.repository.ArticleReadManager
import com.katiba.app.data.repository.BookmarkManager
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.data.repository.StreakManager
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.bracelet
import org.jetbrains.compose.resources.painterResource
import com.katiba.app.data.repository.AuthRepository
import com.katiba.app.data.service.GoogleSignInService
import com.katiba.app.ui.auth.LoginScreen
import com.katiba.app.ui.auth.SignUpScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.*

/**
 * Profile screen with redesigned layout matching the Kenyan civic app design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    googleSignInService: GoogleSignInService? = null,
    onSettingsClick: () -> Unit,
    onResumeLesson: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by authRepository.currentUser.collectAsState(initial = null)
    var isLoginMode by remember { mutableStateOf(true) }

    // If no user is logged in, show Auth screens
    if (currentUser == null) {
        if (isLoginMode) {
            LoginScreen(
                authRepository = authRepository,
                googleSignInService = googleSignInService,
                onLoginSuccess = { /* Profile will automatically update due to currentUser observation */ },
                onNavigateToSignUp = { isLoginMode = false },
                onNavigateToForgotPassword = { /* TODO: Navigate to global forgot password route or show dialog */ }
            )
        } else {
            SignUpScreen(
                authRepository = authRepository,
                googleSignInService = googleSignInService,
                onSignUpSuccess = { _, _ -> /* Profile will automatically update */ },
                onNavigateToLogin = { isLoginMode = true }
            )
        }
        return
    }

    // Map AuthUser to UserProfile (combining real auth data with sample stats for now)
    val userProfile = remember(currentUser) {
        val sample = SampleDataRepository.getUserProfile()
        UserProfile(
            id = currentUser!!.id,
            name = currentUser!!.name.ifEmpty { "Mzalendo" },
            email = currentUser!!.email,
            avatarUrl = currentUser!!.avatarUrl,
            emailVerified = currentUser!!.emailVerified,
            joinedDate = if (currentUser!!.joinedDate.isNotEmpty()) currentUser!!.joinedDate else "2024",
            badges = sample.badges,
            // Use sample data for stats until connected to backend
            streak = sample.streak,
            longestStreak = sample.longestStreak,
            totalLessonsCompleted = sample.totalLessonsCompleted,
            xp = sample.xp,
            // Use defaults or sample for civic data
            county = sample.county,
            constituency = sample.constituency,
            ward = sample.ward,
            isRegisteredVoter = sample.isRegisteredVoter,
            nationalId = sample.nationalId
        )
    }
    
    val lessons = remember { SampleDataRepository.getLessons() }
    var showCivicDataSheet by remember { mutableStateOf(false) }
    var showStreakSheet by remember { mutableStateOf(false) }
    var showAchievementsSheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val achievementsSheetState = rememberModalBottomSheetState()

    // Initialize streak on first composition
    var streakCount by remember { mutableStateOf(0) }
    var bestStreak by remember { mutableStateOf(0) }
    var articlesReadCount by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        streakCount = StreakManager.checkAndUpdateStreak()
        bestStreak = StreakManager.getBestStreak()
        articlesReadCount = ArticleReadManager.getArticlesReadCount()
    }

    // Calculate current course progress
    val currentChapter = 4 // Bill of Rights
    val chapterLessons = lessons.filter { it.chapterNumber == currentChapter }
    val completedLessons = chapterLessons.count { it.isCompleted }
    val totalLessons = chapterLessons.size
    val nextLesson = chapterLessons.find { !it.isCompleted }
    // Use a shortened lesson title for display (e.g., "Right to Life" instead of full title)
    val nextLessonDisplay = nextLesson?.title?.removePrefix("Your ")?.removePrefix("The ") ?: "Complete!"

    val scrollState = rememberScrollState()
    val headerAlpha by animateFloatAsState(
        targetValue = if (scrollState.value > 40) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "profileHeaderAlpha"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 72.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            ProfileCard(userProfile = userProfile)

            // Stats Cards (Day Streak & Articles Read)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StreakStatCard(
                    streakCount = streakCount,
                    onClick = { showStreakSheet = true },
                    modifier = Modifier.weight(1f)
                )
                ArticlesReadStatCard(
                    articlesCount = articlesReadCount,
                    modifier = Modifier.weight(1f)
                )
            }

            // Achievements Section
            AchievementsCard(
                badges = userProfile.badges,
                onClick = { showAchievementsSheet = true }
            )

            // Bookmarks Section
            BookmarksCard(
                onClick = { showBookmarksSheet = true }
            )

            // Current Course Section
            CurrentCourseCard(
                chapterNumber = currentChapter,
                chapterTitle = "The Bill of Rights",
                completedLessons = completedLessons,
                totalLessons = totalLessons,
                nextLessonTitle = nextLessonDisplay,
                onContinueClick = {
                    nextLesson?.let { lesson ->
                        onResumeLesson(lesson.id)
                    }
                }
            )

            // My Civic Data Section
            CivicDataCard(
                userProfile = userProfile,
                onClick = { showCivicDataSheet = true }
            )
        }

        // Floating header
        TopAppBar(
            title = {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            windowInsets = WindowInsets(0.dp),
            modifier = Modifier.align(Alignment.TopCenter)
        )
        } // end Box
    }
    
    // Civic Data Bottom Sheet
    if (showCivicDataSheet) {
        CivicDataBottomSheet(
            userProfile = userProfile,
            onDismiss = { showCivicDataSheet = false }
        )
    }

    // Streak Bottom Sheet
    if (showStreakSheet) {
        StreakBottomSheet(
            sheetState = sheetState,
            onDismiss = { showStreakSheet = false },
            currentStreak = streakCount,
            bestStreak = bestStreak
        )
    }
    
    // Achievements Bottom Sheet
    if (showAchievementsSheet) {
        AchievementsBottomSheet(
            sheetState = achievementsSheetState,
            badges = userProfile.badges,
            onDismiss = { showAchievementsSheet = false }
        )
    }

    // Bookmarks Bottom Sheet
    if (showBookmarksSheet) {
        BookmarksBottomSheet(
            onDismiss = { showBookmarksSheet = false }
        )
    }
}

@Composable
private fun BeadworkBorder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
    ) {
        // Create repeating pattern of Black, Red, Green
        repeat(30) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        when (it % 3) {
                            0 -> KatibaColors.KenyaBlack
                            1 -> KatibaColors.KenyaRed
                            else -> Color(0xFF009900)
                        }
                    )
            )
        }
    }
}

@Composable
private fun ProfileCard(userProfile: UserProfile) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Surface variant background with abstract pale patterns
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(surfaceVariantColor) // Theme-aware surface variant
                    .clip(RoundedCornerShape(24.dp))
                    .drawBehind {
                        // Draw pale abstract circles
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = 120.dp.toPx(),
                            center = Offset(size.width * 0.9f, size.height * 0.8f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = 80.dp.toPx(),
                            center = Offset(size.width * 0.15f, size.height * 0.25f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.4f),
                            radius = 60.dp.toPx(),
                            center = Offset(size.width * 0.55f, size.height * 0.15f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.35f),
                            radius = 45.dp.toPx(),
                            center = Offset(size.width * 0.75f, size.height * 0.45f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = 35.dp.toPx(),
                            center = Offset(size.width * 0.3f, size.height * 0.7f)
                        )
                        // Add some wave-like patterns
                        drawCircle(
                            color = Color.White.copy(alpha = 0.25f),
                            radius = 100.dp.toPx(),
                            center = Offset(size.width * 0.0f, size.height * 0.6f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.2f),
                            radius = 70.dp.toPx(),
                            center = Offset(size.width * 1.0f, size.height * 0.2f)
                        )
                    }
            )

            // Avatar and user info positioned in Row at left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier.padding(end = 15.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .shadow(8.dp, CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE8D5B7),
                                        Color(0xFFD4A574)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Placeholder avatar - shows initials
                        Text(
                            text = userProfile.name.split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString(""),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B4513)
                        )
                    }
                }

                // Name and citizen since text
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userProfile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "User Since 2026",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}



@Composable
private fun StatCard(
    icon: String,
    iconBackgroundColor: Color,
    value: String,
    label: String,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left border accent
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(borderColor)
                    .align(Alignment.CenterStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Value
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Label
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun StreakStatCard(
    streakCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left border accent
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(KatibaColors.KenyaRed)
                    .align(Alignment.CenterStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top row: Icon and Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Icon - Bolt icon with yellow-50 background
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEFCE8)), // Yellow-50
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (streakCount > 0) BoltIconFilled else BoltIconOutline,
                            contentDescription = "Streak",
                            tint = if (streakCount > 0) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Value on top right
                    Text(
                        text = streakCount.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Label
                Text(
                    text = "DAY STREAK",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun ArticlesReadStatCard(
    articlesCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left border accent
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(KatibaColors.KenyaGreen)
                    .align(Alignment.CenterStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top row: Icon and Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Icon - Book icon with grey-50 background
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = BookIcon,
                            contentDescription = "Articles Read",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Value on top right
                    Text(
                        text = articlesCount.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Label
                Text(
                    text = "ARTICLES READ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// Book icon for articles read card
private val BookIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Book",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            // Book spine on left
            moveTo(4f, 19.5f)
            verticalLineTo(4.5f)
            arcTo(2f, 2f, 0f, false, true, 6f, 2.5f)
            horizontalLineTo(20f)
            verticalLineTo(22f)
            horizontalLineTo(6f)
            arcTo(2f, 2f, 0f, false, true, 4f, 20f)
            verticalLineTo(19.5f)
            close()
            // Book page fold
            moveTo(4f, 19.5f)
            horizontalLineTo(20f)
        }
    }.build()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreakBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    currentStreak: Int,
    bestStreak: Int
) {
    val dailyRefreshStreak = remember { StreakManager.getDailyRefreshStreak() }
    val bestDailyRefreshStreak = remember { StreakManager.getBestDailyRefreshStreak() }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Info, contentDescription = null)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Share, contentDescription = null)
                }
            }

            Text(
                text = "Streaks help you build your daily Katiba habit.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (currentStreak > 0) BoltIconFilled else BoltIconOutline,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentStreak > 0) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currentStreak",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentStreak > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("App Streak", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (bestStreak > 0) BoltIconFilled else BoltIconOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (bestStreak > 0) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$bestStreak Best",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bestStreak > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(modifier = Modifier.width(1.dp).height(80.dp).background(MaterialTheme.colorScheme.outlineVariant))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (dailyRefreshStreak > 0) KatibaColors.KenyaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$dailyRefreshStreak",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dailyRefreshStreak > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("Daily Refresh Streak", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (bestDailyRefreshStreak > 0) KatibaColors.KenyaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$bestDailyRefreshStreak Best",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bestDailyRefreshStreak > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (currentStreak > 0) {
                Text(
                    text = when {
                        currentStreak == 1 -> "Just getting started!"
                        currentStreak < 7 -> "$currentStreak days in a row"
                        currentStreak < 30 -> "${currentStreak / 7} ${if (currentStreak / 7 == 1) "week" else "weeks"} in a row"
                        else -> "${currentStreak / 30} ${if (currentStreak / 30 == 1) "month" else "months"} in a row"
                    },
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$currentStreak ${if (currentStreak == 1) "day" else "days"} in the Katiba App",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("Start your streak today!", fontWeight = FontWeight.Bold)
                Text("Open the app daily to build your habit", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Week calendar with real streak data
            val weekData = remember { StreakManager.getWeekStreakData() }
            val todayString = remember { 
                val now = Clock.System.now()
                now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekData.forEach { (dateStr, streakData) ->
                    val date = dateStr.substringAfterLast("-")
                    val dayOfWeek = remember(dateStr) {
                        val parts = dateStr.split("-")
                        val localDate = kotlinx.datetime.LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                        val dayIndex = localDate.dayOfWeek.ordinal
                        listOf("M", "T", "W", "T", "F", "S", "S")[dayIndex]
                    }
                    val isToday = dateStr == todayString
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayOfWeek, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .let { 
                                    if (isToday) it.border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else it
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(date)
                            // Red dot for app streak
                            if (streakData.hasAppStreak) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(6.dp)
                                        .background(KatibaColors.KenyaRed, CircleShape)
                                )
                            }
                            // Grey drop marker for daily refresh (bottom left)
                            if (streakData.hasDailyRefresh) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .size(5.dp)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Bolt icon filled (for streak > 0)
private val BoltIconFilled: ImageVector
    get() = ImageVector.Builder(
        name = "BoltFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(13f, 3f)
            lineTo(13f, 10f)
            lineTo(19f, 10f)
            lineTo(11f, 21f)
            lineTo(11f, 14f)
            lineTo(5f, 14f)
            lineTo(13f, 3f)
            close()
        }
    }.build()

// Bolt icon outline (for streak = 0)
private val BoltIconOutline: ImageVector
    get() = ImageVector.Builder(
        name = "BoltOutline",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(13f, 3f)
            lineTo(13f, 10f)
            lineTo(19f, 10f)
            lineTo(11f, 21f)
            lineTo(11f, 14f)
            lineTo(5f, 14f)
            lineTo(13f, 3f)
            close()
        }
    }.build()

@Composable
private fun AchievementsCard(
    badges: List<Badge>,
    onClick: () -> Unit
) {
    val earnedCount = badges.count { it.isEarned }
    val totalCount = badges.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$earnedCount of $totalCount earned",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (badges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Show up to 4 badge previews
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    badges.take(4).forEach { badge ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(56.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (badge.isEarned) KatibaColors.BeadGold.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = badge.iconUrl.ifEmpty { badge.name.take(1) },
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = badge.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (badge.isEarned) MaterialTheme.colorScheme.onSurface
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (earnedCount == 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Complete lessons and milestones to earn achievements",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Complete lessons and milestones to earn achievements",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AchievementItem(
    icon: String,
    name: String,
    isEarned: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isEarned) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFDE047),
                                Color(0xFFF59E0B)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE5E7EB),
                                Color(0xFF9CA3AF)
                            )
                        )
                    }
                )
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .then(
                    if (!isEarned) Modifier else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 28.sp,
                modifier = if (!isEarned) Modifier else Modifier
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isEarned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AchievementsBottomSheet(
    sheetState: SheetState,
    badges: List<Badge>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 Your Achievements",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Unlock badges by completing civic learning milestones",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Empty state - achievements will be implemented
            Text(
                text = "Complete lessons and milestones to earn achievements!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CurrentCourseCard(
    chapterNumber: Int,
    chapterTitle: String,
    completedLessons: Int,
    totalLessons: Int,
    nextLessonTitle: String,
    onContinueClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Current Course",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Chapter number box
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chapterNumber.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }

                // Course info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Chapter $chapterNumber: $chapterTitle",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$completedLessons of $totalLessons lessons completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val progress = if (totalLessons > 0) {
                            completedLessons.toFloat() / totalLessons.toFloat()
                        } else 0f

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(5.dp))
                                .background(KatibaColors.KenyaGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(16.dp))

            // Next lesson row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next Lesson: $nextLessonTitle",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onContinueClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KatibaColors.KenyaRed
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CivicDataCard(
    userProfile: UserProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Civic Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Arrow indicator
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "View details",
                    tint = KatibaColors.KenyaGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            CivicDataRow(
                label = "County",
                value = userProfile.county.ifEmpty { "Not set" },
                showDivider = true
            )

            CivicDataRow(
                label = "Constituency",
                value = userProfile.constituency.ifEmpty { "Not set" },
                showDivider = true
            )

            CivicDataRow(
                label = "Ward",
                value = userProfile.ward.ifEmpty { "Not set" },
                showDivider = false
            )
        }
    }
}

@Composable
private fun CivicDataRow(
    label: String,
    value: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CivicDataBottomSheet(
    userProfile: UserProfile,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Civic Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // National ID
            DetailRow(
                label = "National ID",
                value = userProfile.nationalId.ifEmpty { "Not set" }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // County
            DetailRow(
                label = "County",
                value = userProfile.county.ifEmpty { "Not set" }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Constituency
            DetailRow(
                label = "Constituency",
                value = userProfile.constituency.ifEmpty { "Not set" }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ward
            DetailRow(
                label = "Ward",
                value = userProfile.ward.ifEmpty { "Not set" }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Voter Status Badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (userProfile.isRegisteredVoter) {
                        KatibaColors.KenyaGreen.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Voter Status",
                            tint = if (userProfile.isRegisteredVoter) KatibaColors.KenyaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Voter Registration Status",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (userProfile.isRegisteredVoter) "Registered Voter" else "Not Registered",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.isRegisteredVoter) KatibaColors.KenyaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (userProfile.isRegisteredVoter) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(KatibaColors.KenyaGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BookmarksCard(
    onClick: () -> Unit
) {
    // Use mutableIntStateOf so count refreshes on recomposition
    val bookmarkCount = BookmarkManager.getBookmarkCount()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bookmarks",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (bookmarkCount > 0) "$bookmarkCount saved clause${if (bookmarkCount != 1) "s" else ""}" else "No saved clauses yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = BookmarkIconVector,
                contentDescription = null,
                tint = KatibaColors.BeadGold,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// Bookmark icon for the BookmarksCard
private val BookmarkIconVector: ImageVector
    get() = ImageVector.Builder(
        name = "BookmarkOutline",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 3f)
            horizontalLineTo(7f)
            curveTo(5.9f, 3f, 5f, 3.9f, 5f, 5f)
            verticalLineTo(21f)
            lineTo(12f, 18f)
            lineTo(19f, 21f)
            verticalLineTo(5f)
            curveTo(19f, 3.9f, 18.1f, 3f, 17f, 3f)
            close()
        }
    }.build()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var bookmarks by remember { mutableStateOf(BookmarkManager.getBookmarks()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Bookmarks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔖", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No bookmarks yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Save clauses of the day to view them here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                bookmarks.forEach { bookmark ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chapter ${bookmark.chapterNumber} • ${bookmark.chapterTitle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KatibaColors.KenyaGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Article ${bookmark.articleNumber} - ${bookmark.articleTitle}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${bookmark.clauseText}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    BookmarkManager.removeBookmark(bookmark.id)
                                    bookmarks = BookmarkManager.getBookmarks()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Remove bookmark",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

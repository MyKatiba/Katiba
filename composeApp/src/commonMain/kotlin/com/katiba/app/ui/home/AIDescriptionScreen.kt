package com.katiba.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.data.repository.StreakManager
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.kenya_shield
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

/**
 * Multi-page detail view for Deep Dive content
 * Page 0: Clause Details (moved from ClauseDetailScreen)
 * Page 1: Video Lesson
 * Page 2-3: Inspirational/Encouragement placeholder pages
 */
@Composable
fun AIDescriptionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyContent = remember { SampleDataRepository.getDailyContent() }
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    
    // Track if user has reached the final page
    var hasReachedFinalPage by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var showDoneButton by remember { mutableStateOf(false) }
    
    // Trigger celebration when reaching page 3
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 3 && !hasReachedFinalPage) {
            hasReachedFinalPage = true
            showCelebration = true
            delay(1000) // Show fireworks for 1 second
            showCelebration = false
            showDoneButton = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                // Use dark background for pages 2-3, light for 0-1
                if (pagerState.currentPage >= 2) Color(0xFF2D2D2D)
                else MaterialTheme.colorScheme.background
            )
    ) {
        // Main content pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> ClauseDetailPage(
                    dailyContent = dailyContent
                )
                1 -> VideoPage(
                    videoUrl = dailyContent.videoUrl,
                    educatorName = dailyContent.educatorName,
                    articleTitle = dailyContent.articleTitle
                )
                2 -> InspirationPage(
                    label = "Be Encouraged",
                    quote = "Every citizen has the right of access to information held by the State.",
                    source = "Constitution of Kenya, 2010"
                )
                3 -> InspirationPage(
                    label = "Did You Know?",
                    quote = "The Bill of Rights is an integral part of Kenya's democratic state and is the framework for social, economic, and cultural policies.",
                    source = "Constitution of Kenya, 2010"
                )
            }
        }

        // Minimal top bar with only story progress indicators (height = 10dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
        ) {
            // Story-style progress indicators
            StoryProgressIndicator(
                pageCount = 4,
                currentPage = pagerState.currentPage,
                isDarkTheme = pagerState.currentPage >= 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // Show Done button on final page after celebration
        if (pagerState.currentPage == 3 && showDoneButton) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val offset by animateDpAsState(
                targetValue = if (isPressed) 4.dp else 0.dp,
                label = "button press"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 48.dp)
            ) {
                // Shadow box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .offset(y = 4.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF1A1A1A))
                )
                
                // Done button with physical UI
                Button(
                    onClick = {
                        // Record daily refresh completion
                        StreakManager.recordDailyRefreshCompletion()
                        // Navigate back to home
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .offset(y = offset),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    interactionSource = interactionSource,
                    border = BorderStroke(2.dp, Color(0xFF2D2D2D))
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                }
            }
        }
        
        // Fireworks celebration animation
        if (showCelebration) {
            FireworksAnimation(
                modifier = Modifier.fillMaxSize()
            )
        }

        // Tap zones for pages 0-1
        if (pagerState.currentPage < 2) {
            // Left tap zone for previous page
            if (pagerState.currentPage > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(80.dp)
                        .align(Alignment.CenterStart)
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                )
            }

            // Right tap zone for next page
            if (pagerState.currentPage < 3) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(80.dp)
                        .align(Alignment.CenterEnd)
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                )
            }
        }
    }
}

/**
 * Instagram/Story-style horizontal segmented progress indicator
 */
@Composable
private fun StoryProgressIndicator(
    pageCount: Int,
    currentPage: Int,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(pageCount) { index ->
            val isActive = index <= currentPage

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        when {
                            isActive -> KatibaColors.KenyaRed
                            isDarkTheme -> Color.Gray.copy(alpha = 0.4f)
                            else -> Color.Gray.copy(alpha = 0.4f)
                        }
                    )
            )
        }
    }
}

/**
 * Fireworks celebration animation
 */
@Composable
private fun FireworksAnimation(
    modifier: Modifier = Modifier
) {
    // Create multiple particles
    val particleCount = 30
    val particles = remember {
        List(particleCount) {
            FireworkParticle(
                startX = Random.nextFloat(),
                startY = 0.5f,
                targetX = Random.nextFloat(),
                targetY = Random.nextFloat(),
                color = listOf(
                    Color(0xFFFFD700), // Gold
                    Color(0xFFFF6B6B), // Red
                    KatibaColors.KenyaGreen,
                    Color(0xFF4ECDC4),  // Cyan
                    Color(0xFFFFBE0B)   // Yellow
                ).random()
            )
        }
    }
    
    Box(modifier = modifier) {
        particles.forEach { particle ->
            var animationProgress by remember { mutableStateOf(0f) }
            
            LaunchedEffect(Unit) {
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                ) { value, _ ->
                    animationProgress = value
                }
            }
            
            val currentX = particle.startX + (particle.targetX - particle.startX) * animationProgress
            val currentY = particle.startY + (particle.targetY - particle.startY) * animationProgress
            val alpha = 1f - animationProgress
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
                    .offset(
                        x = (currentX * 1000).dp,
                        y = (currentY * 1000).dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size((8 * (1f - animationProgress * 0.5f)).dp)
                        .background(particle.color.copy(alpha = alpha), CircleShape)
                )
            }
        }
    }
}

private data class FireworkParticle(
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    val color: Color
)

/**
 * Page 0: Clause Detail Page (moved from ClauseDetailScreen)
 */
@Composable
private fun ClauseDetailPage(
    dailyContent: com.katiba.app.data.model.DailyContent
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Kenya shield watermark in bottom right, tilted 30 degrees left
        Image(
            painter = painterResource(Res.drawable.kenya_shield),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .rotate(-30f)
                .alpha(0.08f),
            contentScale = ContentScale.Fit
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 100.dp) // Account for top bar
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // "CLAUSE OF THE DAY" pill badge
            Surface(
                color = KatibaColors.KenyaRed,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "CLAUSE OF THE DAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Clause card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Article number and bookmark row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${dailyContent.articleNumber}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Light,
                            color = Color(0xFFD3D3D3) // Light grey
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Bookmark",
                            tint = Color(0xFFC0C0C0),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quote text
                    Text(
                        text = "\"${dailyContent.clause.text}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Start
                    )

                    // Sub-clauses if any
                    if (dailyContent.clause.subClauses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        dailyContent.clause.subClauses.forEach { subClause ->
                            Text(
                                text = "(${subClause.label}) ${subClause.text}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black,
                                lineHeight = 28.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Small decorative line (Kenya flag colors)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(3.dp)
                                .background(KatibaColors.KenyaBlack)
                        )
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(3.dp)
                                .background(KatibaColors.KenyaRed)
                        )
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(3.dp)
                                .background(KatibaColors.KenyaGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Constitution source
                    Text(
                        text = "Constitution of Kenya, 2010",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Understanding this clause section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Yellow lightbulb icon
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFFD700), // Bright yellow/gold
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Understanding this clause",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI description content
                Text(
                    text = dailyContent.aiDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun VideoPage(
    videoUrl: String,
    educatorName: String,
    articleTitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Kenya shield watermark
        Image(
            painter = painterResource(Res.drawable.kenya_shield),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .rotate(-30f)
                .alpha(0.08f),
            contentScale = ContentScale.Fit
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp) // Reduced for minimal header
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
        // Video player placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(16.dp))
                .background(KatibaColors.KenyaBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Play button
                Surface(
                    color = KatibaColors.KenyaRed,
                    shape = RoundedCornerShape(50)
                ) {
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Video Lesson",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = articleTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Educator info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(KatibaColors.KenyaGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = educatorName.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = educatorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Civic Educator",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Pages 2-3: Inspirational/Encouragement pages with dark theme
 * Design inspired by the reference image
 */
@Composable
private fun InspirationPage(
    label: String,
    quote: String,
    source: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D2D2D))
            .padding(top = 60.dp) // Reduced for minimal header
    ) {
        // Kenya shield watermark
        Image(
            painter = painterResource(Res.drawable.kenya_shield),
            contentDescription = null,
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .rotate(-30f)
                .alpha(0.05f),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quote
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Normal,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Source
            Text(
                text = "— $source",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                fontStyle = FontStyle.Italic
            )
        }
    }
}

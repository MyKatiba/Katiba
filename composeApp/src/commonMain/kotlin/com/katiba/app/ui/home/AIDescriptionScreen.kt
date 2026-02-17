package com.katiba.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.kenya_shield
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

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

        // Top bar with story progress indicators
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    if (pagerState.currentPage >= 2) Color(0xFF2D2D2D).copy(alpha = 0.95f)
                    else MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
                .statusBarsPadding()
                .padding(top = 8.dp)
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

            // Close button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = if (pagerState.currentPage >= 2) Color.White
                               else MaterialTheme.colorScheme.onBackground
                    )
                }

                // Page title
                Text(
                    text = when (pagerState.currentPage) {
                        0 -> "Deep Dive"
                        1 -> "Video Lesson"
                        2 -> "Inspiration"
                        else -> "Did You Know?"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (pagerState.currentPage >= 2) Color.White
                            else MaterialTheme.colorScheme.onBackground
                )

                // Spacer for symmetry
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Bottom navigation arrows (visible on pages 2-3)
        if (pagerState.currentPage >= 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left arrow
                IconButton(
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage > 0) {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A4A4A))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        tint = Color.White
                    )
                }

                // Right arrow
                IconButton(
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage < 3) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A4A4A))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
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
                            isActive && isDarkTheme -> Color.White
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
        // Kenya shield watermark in bottom right, tilted 45 degrees
        Image(
            painter = painterResource(Res.drawable.kenya_shield),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
                .rotate(-45f)
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

            // Article title
            Text(
                text = dailyContent.articleTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            // Chapter subtitle
            Text(
                text = "${dailyContent.chapterTitle}, Part ${dailyContent.articleNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Clause card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFAFAFA),
                border = BorderStroke(1.dp, Color(0xFFE8E8E8))
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
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Light,
                            color = Color.Black
                        )
                        IconButton(
                            onClick = { /* Bookmark action */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Save",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quote with decorative line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        // Decorative vertical line
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(KatibaColors.KenyaRed)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\"${dailyContent.clause.text}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = Color.Black,
                                lineHeight = 28.sp
                            )

                            // Sub-clauses if any
                            if (dailyContent.clause.subClauses.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                dailyContent.clause.subClauses.forEach { subClause ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "(${subClause.label})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = KatibaColors.KenyaGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.width(28.dp)
                                        )
                                        Text(
                                            text = subClause.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Black.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Constitution source
                    Text(
                        text = "Constitution of Kenya, 2010",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 19.dp)
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
                    // Green bullet indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(KatibaColors.KenyaGreen)
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 100.dp) // Account for top bar
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
            .padding(top = 100.dp) // Account for top bar
    ) {
        // Kenya shield watermark
        Image(
            painter = painterResource(Res.drawable.kenya_shield),
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .alpha(0.05f),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(bottom = 100.dp), // Account for bottom nav arrows
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

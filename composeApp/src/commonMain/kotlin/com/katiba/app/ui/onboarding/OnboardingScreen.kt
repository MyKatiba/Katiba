package com.katiba.app.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.onboarding_screen1
import katiba.composeapp.generated.resources.onboarding_screen2
import katiba.composeapp.generated.resources.onboarding_screen3
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Data class representing a single onboarding page
 */
private data class OnboardingPage(
    val backgroundImage: DrawableResource,
    val title: String,
    val description: String,
    val accentColor: Color
)

private val onboardingPages = listOf(
    OnboardingPage(
        backgroundImage = Res.drawable.onboarding_screen1,
        title = "Know Your Rights",
        description = "Learn the Kenyan Constitution in bite-sized lessons. Understand the laws that protect and empower you as a citizen.",
        accentColor = KatibaColors.KenyaGreen
    ),
    OnboardingPage(
        backgroundImage = Res.drawable.onboarding_screen2,
        title = "Learn Daily",
        description = "Build your streak with daily clause insights, AI-powered summaries, and practical tips for civic life.",
        accentColor = KatibaColors.KenyaRed
    ),
    OnboardingPage(
        backgroundImage = Res.drawable.onboarding_screen3,
        title = "Become a Mzalendo",
        description = "Earn badges, track your progress, and become a civic champion for Kenya. Your journey starts here!",
        accentColor = KatibaColors.BeadGold
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    val currentPage = pagerState.currentPage

    // Use Box to allow full-screen image with overlaid buttons
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Pager content fills the entire screen
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPageContent(
                page = onboardingPages[pageIndex],
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom section: page indicator + button (overlaid on the full-screen pager)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Custom page indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(onboardingPages.size) { index ->
                        val isSelected = index == currentPage
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = tween(300)
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                            animationSpec = tween(300)
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Centered button with rounded rectangle shape and physical shadow
                if (currentPage == onboardingPages.size - 1) {
                    // "Get Started" button — only on page 3
                    val getStartedInteractionSource = remember { MutableInteractionSource() }
                    val isGetStartedPressed by getStartedInteractionSource.collectIsPressedAsState()
                    val getStartedPressOffset by animateDpAsState(
                        targetValue = if (isGetStartedPressed) 4.dp else 0.dp,
                        animationSpec = tween(durationMillis = 100)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .align(Alignment.BottomCenter)
                                .background(KatibaColors.DarkGreen, RoundedCornerShape(16.dp))
                        )
                        Button(
                            onClick = onGetStarted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = getStartedPressOffset),
                            shape = RoundedCornerShape(16.dp),
                            interactionSource = getStartedInteractionSource,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KatibaColors.KenyaGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    // "Next" button — pages 1 & 2
                    val nextInteractionSource = remember { MutableInteractionSource() }
                    val isNextPressed by nextInteractionSource.collectIsPressedAsState()
                    val nextPressOffset by animateDpAsState(
                        targetValue = if (isNextPressed) 4.dp else 0.dp,
                        animationSpec = tween(durationMillis = 100)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .align(Alignment.BottomCenter)
                                .background(KatibaColors.DarkGreen, RoundedCornerShape(16.dp))
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = nextPressOffset),
                            shape = RoundedCornerShape(16.dp),
                            interactionSource = nextInteractionSource,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KatibaColors.KenyaGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Next",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        // Background image
        Image(
            painter = painterResource(page.backgroundImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Text content at the bottom (above the overlaid buttons)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)
                .padding(bottom = 160.dp), // Increased padding to make room for overlaid buttons
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

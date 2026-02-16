package com.katiba.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.kenya_shield
import org.jetbrains.compose.resources.painterResource

/**
 * Story-style progress indicators at the top
 */
@Composable
private fun StoryProgressIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(totalSteps) { index ->
            val progress by animateFloatAsState(
                targetValue = when {
                    index < currentStep -> 1f
                    index == currentStep -> 1f
                    else -> 0f
                },
                label = "progress"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            if (index <= currentStep) KatibaColors.KenyaRed
                            else Color.Transparent
                        )
                )
            }
        }
    }
}

/**
 * Focused single-page detail view for the Clause of the Day
 * Redesigned with story-style progress indicators and modern card layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClauseDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyContent = remember { SampleDataRepository.getDailyContent() }
    val scrollState = rememberScrollState()

    // For story progress - could be driven by actual content sections
    val totalSteps = 5
    var currentStep by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
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

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Story progress indicators
            StoryProgressIndicator(
                totalSteps = totalSteps,
                currentStep = currentStep,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Top bar with close and navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.DarkGray
                    )
                }

                // Navigation arrows could go here for story navigation
                Row {
                    IconButton(
                        onClick = { if (currentStep > 0) currentStep-- },
                        enabled = currentStep > 0
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = if (currentStep > 0) Color.DarkGray else Color.Gray.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
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
                                        // Sub-sub-clauses
                                        if (subClause.subSubClauses.isNotEmpty()) {
                                            subClause.subSubClauses.forEach { subSubClause ->
                                                Row(
                                                    modifier = Modifier.padding(start = 28.dp, top = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "(${subSubClause.label})",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = KatibaColors.KenyaGreen.copy(alpha = 0.8f),
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.width(28.dp)
                                                    )
                                                    Text(
                                                        text = subSubClause.text,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Black.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
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

                    // Tips section if available
                    if (dailyContent.tips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        dailyContent.tips.forEach { tip ->
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black.copy(alpha = 0.8f),
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

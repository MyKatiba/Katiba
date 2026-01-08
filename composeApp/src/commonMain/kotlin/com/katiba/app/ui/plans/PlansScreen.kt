package com.katiba.app.ui.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.katiba.app.data.model.Lesson
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.ui.theme.KatibaColors

/**
 * Duolingo-inspired learning path screen with milestone progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lessons = remember { SampleDataRepository.getLessons() }
    val userProfile = remember { SampleDataRepository.getUserProfile() }
    
    // Group lessons by chapter
    val lessonsByChapter = remember(lessons) {
        lessons.groupBy { it.chapterNumber }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Learning Path",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Streak indicator
                    StreakBadge(streak = userProfile.streak)
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress summary
            ProgressSummaryCard(
                completedLessons = lessons.count { it.isCompleted },
                totalLessons = lessons.size,
                currentChapter = lessonsByChapter.entries
                    .find { it.value.any { lesson -> lesson.isCurrent } }
                    ?.key ?: 1
            )
            
            // Learning path
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lessonsByChapter.forEach { (chapterNumber, chapterLessons) ->
                    // Chapter header
                    item(key = "header_$chapterNumber") {
                        ChapterSectionHeader(
                            chapterNumber = chapterNumber,
                            isCompleted = chapterLessons.all { it.isCompleted },
                            isLocked = chapterLessons.all { it.isLocked }
                        )
                    }
                    
                    // Lessons in chapter
                    items(
                        items = chapterLessons,
                        key = { it.id }
                    ) { lesson ->
                        LessonNode(
                            lesson = lesson,
                            isLastInChapter = lesson == chapterLessons.last(),
                            onClick = {
                                if (!lesson.isLocked) {
                                    onLessonClick(lesson.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    Surface(
        color = KatibaColors.KenyaRed.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "🔥",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = streak.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KatibaColors.KenyaRed
            )
        }
    }
}

@Composable
private fun ProgressSummaryCard(
    completedLessons: Int,
    totalLessons: Int,
    currentChapter: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        KatibaColors.KenyaGreen,
                        KatibaColors.DarkGreen
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "$completedLessons of $totalLessons lessons",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Currently on Chapter $currentChapter",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Circular progress
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(completedLessons * 100 / totalLessons)}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChapterSectionHeader(
    chapterNumber: Int,
    isCompleted: Boolean,
    isLocked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chapter badge (shield-inspired)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        isCompleted -> KatibaColors.KenyaGreen
                        isLocked -> MaterialTheme.colorScheme.surfaceVariant
                        else -> KatibaColors.BeadGold
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            } else if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = chapterNumber.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Column {
            Text(
                text = "Chapter $chapterNumber",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isLocked) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
        }
    }
}

@Composable
private fun LessonNode(
    lesson: Lesson,
    isLastInChapter: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Connector line and node
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Node circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(
                        elevation = if (lesson.isCurrent) 8.dp else 2.dp,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(
                        when {
                            lesson.isCompleted -> KatibaColors.KenyaGreen
                            lesson.isCurrent -> KatibaColors.BeadGold
                            lesson.isLocked -> MaterialTheme.colorScheme.surfaceVariant
                            else -> Color.White
                        }
                    )
                    .then(
                        if (lesson.isCurrent) {
                            Modifier.background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        KatibaColors.BeadGold,
                                        KatibaColors.BeadOrange
                                    )
                                )
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    lesson.isCompleted -> {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                    lesson.isLocked -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    lesson.isCurrent -> {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(KatibaColors.KenyaGreen)
                        )
                    }
                }
            }
            
            // Connector line
            if (!isLastInChapter) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(60.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            if (lesson.isCompleted) {
                                KatibaColors.KenyaGreen.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Lesson card
        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = !lesson.isLocked, onClick = onClick),
            color = if (lesson.isCurrent) {
                KatibaColors.BeadGold.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            shape = RoundedCornerShape(12.dp),
            shadowElevation = if (lesson.isCurrent) 4.dp else 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (lesson.isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (lesson.isLocked) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                    Text(
                        text = lesson.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                // XP reward
                if (!lesson.isLocked) {
                    Surface(
                        color = if (lesson.isCompleted) {
                            KatibaColors.KenyaGreen.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+${lesson.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (lesson.isCompleted) {
                                KatibaColors.KenyaGreen
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

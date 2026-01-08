package com.katiba.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.katiba.app.data.model.ActivityRecord
import com.katiba.app.data.model.ActivityType
import com.katiba.app.data.model.Badge
import com.katiba.app.data.model.UserProfile
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.ui.components.KatibaCard
import com.katiba.app.ui.theme.KatibaColors

/**
 * Profile screen with user bio, residence, streak, badges, and activity
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile = remember { SampleDataRepository.getUserProfile() }
    val recentActivity = remember { SampleDataRepository.getRecentActivity() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile header with bio
            item {
                ProfileHeaderCard(userProfile = userProfile)
            }
            
            // Residence info
            item {
                ResidenceCard(userProfile = userProfile)
            }
            
            // Streak card
            item {
                StreakCard(
                    currentStreak = userProfile.streak,
                    longestStreak = userProfile.longestStreak
                )
            }
            
            // Badges/Achievements
            item {
                BadgesCard(badges = userProfile.badges)
            }
            
            // Recent Activity
            item {
                ActivityCard(activities = recentActivity)
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(userProfile: UserProfile) {
    KatibaCard(
        showBeadworkBorder = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                KatibaColors.KenyaGreen,
                                KatibaColors.DarkGreen
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userProfile.name.first().toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column {
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userProfile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Joined ${userProfile.joinedDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Stats row
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = userProfile.totalLessonsCompleted.toString(),
                label = "Lessons"
            )
            StatItem(
                value = userProfile.badges.count { it.isEarned }.toString(),
                label = "Badges"
            )
            StatItem(
                value = userProfile.streak.toString(),
                label = "Day Streak"
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = KatibaColors.KenyaGreen
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResidenceCard(userProfile: UserProfile) {
    KatibaCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📍",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Residence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ResidenceItem(label = "County", value = userProfile.county)
                ResidenceItem(label = "Constituency", value = userProfile.constituency)
                ResidenceItem(label = "Ward", value = userProfile.ward)
            }
        }
    }
}

@Composable
private fun ResidenceItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifEmpty { "Not set" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StreakCard(
    currentStreak: Int,
    longestStreak: Int
) {
    KatibaCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Streak",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Longest: $longestStreak days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Current streak display
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                KatibaColors.KenyaRed,
                                KatibaColors.BeadOrange
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentStreak.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "days",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgesCard(badges: List<Badge>) {
    KatibaCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🏆",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Badges",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "${badges.count { it.isEarned }}/${badges.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Badge grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                badges.take(5).forEach { badge ->
                    BadgeItem(
                        badge = badge,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(
    badge: Badge,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (badge.isEarned) {
                        KatibaColors.BeadGold.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (badge.isEarned) {
                Text(
                    text = "⭐",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = badge.name.take(8) + if (badge.name.length > 8) "..." else "",
            style = MaterialTheme.typography.labelSmall,
            color = if (badge.isEarned) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            maxLines = 1
        )
    }
}

@Composable
private fun ActivityCard(activities: List<ActivityRecord>) {
    KatibaCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📊",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            activities.take(5).forEach { activity ->
                ActivityItem(activity = activity)
                if (activity != activities.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: ActivityRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Activity type icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (activity.type) {
                            ActivityType.LESSON_COMPLETED -> KatibaColors.KenyaGreen.copy(alpha = 0.1f)
                            ActivityType.BADGE_EARNED -> KatibaColors.BeadGold.copy(alpha = 0.1f)
                            ActivityType.STREAK_MILESTONE -> KatibaColors.KenyaRed.copy(alpha = 0.1f)
                            ActivityType.CHAPTER_COMPLETED -> KatibaColors.KenyaGreen.copy(alpha = 0.2f)
                            ActivityType.DAILY_CLAUSE_READ -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (activity.type) {
                        ActivityType.LESSON_COMPLETED -> "📚"
                        ActivityType.BADGE_EARNED -> "🏆"
                        ActivityType.STREAK_MILESTONE -> "🔥"
                        ActivityType.CHAPTER_COMPLETED -> "✓"
                        ActivityType.DAILY_CLAUSE_READ -> "📖"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            Column {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (activity.xpEarned > 0) {
            Text(
                text = "+${activity.xpEarned} XP",
                style = MaterialTheme.typography.labelSmall,
                color = KatibaColors.KenyaGreen
            )
        }
    }
}

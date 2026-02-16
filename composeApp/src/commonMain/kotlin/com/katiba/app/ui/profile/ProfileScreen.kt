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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.data.model.Badge
import com.katiba.app.data.model.UserProfile
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.bracelet
import org.jetbrains.compose.resources.painterResource

/**
 * Profile screen with redesigned layout matching the Kenyan civic app design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile = remember { SampleDataRepository.getUserProfile() }
    val lessons = remember { SampleDataRepository.getLessons() }
    var showCivicDataSheet by remember { mutableStateOf(false) }

    // Calculate current course progress
    val currentChapter = 4 // Bill of Rights
    val chapterLessons = lessons.filter { it.chapterNumber == currentChapter }
    val completedLessons = chapterLessons.count { it.isCompleted }
    val totalLessons = chapterLessons.size
    val nextLesson = chapterLessons.find { !it.isCompleted }
    // Use a shortened lesson title for display (e.g., "Right to Life" instead of full title)
    val nextLessonDisplay = nextLesson?.title?.removePrefix("Your ")?.removePrefix("The ") ?: "Complete!"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KatibaColors.Background)
    ) {
        // Top bar with beadwork border
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KatibaColors.Background)
        ) {
            // Beadwork border
            BeadworkBorder()

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = KatibaColors.KenyaBlack
                )
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.Gray
                    )
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            ProfileCard(userProfile = userProfile)

            // Stats Cards (Day Streak & Articles Read)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    icon = "🔥",
                    iconBackgroundColor = Color(0xFFFED7AA), // Orange light
                    value = userProfile.streak.toString(),
                    label = "DAY STREAK",
                    borderColor = KatibaColors.KenyaRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = "📖",
                    iconBackgroundColor = Color(0xFFBBF7D0), // Green light
                    value = "45",
                    label = "ARTICLES READ",
                    borderColor = KatibaColors.KenyaGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // Achievements Section
            AchievementsCard(badges = userProfile.badges)

            // Current Course Section
            CurrentCourseCard(
                chapterNumber = currentChapter,
                chapterTitle = "The Bill of Rights",
                completedLessons = completedLessons,
                totalLessons = totalLessons,
                nextLessonTitle = nextLessonDisplay
            )

            // My Civic Data Section
            CivicDataCard(
                userProfile = userProfile,
                onClick = { showCivicDataSheet = true }
            )
        }
    }
    
    // Civic Data Bottom Sheet
    if (showCivicDataSheet) {
        CivicDataBottomSheet(
            userProfile = userProfile,
            onDismiss = { showCivicDataSheet = false }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Bracelet header image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Image(
                    painter = painterResource(Res.drawable.bracelet),
                    contentDescription = "Kenyan beadwork bracelet",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Subtle gradient overlay for better avatar visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }

            // Avatar overlapping the header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-48).dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar with edit button
                Box {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
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

                    // Edit button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(CircleShape)
                            .background(KatibaColors.KenyaRed)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Content below avatar (adjusted for avatar overlap)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-32).dp)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Name
                Text(
                    text = "Wanjiku Kamau",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KatibaColors.KenyaBlack
                )

                // Member since
                Text(
                    text = "Citizen since 2023",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tags row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    // Location tag
                    ProfileTag(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = KatibaColors.KenyaRed,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        text = "Nairobi, Kenya"
                    )

                    // Verified tag
                    ProfileTag(
                        icon = {
                            Text(
                                text = "✓",
                                color = KatibaColors.KenyaGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = "Verified Voter"
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileTag(
    icon: @Composable () -> Unit,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFF3F4F6)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4B5563)
            )
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    color = KatibaColors.KenyaBlack
                )

                // Label
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun AchievementsCard(badges: List<Badge>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    color = KatibaColors.KenyaBlack
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = KatibaColors.KenyaRed,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Achievements horizontal scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AchievementItem(
                    icon = "🛡️",
                    name = "Civic\nDefender",
                    isEarned = true
                )
                AchievementItem(
                    icon = "⚖️",
                    name = "Rights\nExpert",
                    isEarned = false
                )
                AchievementItem(
                    icon = "🗳️",
                    name = "Voter\nReady",
                    isEarned = false
                )
                AchievementItem(
                    icon = "🎓",
                    name = "Scholar",
                    isEarned = false
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
                .border(2.dp, Color.White, CircleShape)
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
            color = if (isEarned) Color(0xFF374151) else Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun CurrentCourseCard(
    chapterNumber: Int,
    chapterTitle: String,
    completedLessons: Int,
    totalLessons: Int,
    nextLessonTitle: String
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                color = KatibaColors.KenyaBlack
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
                        .background(KatibaColors.KenyaBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chapterNumber.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                        color = KatibaColors.KenyaBlack
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$completedLessons of $totalLessons lessons completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFE5E7EB))
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

            HorizontalDivider(color = Color(0xFFF3F4F6))

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
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = { },
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    color = KatibaColors.KenyaBlack
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
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = KatibaColors.KenyaBlack
            )
        }

        if (showDivider) {
            HorizontalDivider(color = Color(0xFFF3F4F6))
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
        containerColor = Color.White,
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
                    color = KatibaColors.KenyaBlack
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // National ID
            DetailRow(
                icon = "🆔",
                label = "National ID",
                value = userProfile.nationalId.ifEmpty { "Not set" }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // County
            DetailRow(
                icon = "🏛️",
                label = "County",
                value = userProfile.county.ifEmpty { "Not set" }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Constituency
            DetailRow(
                icon = "📍",
                label = "Constituency",
                value = userProfile.constituency.ifEmpty { "Not set" }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ward
            DetailRow(
                icon = "🗺️",
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
                        Color(0xFFF3F4F6)
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
                        Text(
                            text = "🗳️",
                            fontSize = 24.sp
                        )
                        Column {
                            Text(
                                text = "Voter Registration Status",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (userProfile.isRegisteredVoter) "Registered Voter" else "Not Registered",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.isRegisteredVoter) KatibaColors.KenyaGreen else Color.Gray
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
            
            // Close button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KatibaColors.KenyaGreen
                )
            ) {
                Text(
                    text = "Close",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = KatibaColors.KenyaBlack
            )
        }
    }
}

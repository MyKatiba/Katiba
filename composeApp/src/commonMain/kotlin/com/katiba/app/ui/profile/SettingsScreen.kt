package com.katiba.app.ui.profile

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.ui.theme.KatibaColors

/**
 * Settings screen with account, notifications, display preferences, and about sections
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSignOut: () -> Unit,
    onEditProfile: () -> Unit = {},
    onPasswordSecurity: () -> Unit = {},
    onUpdateResidence: () -> Unit = {},
    onNationalID: () -> Unit = {},
    onAppearance: () -> Unit = {},
    onFontSize: () -> Unit = {},
    onLanguage: () -> Unit = {},
    onAboutKatiba: () -> Unit = {},
    onSendFeedback: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
                HorizontalDivider(thickness = 2.dp, color = Color.Gray.copy(alpha = 0.3f))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account section
            item {
                SettingsSection(title = "Account") {
                    SettingsItem(
                        emoji = "👤",
                        title = "Edit Profile",
                        subtitle = "Name, email, avatar",
                        onClick = onEditProfile
                    )
                    HorizontalDivider()
                    SettingsItem(
                        emoji = "🔒",
                        title = "Password & Security",
                        subtitle = "Change password, 2FA",
                        onClick = onPasswordSecurity
                    )
                    HorizontalDivider()
                    SettingsItem(
                        emoji = "📍",
                        title = "Update Residence",
                        subtitle = "County, constituency, ward",
                        onClick = onUpdateResidence
                    )
                }
            }
            
            // Civic Information section
            item {
                var isRegisteredVoter by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(false) }
                
                SettingsSection(title = "Civic Information") {
                    SettingsItem(
                        emoji = "🇰🇪",
                        title = "National ID",
                        subtitle = "Update your national ID number",
                        onClick = onNationalID
                    )
                    HorizontalDivider()
                    SettingsToggleItem(
                        emoji = "✅",
                        title = "Registered Voter",
                        subtitle = if (isRegisteredVoter) "You are registered to vote" else "Not registered to vote",
                        checked = isRegisteredVoter,
                        enabled = !isLoading,
                        onCheckedChange = { checked ->
                            isRegisteredVoter = checked
                        }
                    )
                }
            }
            
            // Notifications section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsToggleItem(
                        emoji = "🔔",
                        title = "Daily Reminders",
                        subtitle = "Get notified about your daily clause",
                        checked = true,
                        onCheckedChange = { }
                    )
                    HorizontalDivider()
                    SettingsToggleItem(
                        emoji = "🔥",
                        title = "Streak Reminders",
                        subtitle = "Don't break your streak!",
                        checked = true,
                        onCheckedChange = { }
                    )
                    HorizontalDivider()
                    SettingsToggleItem(
                        emoji = "🆕",
                        title = "New Content",
                        subtitle = "Be notified of new lessons",
                        checked = false,
                        onCheckedChange = { }
                    )
                }
            }
            
            // Display section
            item {
                SettingsSection(title = "Display") {
                    SettingsItem(
                        emoji = "🎨",
                        title = "Appearance",
                        subtitle = "Light, Dark, System",
                        onClick = onAppearance
                    )
                    HorizontalDivider()
                    SettingsItem(
                        emoji = "🔤",
                        title = "Font Size",
                        subtitle = "Adjust text size",
                        onClick = onFontSize
                    )
                    HorizontalDivider()
                    SettingsItem(
                        emoji = "🌐",
                        title = "Language",
                        subtitle = "English",
                        onClick = onLanguage
                    )
                }
            }
            
            // About section
            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        emoji = "ℹ️",
                        title = "About Katiba",
                        subtitle = "Learn about the app",
                        onClick = onAboutKatiba
                    )
                    HorizontalDivider()
                    SettingsItem(
                        emoji = "📜",
                        title = "Terms of Service",
                        subtitle = "Read our terms"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        emoji = "🛡️",
                        title = "Privacy Policy",
                        subtitle = "How we protect your data"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        emoji = "💬",
                        title = "Send Feedback",
                        subtitle = "Help us improve",
                        onClick = onSendFeedback
                    )
                }
            }
            
            // Version info
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "Katiba v1.0.0",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Beadwork accent
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            KatibaColors.KenyaBlack,
                                            KatibaColors.KenyaRed,
                                            KatibaColors.KenyaGreen
                                        )
                                    )
                                )
                        )
                    }
                }
            }
            
            // Sign out
            item {
                val interactionSource = remember { MutableInteractionSource() }
                val isButtonPressed by interactionSource.collectIsPressedAsState()
                val buttonPressOffset by animateDpAsState(
                    targetValue = if (isButtonPressed) 4.dp else 0.dp,
                    animationSpec = tween(durationMillis = 100)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    // Shadow box at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                color = Color(0xFF8B0000),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )

                    // Button with press animation
                    Surface(
                        color = KatibaColors.KenyaRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .offset(y = buttonPressOffset)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onSignOut() }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.titleMedium,
                                color = KatibaColors.KenyaRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit = { }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 28.sp,
            color = KatibaColors.KenyaGreen,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsToggleItem(
    emoji: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 28.sp,
            color = if (enabled) KatibaColors.KenyaGreen else Color.Gray,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = KatibaColors.KenyaWhite,
                checkedTrackColor = KatibaColors.KenyaGreen
            )
        )
    }
}

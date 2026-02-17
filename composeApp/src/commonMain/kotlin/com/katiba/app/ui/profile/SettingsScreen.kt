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
import com.katiba.app.ui.theme.KatibaColors

/**
 * Settings screen with account, notifications, display preferences, and about sections
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSignOut: () -> Unit,
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
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Name, email, avatar"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Password & Security",
                        subtitle = "Change password, 2FA"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = Icons.Default.LocationOn,
                        title = "Update Residence",
                        subtitle = "County, constituency, ward"
                    )
                }
            }
            
            // Civic Information section
            item {
                var isRegisteredVoter by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(false) }
                
                SettingsSection(title = "Civic Information") {
                    SettingsItem(
                        icon = Icons.Default.Badge,
                        title = "National ID",
                        subtitle = "Update your national ID number"
                    )
                    HorizontalDivider()
                    SettingsToggleItem(
                        icon = Icons.Default.HowToVote,
                        title = "Registered Voter",
                        subtitle = if (isRegisteredVoter) "You are registered to vote" else "Not registered to vote",
                        checked = isRegisteredVoter,
                        enabled = !isLoading,
                        onCheckedChange = { checked ->
                            isRegisteredVoter = checked
                            // TODO: Call API to update voter status
                            // isLoading = true
                            // userApiClient.updateProfile(isRegisteredVoter = checked)
                            // isLoading = false
                        }
                    )
                }
            }
            
            // Notifications section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Daily Reminders",
                        subtitle = "Get notified about your daily clause",
                        checked = true,
                        onCheckedChange = { }
                    )
                    HorizontalDivider()
                    SettingsToggleItem(
                        icon = Icons.Default.TrackChanges,
                        title = "Streak Reminders",
                        subtitle = "Don't break your streak!",
                        checked = true,
                        onCheckedChange = { }
                    )
                    HorizontalDivider()
                    SettingsToggleItem(
                        icon = Icons.Default.Campaign,
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
                        icon = Icons.Default.DarkMode,
                        title = "Appearance",
                        subtitle = "Light, Dark, System"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = Icons.Default.TextFields,
                        title = "Font Size",
                        subtitle = "Adjust text size"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "English"
                    )
                }
            }
            
            // About section
            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        icon = Icons.Default.MenuBook,
                        title = "About Katiba",
                        subtitle = "Learn about the app"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = "Terms of Service",
                        subtitle = "Read our terms"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Privacy Policy",
                        subtitle = "How we protect your data"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = Icons.Default.Chat,
                        title = "Send Feedback",
                        subtitle = "Help us improve"
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
    icon: ImageVector,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp).padding(end = 12.dp)
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
    icon: ImageVector,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray,
            modifier = Modifier.size(20.dp).padding(end = 12.dp)
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

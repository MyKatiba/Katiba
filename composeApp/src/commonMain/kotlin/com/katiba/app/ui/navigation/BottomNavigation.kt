package com.katiba.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.ui.theme.KatibaColors

/**
 * Bottom navigation tabs with Navigation 3 style NavKey routes
 */
enum class BottomNavTab(
    val label: String,
    val route: NavKey,
    val selectedColor: Color
) {
    HOME("Home", HomeRoute, KatibaColors.KenyaGreen),
    KATIBA("Katiba", ConstitutionRoute, KatibaColors.KenyaRed),
    PLANS("Plans", PlansRoute, KatibaColors.BeadGold),
    PROFILE("Profile", ProfileRoute, Color(0xFF6B7280))
}

@Composable
fun KatibaBottomNavigation(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main navigation row - raised higher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavTab.entries.forEach { tab ->
                    val isSelected = tab == currentTab
                    BottomNavItem(
                        tab = tab,
                        isSelected = isSelected,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
            // Extra space at the bottom for phone's navigation bar
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Background for selected state with animation
    val backgroundColor = if (isSelected) {
        tab.selectedColor.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }

    val iconColor = if (isSelected) {
        tab.selectedColor
    } else {
        Color(0xFF9CA3AF) // Gray for unselected
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = if (isSelected) 16.dp else 12.dp,
                vertical = 8.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getTabIcon(tab),
                contentDescription = tab.label,
                modifier = Modifier.size(24.dp),
                tint = iconColor
            )

            // Animated visibility for label - only show when selected
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut() + shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tab.label,
                        color = tab.selectedColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun getTabIcon(tab: BottomNavTab): ImageVector {
    return when (tab) {
        BottomNavTab.HOME -> HomeIcon
        BottomNavTab.KATIBA -> BookIcon
        BottomNavTab.PLANS -> PlansIcon
        BottomNavTab.PROFILE -> ProfileIcon
    }
}

// Custom vector icons
private val HomeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Home",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = androidx.compose.ui.graphics.SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(3f, 9f)
            lineTo(12f, 2f)
            lineTo(21f, 9f)
            verticalLineTo(20f)
            horizontalLineTo(15f)
            verticalLineTo(14f)
            horizontalLineTo(9f)
            verticalLineTo(20f)
            horizontalLineTo(3f)
            close()
        }
    }.build()

private val BookIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Book",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = androidx.compose.ui.graphics.SolidColor(Color.Black), strokeLineWidth = 2f) {
            moveTo(4f, 19.5f)
            curveTo(4f, 18.119f, 5.119f, 17f, 6.5f, 17f)
            horizontalLineTo(20f)
            moveTo(4f, 19.5f)
            verticalLineTo(4.5f)
            curveTo(4f, 3.119f, 5.119f, 2f, 6.5f, 2f)
            horizontalLineTo(20f)
            verticalLineTo(22f)
            horizontalLineTo(6.5f)
            curveTo(5.119f, 22f, 4f, 20.881f, 4f, 19.5f)
        }
    }.build()

// Custom Plans icon (carousel vertical) as specified by user
private val PlansIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Plans",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Main rectangle
        path(fill = androidx.compose.ui.graphics.SolidColor(Color.Black)) {
            moveTo(18f, 6f)
            horizontalLineTo(6f)
            curveTo(4.9f, 6f, 4f, 6.9f, 4f, 8f)
            verticalLineTo(16f)
            curveTo(4f, 17.1f, 4.9f, 18f, 6f, 18f)
            horizontalLineTo(18f)
            curveTo(19.1f, 18f, 20f, 17.1f, 20f, 16f)
            verticalLineTo(8f)
            curveTo(20f, 6.9f, 19.1f, 6f, 18f, 6f)
            close()
        }
        // Bottom element
        path(fill = androidx.compose.ui.graphics.SolidColor(Color.Black)) {
            moveTo(16f, 19f)
            curveTo(16f, 19f, 17.1f, 19f, 18f, 19.85f)
            lineTo(18f, 21f)
            lineTo(18f, 22f)
            curveTo(18f, 22.55f, 17.55f, 23f, 17f, 23f)
            lineTo(17f, 22f)
            lineTo(17f, 21f)
            horizontalLineTo(7f)
            verticalLineTo(22f)
            curveTo(7f, 22.55f, 6.55f, 23f, 6f, 23f)
            lineTo(6f, 22f)
            verticalLineTo(21f)
            curveTo(6f, 19.9f, 6.9f, 19f, 8f, 19f)
            horizontalLineTo(16f)
            close()
        }
        // Top element
        path(fill = androidx.compose.ui.graphics.SolidColor(Color.Black)) {
            moveTo(17f, 1f)
            curveTo(17.55f, 1f, 18f, 1.45f, 18f, 2f)
            verticalLineTo(3f)
            curveTo(17.1f, 4f, 16f, 5f, 16f, 5f)
            horizontalLineTo(8f)
            curveTo(6.9f, 5f, 6f, 4.1f, 6f, 3f)
            verticalLineTo(2f)
            curveTo(6f, 1.45f, 6.45f, 1f, 7f, 1f)
            lineTo(7f, 2f)
            verticalLineTo(3f)
            horizontalLineTo(17f)
            verticalLineTo(2f)
            lineTo(17f, 1f)
            close()
        }
    }.build()

private val ProfileIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Profile",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = androidx.compose.ui.graphics.SolidColor(Color.Black), strokeLineWidth = 2f) {
            // Head circle
            moveTo(12f, 8f)
            moveToRelative(-4f, 0f)
            arcToRelative(4f, 4f, 0f, true, true, 8f, 0f)
            arcToRelative(4f, 4f, 0f, true, true, -8f, 0f)
        }
        path(fill = null, stroke = androidx.compose.ui.graphics.SolidColor(Color.Black), strokeLineWidth = 2f) {
            // Body arc
            moveTo(20f, 21f)
            verticalLineTo(19f)
            curveTo(20f, 16.79f, 18.21f, 15f, 16f, 15f)
            horizontalLineTo(8f)
            curveTo(5.79f, 15f, 4f, 16.79f, 4f, 19f)
            verticalLineTo(21f)
        }
    }.build()

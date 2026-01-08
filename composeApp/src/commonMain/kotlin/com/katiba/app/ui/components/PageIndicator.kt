package com.katiba.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.katiba.app.ui.theme.KatibaColors

/**
 * Page indicator dots for multi-page views
 */
@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = KatibaColors.KenyaGreen,
    inactiveColor: Color = KatibaColors.SurfaceVariant,
    dotSize: Dp = 8.dp,
    spacing: Dp = 8.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) activeColor else inactiveColor
                    )
            )
        }
    }
}

/**
 * Beadwork-inspired page indicator with Kenyan flag colors
 */
@Composable
fun BeadworkPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    dotSize: Dp = 10.dp,
    spacing: Dp = 6.dp
) {
    val colors = listOf(
        KatibaColors.KenyaBlack,
        KatibaColors.KenyaRed,
        KatibaColors.KenyaGreen
    )
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        repeat(pageCount) { index ->
            val color = if (index == currentPage) {
                colors[index % colors.size]
            } else {
                KatibaColors.SurfaceVariant
            }
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

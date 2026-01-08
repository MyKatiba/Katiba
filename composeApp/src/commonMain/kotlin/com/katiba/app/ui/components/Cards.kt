package com.katiba.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.katiba.app.ui.theme.KatibaColors

/**
 * Custom card component with optional Kenyan beadwork-inspired top border
 */
@Composable
fun KatibaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showBeadworkBorder: Boolean = true,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Beadwork-inspired gradient top border
        if (showBeadworkBorder) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                KatibaColors.KenyaBlack,
                                KatibaColors.KenyaRed,
                                KatibaColors.KenyaGreen,
                                KatibaColors.KenyaWhite,
                                KatibaColors.KenyaGreen,
                                KatibaColors.KenyaRed,
                                KatibaColors.KenyaBlack
                            )
                        )
                    )
                    .padding(vertical = 3.dp)
            )
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                ),
            shape = if (showBeadworkBorder) {
                RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            } else {
                RoundedCornerShape(16.dp)
            },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            content()
        }
    }
}

/**
 * Card with gradient background inspired by Kenyan colors
 */
@Composable
fun KatibaGradientCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradientColors: List<Color> = listOf(
        KatibaColors.KenyaGreen.copy(alpha = 0.9f),
        KatibaColors.DarkGreen
    ),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

/**
 * Section header with Kenyan styling
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

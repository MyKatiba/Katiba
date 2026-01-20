package com.katiba.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Android actual implementation for PlatformBackHandler.
 * Uses the native BackHandler from androidx.activity.compose.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}


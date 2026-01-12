package com.katiba.app.ui.navigation
import androidx.compose.runtime.Composable
/**
 * iOS actual implementation for PlatformBackHandler.
 * iOS doesn't have a hardware back button like Android, so this is a no-op.
 * Navigation is handled by the native iOS gestures and navigation patterns.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS - iOS uses swipe gestures and navigation controller for back navigation
}

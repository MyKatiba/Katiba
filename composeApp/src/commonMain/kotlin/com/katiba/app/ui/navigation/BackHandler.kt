@file:JvmName("CommonBackHandlerKt")

package com.katiba.app.ui.navigation

import androidx.compose.runtime.Composable
import kotlin.jvm.JvmName

/**
 * Expect declaration for platform-specific back button handling.
 * On Android, this wraps the system back button handler.
 * On iOS, this is a no-op since iOS doesn't have a hardware back button.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

/**
 * Convenience wrapper for PlatformBackHandler that matches the common BackHandler API.
 * Use this in commonMain composables for back button handling.
 */
@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    PlatformBackHandler(enabled = enabled, onBack = onBack)
}


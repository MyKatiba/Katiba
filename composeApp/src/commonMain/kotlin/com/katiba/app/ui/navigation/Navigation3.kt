package com.katiba.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import kotlin.reflect.KClass

/**
 * Navigation 3 Style Navigation for Compose Multiplatform
 *
 * Core Philosophy: You own the backstack as a mutable list.
 * Navigation corresponds to standard list operations: add, removeLast, etc.
 */

/**
 * Marker interface for all navigation destinations.
 * All route classes must implement this interface.
 */
interface NavKey

/**
 * Creates and remembers a navigation backstack with the given initial destination.
 * The backstack is a SnapshotStateList that you can directly manipulate.
 */
@Composable
fun rememberNavBackStack(initialKey: NavKey): SnapshotStateList<NavKey> {
    return remember {
        mutableStateListOf(initialKey)
    }
}

/**
 * Entry decorator for managing saved state and ViewModel scoping.
 */
class NavEntryDecorator(
    val decorateEntry: @Composable (key: NavKey, content: @Composable () -> Unit) -> Unit
)

/**
 * Creates a saved state decorator that preserves composable state across navigation.
 */
@Composable
fun rememberSavedStateNavEntryDecorator(): NavEntryDecorator {
    val saveableStateHolder = rememberSaveableStateHolder()

    return remember {
        NavEntryDecorator { key, content ->
            saveableStateHolder.SaveableStateProvider(key.hashCode()) {
                content()
            }
        }
    }
}

/**
 * Scope for building navigation entries using a DSL approach.
 */
class EntryProviderScope {
    val entries = mutableMapOf<KClass<out NavKey>, @Composable (NavKey) -> Unit>()

    /**
     * Register a screen for a specific NavKey type.
     */
    inline fun <reified T : NavKey> entry(noinline content: @Composable (T) -> Unit) {
        entries[T::class] = { key ->
            @Suppress("UNCHECKED_CAST")
            content(key as T)
        }
    }
}

/**
 * Creates an entry provider using DSL syntax.
 */
fun entryProvider(builder: EntryProviderScope.() -> Unit): EntryProviderScope {
    return EntryProviderScope().apply(builder)
}

/**
 * NavDisplay - The main navigation composable that replaces NavHost.
 * Observes the backstack and renders the appropriate content.
 */
@Composable
fun NavDisplay(
    backStack: SnapshotStateList<NavKey>,
    modifier: Modifier = Modifier,
    entryDecorators: List<NavEntryDecorator> = emptyList(),
    entryProvider: EntryProviderScope
) {
    val currentKey = backStack.lastOrNull() ?: return

    Box(modifier = modifier) {
        AnimatedContent(
            targetState = currentKey,
            transitionSpec = {
                // Determine direction based on backstack changes
                val isNavigatingForward = backStack.size > 1

                if (isNavigatingForward) {
                    // Forward navigation: slide in from right
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300)))
                } else {
                    // Back navigation: slide in from left
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300)))
                }
            },
            label = "NavDisplay"
        ) { key ->
            val provider = entryProvider.entries[key::class]

            if (provider != null) {
                // Apply decorators
                var decoratedContent: @Composable () -> Unit = { provider(key) }

                entryDecorators.forEach { decorator ->
                    val previousContent = decoratedContent
                    decoratedContent = {
                        decorator.decorateEntry(key, previousContent)
                    }
                }

                decoratedContent()
            } else {
                // Fallback for unregistered routes
                throw IllegalStateException("No entry registered for ${key::class.simpleName}")
            }
        }
    }
}

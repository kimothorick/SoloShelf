package com.kimothorick.soloshelf.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import com.kimothorick.soloshelf.util.isAtLeastMedium

enum class SettingsPane {
    LIST,
    DETAIL,
    SINGLE,
}

val LocalSettingsPane = staticCompositionLocalOf { SettingsPane.SINGLE }

val isDetailPane: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalSettingsPane.current == SettingsPane.DETAIL

/**
 * A custom [Scene] that displays two [NavEntry]s side-by-side in a 50/50 split.
 */
class SettingsTwoPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(listEntry, detailEntry)
    override val content: @Composable (() -> Unit) = {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(0.5f)) {
                CompositionLocalProvider(LocalSettingsPane provides SettingsPane.LIST) {
                    listEntry.Content()
                }
            }
            Column(modifier = Modifier.weight(0.5f)) {
                AnimatedContent(
                    targetState = detailEntry,
                    transitionSpec = {
                        (slideInHorizontally { it / 4 } + fadeIn()).togetherWith(
                            fadeOut(),
                        )
                    },
                    label = "SettingsDetailChange",
                ) { entry ->
                    CompositionLocalProvider(LocalSettingsPane provides SettingsPane.DETAIL) {
                        entry.Content()
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed
         * as a list in the [SettingsTwoPaneScene].
         */
        fun listPane() =
            metadata {
                put(ListKey, value = true)
            }

        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed
         * as a detail in the [SettingsTwoPaneScene].
         */
        fun detailPane() =
            metadata {
                put(DetailKey, value = true)
            }
    }

    object ListKey : NavMetadataKey<Boolean>

    object DetailKey : NavMetadataKey<Boolean>
}

@Composable
fun <T : Any> rememberSettingsTwoPaneSceneStrategy(): SettingsTwoPaneSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

    return remember(windowSizeClass) {
        SettingsTwoPaneSceneStrategy(windowSizeClass)
    }
}

/**
 * A [SceneStrategy] that activates a [SettingsTwoPaneScene] if the window is wide enough
 * and the top two back stack entries declare support for settings-pane display.
 */
class SettingsTwoPaneSceneStrategy<T : Any>(
    val windowSizeClass: WindowSizeClass,
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>,
    ): Scene<T>? {
        // Condition 1: Only return a Scene if the window is sufficiently wide to render two panes.
        if (!windowSizeClass.isAtLeastMedium) {
            return null
        }

        val detailEntry =
            entries.lastOrNull()?.takeIf { it.metadata.contains(SettingsTwoPaneScene.DetailKey) } ?: return null
        val listEntry = entries.findLast { it.metadata.contains(SettingsTwoPaneScene.ListKey) } ?: return null

        // We use the list's contentKey to uniquely identify the scene.
        // This allows the detail panes to be displayed instantly through recomposition, rather than
        // having NavDisplay animate the whole scene out when the selected detail item changes.
        val sceneKey = listEntry.contentKey

        return SettingsTwoPaneScene(
            key = sceneKey,
            previousEntries = entries.dropLast(1),
            listEntry = listEntry,
            detailEntry = detailEntry,
        )
    }
}

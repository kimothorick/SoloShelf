package com.kimothorick.soloshelf.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.components.SearchBar
import com.kimothorick.soloshelf.ui.screens.Audiobooks
import com.kimothorick.soloshelf.ui.screens.Bookshelf
import com.kimothorick.soloshelf.ui.screens.Library
import com.kimothorick.soloshelf.ui.screens.settings.Settings

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
) {
    val rootBackStack = remember { mutableStateListOf<Route>(Route.Library) }

    NavDisplay(
        modifier = modifier,
        backStack = rootBackStack,
        onBack = { rootBackStack.removeAt(rootBackStack.lastIndex) },
        entryProvider = { key ->
            when (key) {
                is Route.Settings -> NavEntry(key) {
                    Settings(
                        onBack = { rootBackStack.removeAt(rootBackStack.lastIndex) },
                    )
                }

                else -> NavEntry(key) {
                    MainScreen(
                        onNavigateToSettings = { rootBackStack.add(Route.Settings) },
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topLevelBackStack = remember { TopLevelBackStack<Route>(Route.Library) }

    val bottomNavItems = listOf(
        Route.Library,
        Route.Bookshelf,
        Route.Audiobooks,
    )

    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    Scaffold(
        modifier = modifier,
        topBar = {
            SearchBar(
                state = searchBarState,
                textFieldState = textFieldState,
                onSearchTriggered = { },
                onNavigateToSettings = onNavigateToSettings,
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                bottomNavItems.forEach { screen ->
                    val isSelected = screen == topLevelBackStack.topLevelKey
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    is Route.Library -> Icons.AutoMirrored.Filled.MenuBook
                                    is Route.Bookshelf -> Icons.Filled.BarChart
                                    is Route.Audiobooks -> Icons.Filled.Headphones
                                    else -> error("Unknown nav item")
                                },
                                contentDescription = when (screen) {
                                    is Route.Library -> stringResource(R.string.library)
                                    is Route.Bookshelf -> stringResource(R.string.bookshelf)
                                    is Route.Audiobooks -> stringResource(R.string.audiobooks)
                                    else -> error("Unknown nav item")
                                },
                            )
                        },
                        label = {
                            Text(
                                text = when (screen) {
                                    is Route.Library -> stringResource(R.string.library)
                                    is Route.Bookshelf -> stringResource(R.string.bookshelf)
                                    is Route.Audiobooks -> stringResource(R.string.audiobooks)
                                    else -> error("Unknown nav item")
                                },
                            )
                        },
                        selected = isSelected,
                        onClick = { topLevelBackStack.addTopLevel(screen) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryProvider = { key ->
                when (key) {
                    is Route.Library -> NavEntry(key) { Library() }
                    is Route.Bookshelf -> NavEntry(key) { Bookshelf() }
                    is Route.Audiobooks -> NavEntry(key) { Audiobooks() }
                    else -> error("Unknown route in MainScreen")
                }
            },
        )
    }
}

class TopLevelBackStack<T : Any>(
    startKey: T,
) {
    // Maintain a stack for each top level route
    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey),
    )

    // Expose the current top level route for consumers
    var topLevelKey by mutableStateOf(startKey)
        private set

    // Expose the back stack so it can be rendered by the NavDisplay
    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    fun addTopLevel(
        key: T,
    ) {
        // If the top level doesn't exist, add it
        if (topLevelStacks[key] == null) {
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            // Otherwise just move it to the end of the stacks
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(
        key: T,
    ) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast() {
        val currentStack = topLevelStacks[topLevelKey]
        if (currentStack?.size == 1) {
            // Don't pop the last item in a top-level stack
            return
        }
        val removedKey = currentStack?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelKey = topLevelStacks.keys.last()
        updateBackStack()
    }
}

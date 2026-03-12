package com.kimothorick.soloshelf.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.kimothorick.soloshelf.ui.main.MainViewModel
import com.kimothorick.soloshelf.ui.screens.BookDetails
import com.kimothorick.soloshelf.ui.screens.main.MainScreen
import com.kimothorick.soloshelf.ui.screens.settings.Settings
import com.kimothorick.soloshelf.ui.screens.settings.SettingsViewModel

@Composable
fun NavigationRoot(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val rootBackStack = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { mutableStateListOf<Route>().apply { addAll(it) } },
        ),
    ) {
        mutableStateListOf<Route>(Route.Library)
    }

    NavDisplay(
        modifier = modifier,
        backStack = rootBackStack,
        onBack = { rootBackStack.removeAt(rootBackStack.lastIndex) },
        entryProvider = { key ->
            when (key) {
                is Route.Settings -> NavEntry(key) {
                    Settings(
                        viewModel = settingsViewModel,
                        onBack = { rootBackStack.removeAt(rootBackStack.lastIndex) },
                    )
                }

                is Route.BookDetails -> NavEntry(key) {
                    BookDetails(
                        bookId = key.bookId,
                        onBack = { rootBackStack.removeAt(rootBackStack.lastIndex) },
                        viewModel = hiltViewModel(key = key.bookId.toString())
                    )
                }

                else -> NavEntry(key) {
                    MainScreen(
                        onNavigateToSettings = { rootBackStack.add(Route.Settings) },
                        onNavigateToBookDetails = { bookId ->
                            rootBackStack.add(Route.BookDetails(bookId))
                        },
                        viewModel = mainViewModel,
                    )
                }
            }
        },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300),
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300),
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300),
            )
        },
    )
}

package com.kimothorick.soloshelf.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.kimothorick.soloshelf.ui.main.MainViewModel
import com.kimothorick.soloshelf.ui.screens.BookDetails
import com.kimothorick.soloshelf.ui.screens.main.MainScreen
import com.kimothorick.soloshelf.ui.screens.reader.ReaderScreen
import com.kimothorick.soloshelf.ui.screens.settings.AppearanceScreen
import com.kimothorick.soloshelf.ui.screens.settings.LibraryFoldersScreen
import com.kimothorick.soloshelf.ui.screens.settings.LibraryStorageScreen
import com.kimothorick.soloshelf.ui.screens.settings.LicensesScreen
import com.kimothorick.soloshelf.ui.screens.settings.Settings
import com.kimothorick.soloshelf.ui.screens.settings.SettingsTwoPaneScene
import com.kimothorick.soloshelf.ui.screens.settings.SettingsViewModel
import com.kimothorick.soloshelf.ui.screens.settings.rememberSettingsTwoPaneSceneStrategy
import com.kimothorick.soloshelf.util.isAtLeastMedium

data class MainDestination(
    val route: Route,
    val labelRes: Int,
    val icon: ImageVector,
)

@Composable
fun NavigationRoot(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val rootBackStack = rememberNavBackStack(Route.Library)

    val settingsTwoPaneStrategy = rememberSettingsTwoPaneSceneStrategy<NavKey>()

    val entryProvider = remember {
        entryProvider<NavKey> {
            entry<Route.Settings>(metadata = SettingsTwoPaneScene.listPane()) {
                val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
                val isWide = windowSizeClass.isAtLeastMedium

                Settings(
                    onBack = {
                        if (isWide) {
                            val settingsRoutes = setOf(
                                Route.Settings,
                                Route.Appearance,
                                Route.LibraryStorage,
                                Route.LibraryFolders,
                                Route.Licenses,
                            )
                            rootBackStack.removeAll { it in settingsRoutes }
                            if (!rootBackStack.contains(Route.Library)) {
                                rootBackStack.add(Route.Library)
                            }
                        } else {
                            rootBackStack.removeLastOrNull()
                        }
                    },
                    onNavigateToAppearance = {
                        rootBackStack.addSettingsDetail(Route.Appearance)
                    },
                    onNavigateToLibraryStorage = {
                        rootBackStack.addSettingsDetail(Route.LibraryStorage)
                    },
                    onNavigateToLicenses = {
                        rootBackStack.addSettingsDetail(Route.Licenses)
                    },
                )
            }

            entry<Route.Appearance>(metadata = SettingsTwoPaneScene.detailPane()) {
                AppearanceScreen(
                    viewModel = settingsViewModel,
                    onBack = { rootBackStack.removeLastOrNull() },
                )
            }

            entry<Route.LibraryStorage>(metadata = SettingsTwoPaneScene.detailPane()) {
                LibraryStorageScreen(
                    viewModel = settingsViewModel,
                    onBack = { rootBackStack.removeLastOrNull() },
                    onNavigateToLibraryFolders = {
                        rootBackStack.add(Route.LibraryFolders)
                    },
                )
            }

            entry<Route.LibraryFolders>(metadata = SettingsTwoPaneScene.detailPane()) {
                LibraryFoldersScreen(
                    viewModel = settingsViewModel,
                    onBack = { rootBackStack.removeLastOrNull() },
                )
            }

            entry<Route.Licenses>(metadata = SettingsTwoPaneScene.detailPane()) {
                LicensesScreen(
                    onBack = { rootBackStack.removeLastOrNull() },
                )
            }

            entry<Route.BookDetails> { key ->
                BookDetails(
                    bookId = key.bookId,
                    onBack = { rootBackStack.removeLastOrNull() },
                    onNavigateToReader = { bookId ->
                        rootBackStack.add(Route.Reader(bookId))
                    },
                    viewModel = hiltViewModel(key = key.bookId.toString()),
                )
            }

            entry<Route.Reader> { key ->
                val context = LocalContext.current
                val fragmentActivity = remember(context) {
                    var currentContext = context
                    while (currentContext is android.content.ContextWrapper) {
                        if (currentContext is FragmentActivity) break
                        currentContext = currentContext.baseContext
                    }
                    currentContext as? FragmentActivity
                }
                ReaderScreen(
                    bookId = key.bookId,
                    onBack = { rootBackStack.removeLastOrNull() },
                    viewModel = fragmentActivity?.let {
                        hiltViewModel(viewModelStoreOwner = it)
                    } ?: hiltViewModel(),
                )
            }

            entry<Route.Library> {
                MainScreen(
                    onNavigateToSettings = { rootBackStack.add(Route.Settings) },
                    onNavigateToReader = { bookId ->
                        rootBackStack.add(Route.Reader(bookId))
                    },
                    onNavigateToBookDetails = { bookId ->
                        rootBackStack.add(Route.BookDetails(bookId))
                    },
                    viewModel = mainViewModel,
                )
            }

            entry<Route.Bookshelf> {
                MainScreen(
                    onNavigateToSettings = { rootBackStack.add(Route.Settings) },
                    onNavigateToBookDetails = { bookId ->
                        rootBackStack.add(Route.BookDetails(bookId))
                    },
                    onNavigateToReader = { bookId ->
                        rootBackStack.add(Route.Reader(bookId))
                    },
                    viewModel = mainViewModel,
                )
            }

            entry<Route.Search> {
                MainScreen(
                    onNavigateToSettings = { rootBackStack.add(Route.Settings) },
                    onNavigateToBookDetails = { bookId ->
                        rootBackStack.add(Route.BookDetails(bookId))
                    },
                    onNavigateToReader = { bookId ->
                        rootBackStack.add(Route.Reader(bookId))
                    },
                    viewModel = mainViewModel,
                )
            }
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = rootBackStack,
        onBack = { rootBackStack.removeLastOrNull() },
        sceneStrategies = listOf(settingsTwoPaneStrategy),
        entryProvider = entryProvider,
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

private fun MutableList<NavKey>.addSettingsDetail(
    detail: Route,
) {
    val settingsDetails = listOf(Route.Appearance, Route.LibraryStorage, Route.Licenses)
    removeAll { it in settingsDetails }
    add(detail)
}

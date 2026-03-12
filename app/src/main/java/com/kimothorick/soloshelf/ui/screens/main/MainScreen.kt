package com.kimothorick.soloshelf.ui.screens.main

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.navigation.Route
import com.kimothorick.soloshelf.navigation.TopLevelBackStack
import com.kimothorick.soloshelf.ui.components.CreateCollectionDialog
import com.kimothorick.soloshelf.ui.components.MainAppBar
import com.kimothorick.soloshelf.ui.components.SoloShelfFab
import com.kimothorick.soloshelf.ui.components.SortBottomSheetContent
import com.kimothorick.soloshelf.ui.main.MainUiEvent
import com.kimothorick.soloshelf.ui.main.MainViewModel
import com.kimothorick.soloshelf.ui.screens.Audiobooks
import com.kimothorick.soloshelf.ui.screens.Bookshelf
import com.kimothorick.soloshelf.ui.screens.Library
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews
import kotlinx.coroutines.launch

private data class MainDestination(
    val route: Route,
    val labelRes: Int,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToBookDetails: (Long) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val topLevelBackStack = remember { TopLevelBackStack<Route>(Route.Library) }
    val sortOrder by viewModel.sortOrder.collectAsState()
    val context = LocalContext.current

    var isSortSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isCreateCollectionDialogVisible by rememberSaveable { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MainUiEvent.ShowToast -> {
                    val message = if (event.quantity != null) {
                        context.resources.getQuantityString(event.resId, event.quantity, *event.args.toTypedArray())
                    } else {
                        context.getString(event.resId, *event.args.toTypedArray())
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { viewModel.addBook(it) }
    }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)

    val destinations = remember {
        listOf(
            MainDestination(Route.Library, R.string.library, Icons.AutoMirrored.Filled.MenuBook),
            MainDestination(Route.Bookshelf, R.string.bookshelf, Icons.Filled.BarChart),
            MainDestination(Route.Audiobooks, R.string.audiobooks, Icons.Filled.Headphones),
        )
    }

    NavigationSuiteScaffoldLayout(
        navigationSuite = {
            when (layoutType) {
                NavigationSuiteType.NavigationRail -> {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.background,
                    ) {
                        Spacer(Modifier.weight(1f))
                        destinations.forEach { destination ->
                            Spacer(Modifier.height(2.dp))
                            NavigationRailItem(
                                selected = topLevelBackStack.topLevelKey == destination.route,
                                onClick = { topLevelBackStack.addTopLevel(destination.route) },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.labelRes),
                                    )
                                },
                                label = { Text(stringResource(destination.labelRes)) },
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }

                NavigationSuiteType.NavigationBar -> {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.background,
                    ) {
                        destinations.forEach { destination ->
                            NavigationBarItem(
                                selected = topLevelBackStack.topLevelKey == destination.route,
                                onClick = { topLevelBackStack.addTopLevel(destination.route) },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.labelRes),
                                    )
                                },
                                label = { Text(stringResource(destination.labelRes)) },
                            )
                        }
                    }
                }

                else -> {
                    NavigationSuite(
                        colors = NavigationSuiteDefaults.colors(
                            navigationDrawerContainerColor = MaterialTheme.colorScheme.background,
                            navigationBarContainerColor = MaterialTheme.colorScheme.background,
                            shortNavigationBarContainerColor = MaterialTheme.colorScheme.background,
                            navigationRailContainerColor = MaterialTheme.colorScheme.background,
                        ),
                    ) {
                        destinations.forEach { destination ->
                            item(
                                selected = topLevelBackStack.topLevelKey == destination.route,
                                onClick = { topLevelBackStack.addTopLevel(destination.route) },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.labelRes),
                                    )
                                },
                                label = { Text(stringResource(destination.labelRes)) },
                            )
                        }
                    }
                }
            }
        },
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                MainAppBar(
                    onNavigateToSettings = onNavigateToSettings,
                    onShowSortSheet = { isSortSheetVisible = true },
                )
            },
            floatingActionButton = {
                SoloShelfFab(
                    currentRoute = topLevelBackStack.topLevelKey,
                    onAddEbookFileClick = { filePickerLauncher.launch("application/*") },
                    onAddEbookFolderClick = { },
                    onAddAudiobookFileClick = { filePickerLauncher.launch("audio/*") },
                    onAddAudiobookFolderClick = { },
                    onCreateCollectionClick = { isCreateCollectionDialogVisible = true },
                )
            },
        ) { innerPadding ->
            NavDisplay(
                modifier = Modifier.padding(innerPadding),
                backStack = topLevelBackStack.backStack,
                onBack = { topLevelBackStack.removeLast() },
                entryProvider = { key ->
                    when (key) {
                        is Route.Library -> NavEntry(key) {
                            Library(onNavigateToBookDetails = onNavigateToBookDetails)
                        }

                        is Route.Bookshelf -> NavEntry(key) { Bookshelf() }

                        is Route.Audiobooks -> NavEntry(key) { Audiobooks() }

                        else -> error("Unknown route in MainScreen")
                    }
                },
            )

            if (isSortSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = { isSortSheetVisible = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = {
                        Spacer(
                            Modifier.padding(vertical = 12.dp).size(width = 32.dp, height = 4.dp).background(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape,
                            ),
                        )
                    },
                ) {
                    SortBottomSheetContent(
                        currentSortOrder = sortOrder,
                        onSortOrderSelected = viewModel::updateSortOrder,
                        onDone = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    isSortSheetVisible = false
                                }
                            }
                        },
                    )
                }
            }

            if (isCreateCollectionDialogVisible) {
                CreateCollectionDialog(
                    onDismissRequest = { isCreateCollectionDialogVisible = false },
                    onConfirm = { name ->
                        viewModel.createCollection(name)
                        isCreateCollectionDialogVisible = false
                    },
                )
            }
        }
    }
}

@DevicePreviews
@Composable
private fun MainScreenPreview() {
    SoloShelfTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}

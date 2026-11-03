package com.kimothorick.soloshelf.ui.screens.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.navigation.MainDestination
import com.kimothorick.soloshelf.navigation.Navigator
import com.kimothorick.soloshelf.navigation.Route
import com.kimothorick.soloshelf.navigation.rememberNavigationState
import com.kimothorick.soloshelf.navigation.toDecoratedEntries
import com.kimothorick.soloshelf.ui.components.CreateShelfDialog
import com.kimothorick.soloshelf.ui.components.FloatingBottomNavBar
import com.kimothorick.soloshelf.ui.components.ImportProgressBottomSheetContent
import com.kimothorick.soloshelf.ui.components.MainAppBar
import com.kimothorick.soloshelf.ui.components.SortBottomSheetContent
import com.kimothorick.soloshelf.ui.main.MainUiEvent
import com.kimothorick.soloshelf.ui.main.MainViewModel
import com.kimothorick.soloshelf.ui.screens.Bookshelf
import com.kimothorick.soloshelf.ui.screens.Library
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToBookDetails: (Long) -> Unit,
    onNavigateToReader: (Long) -> Unit,
    viewModel: MainViewModel,
) {
    val navigationState = rememberNavigationState(
        startRoute = Route.Library,
        topLevelRoutes = setOf(Route.Library, Route.Search, Route.Bookshelf),
    )
    val navigator = remember { Navigator(navigationState) }
    val sortOrder by viewModel.sortOrder.collectAsState()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var isSortSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isCreateShelfDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isFabExpanded by rememberSaveable { mutableStateOf(false) }
    var isFabVisible by remember { mutableStateOf(true) }

    BackHandler(enabled = isFabExpanded) {
        isFabExpanded = false
    }

    LaunchedEffect(navigationState.topLevelRoute) {
        isFabVisible = true
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y < -1) {
                    isFabVisible = false
                } else if (available.y > 1) {
                    isFabVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        confirmValueChange = {
            true
        },
    )

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MainUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val ebookPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addBooks(uris)
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let { viewModel.addLibraryFolder(it) }
    }

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)

    val destinations = remember {
        listOf(
            MainDestination(Route.Library, R.string.library, Icons.AutoMirrored.Rounded.MenuBook),
            MainDestination(Route.Search, R.string.search, Icons.Rounded.Search),
            MainDestination(Route.Bookshelf, R.string.bookshelf, Icons.Rounded.BarChart),
        )
    }

    NavigationSuiteScaffoldLayout(
        navigationSuite = {
            when (layoutType) {
                NavigationSuiteType.NavigationRail -> {
                    NavigationRail(
                        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
                    ) {
                        Spacer(Modifier.weight(1f))
                        destinations.forEach { destination ->
                            Spacer(Modifier.height(2.dp))
                            NavigationRailItem(
                                selected = navigationState.topLevelRoute == destination.route,
                                onClick = { navigator.navigate(destination.route) },
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
                    // Handled by Scaffold.bottomBar to ensure FAB is positioned correctly
                }

                else -> {
                    NavigationSuite(
                        colors = NavigationSuiteDefaults.colors(
                            navigationDrawerContainerColor = SoloShelfTheme.colorScheme.surfaceContainer,
                            navigationBarContainerColor = SoloShelfTheme.colorScheme.surfaceContainer,
                            shortNavigationBarContainerColor = SoloShelfTheme.colorScheme.surfaceContainer,
                            navigationRailContainerColor = SoloShelfTheme.colorScheme.surfaceContainer,
                        ),
                    ) {
                        destinations.forEach { destination ->
                            item(
                                selected = navigationState.topLevelRoute == destination.route,
                                onClick = { navigator.navigate(destination.route) },
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
            topBar = {
                MainAppBar(
                    onNavigateToSettings = onNavigateToSettings,
                    onShowSortSheet = { isSortSheetVisible = true },
                    onAddBookClick = {
                        ebookPickerLauncher.launch("*/*")
                    },
                    onAddFolderClick = { folderPickerLauncher.launch(null) },
                    onCreateShelfClick = { isCreateShelfDialogVisible = true },
                )
            },
            containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
            ) {
                val entryProvider = remember {
                    entryProvider<NavKey> {
                        entry<Route.Library> {
                            Library(
                                onNavigateToBookDetails = onNavigateToBookDetails,
                                onNavigateToReader = onNavigateToReader,
                                modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                            )
                        }
                        entry<Route.Search> { /* TODO: Implement Search */ }
                        entry<Route.Bookshelf> {
                            Bookshelf(
                                modifier = Modifier.padding(),
                            )
                        }
                    }
                }

                NavDisplay(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            end = innerPadding.calculateEndPadding(layoutDirection),
                            bottom = 0.dp,
                        ),
                    entries = navigationState.toDecoratedEntries(entryProvider),
                    onBack = { navigator.goBack() },
                )

                if (layoutType == NavigationSuiteType.NavigationBar) {
                    FloatingBottomNavBar(
                        destinations = destinations,
                        currentRoute = navigationState.topLevelRoute as Route,
                        onDestinationSelected = { route -> navigator.navigate(route) },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }

                if (isFabExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { isFabExpanded = false },
                    )
                }
            }

            if (isSortSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = { isSortSheetVisible = false },
                    sheetState = sheetState,
                    containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
                    dragHandle = {
                        Spacer(
                            Modifier
                                .padding(vertical = 12.dp)
                                .size(width = 32.dp, height = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape,
                                ),
                        )
                    },
                ) {
                    SortBottomSheetContent(
                        currentSortOrder = sortOrder,
                        onSortOrderSelected = viewModel::updateSortOrder,
                    )
                }
            }

            if (isCreateShelfDialogVisible) {
                CreateShelfDialog(
                    onDismissRequest = { isCreateShelfDialogVisible = false },
                    onConfirm = { name ->
                        viewModel.createShelf(name)
                        isCreateShelfDialogVisible = false
                    },
                )
            }

            if (importState.items.isNotEmpty()) {
                val importSheetState = rememberBottomSheetState(
                    initialValue = SheetValue.Expanded,
                    confirmValueChange = { targetValue: SheetValue ->
                        if (importState.isImporting && targetValue == SheetValue.Hidden) {
                            false
                        } else {
                            true
                        }
                    },
                )
                ModalBottomSheet(
                    onDismissRequest = {
                        if (!importState.isImporting) {
                            viewModel.clearImportState()
                        }
                    },
                    sheetState = importSheetState,
                    containerColor = SoloShelfTheme.colorScheme.surfaceContainerLowest,
                    dragHandle = {
                        Spacer(
                            Modifier
                                .padding(vertical = 12.dp)
                                .size(width = 32.dp, height = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape,
                                ),
                        )
                    },
                ) {
                    ImportProgressBottomSheetContent(
                        isImporting = importState.isImporting,
                        totalCount = importState.totalCount,
                        completedCount = importState.completedCount,
                        items = importState.items,
                        onDismiss = { viewModel.clearImportState() },
                    )
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun MainScreenPreview() {
    SoloShelfTheme {
        Box(modifier = Modifier.background(SoloShelfTheme.colorScheme.surfaceContainer))
    }
}

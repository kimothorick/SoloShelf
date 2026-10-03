package com.kimothorick.soloshelf.ui.screens.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.compose.foundation.layout.Column
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.navigation.MainDestination
import com.kimothorick.soloshelf.navigation.Navigator
import com.kimothorick.soloshelf.navigation.Route
import com.kimothorick.soloshelf.navigation.rememberNavigationState
import com.kimothorick.soloshelf.navigation.toDecoratedEntries
import com.kimothorick.soloshelf.ui.components.CreateCollectionDialog
import com.kimothorick.soloshelf.ui.components.MainAppBar
import com.kimothorick.soloshelf.ui.components.MiniPlayer
import com.kimothorick.soloshelf.ui.components.SoloShelfFab
import com.kimothorick.soloshelf.ui.components.SortBottomSheetContent
import com.kimothorick.soloshelf.ui.main.MainUiEvent
import com.kimothorick.soloshelf.ui.main.MainViewModel
import com.kimothorick.soloshelf.ui.screens.Bookshelf
import com.kimothorick.soloshelf.ui.screens.Library
import com.kimothorick.soloshelf.ui.screens.player.PlayerViewModel
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToBookDetails: (Long) -> Unit,
    onNavigateToPlayer: (Long) -> Unit = {},
    viewModel: MainViewModel,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val navigationState = rememberNavigationState(
        startRoute = Route.Library,
        topLevelRoutes = setOf(Route.Library, Route.Search, Route.Bookshelf),
    )
    val navigator = remember { Navigator(navigationState) }
    val sortOrder by viewModel.sortOrder.collectAsState()
    val context = LocalContext.current

    var isSortSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isCreateCollectionDialogVisible by rememberSaveable { mutableStateOf(false) }
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
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.addBooks(listOf(it)) }
    }

    val audiobookPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addBooks(uris)
        }
    }

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)

    val destinations = remember {
        listOf(
            MainDestination(Route.Library, R.string.library, Icons.AutoMirrored.Filled.MenuBook),
            MainDestination(Route.Search, R.string.search, Icons.Filled.Search),
            MainDestination(Route.Bookshelf, R.string.bookshelf, Icons.Filled.BarChart),
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
                )
            },
            bottomBar = {
                Column {
                    val playerUiState by playerViewModel.uiState.collectAsState()
                    if (playerUiState.bookId != null && playerUiState.bookTitle.isNotEmpty()) {
                        MiniPlayer(
                            uiState = playerUiState,
                            onPlayPauseClick = { playerViewModel.togglePlayPause() },
                            onClick = {
                                playerUiState.bookId?.let { bookId ->
                                    onNavigateToPlayer(bookId)
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                    if (layoutType == NavigationSuiteType.NavigationBar) {
                        NavigationBar(
                            containerColor = SoloShelfTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            destinations.forEach { destination ->
                                NavigationBarItem(
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
            floatingActionButton = {
                val currentRoute = navigationState.topLevelRoute as Route
                if (currentRoute != Route.Search) {
                    AnimatedVisibility(
                        visible = isFabVisible || isFabExpanded,
                        // Combines fade and scale with a slow 800ms duration
                        enter = fadeIn(animationSpec = tween(durationMillis = 800)) + scaleIn(
                            animationSpec = tween(
                                durationMillis = 400,
                            ),
                        ),
                        exit = fadeOut(animationSpec = tween(durationMillis = 800)) + scaleOut(
                            animationSpec = tween(durationMillis = 400),
                        ),
                    ) {
                        SoloShelfFab(
                            currentRoute = currentRoute,
                            expanded = isFabExpanded,
                            onExpandedChange = { isFabExpanded = it },
                            onAddEbookFileClick = {
                                ebookPickerLauncher.launch(
                                    arrayOf(
                                        "application/epub+zip",
                                        "application/pdf",
                                        "application/zip",
                                    ),
                                )
                            },
                            onAddAudiobookFileClick = {
                                audiobookPickerLauncher.launch(
                                    arrayOf(
                                        "audio/*",
                                        "application/audiobook+zip",
                                        "application/audiobook+json",
                                        "application/x-lpf+zip",
                                        "application/zip",
                                    ),
                                )
                            },
                            onCreateCollectionClick = { isCreateCollectionDialogVisible = true },
                        )
                    }
                }
            },
            containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
            ) {
                val entryProvider = remember {
                    entryProvider<NavKey> {
                        entry<Route.Library> {
                            Library(
                                onNavigateToBookDetails = onNavigateToBookDetails,
                                onNavigateToPlayer = onNavigateToPlayer,
                                modifier = Modifier,
                            )
                        }
                        entry<Route.Search> { /* TODO: Implement Search */ }
                        entry<Route.Bookshelf> {
                            Bookshelf(modifier = Modifier)
                        }
                    }
                }

                NavDisplay(
                    modifier = Modifier.padding(innerPadding),
                    entries = navigationState.toDecoratedEntries(entryProvider),
                    onBack = { navigator.goBack() },
                )

                if (isFabExpanded) {
                    Box(
                        modifier = Modifier.fillMaxSize().clickable(
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
        Box(modifier = Modifier.background(SoloShelfTheme.colorScheme.surfaceContainer))
    }
}

package com.kimothorick.soloshelf.ui.screens.reader

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.components.CircularIconButton
import com.kimothorick.soloshelf.ui.screens.reader.components.ReaderActionsPanel
import com.kimothorick.soloshelf.ui.screens.reader.components.TableOfContents

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val book by viewModel.book.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val publication by viewModel.publication.collectAsState()
    val showBars by viewModel.showBars.collectAsState()
    val showToc by viewModel.showToc.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()

    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    val useSidebar = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    val view = LocalView.current
    LaunchedEffect(showBars, showToc, showSettings, useSidebar) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)

        // Ensure content spans the whole screen including cutouts
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (showBars && !showToc && !showSettings) {
            controller.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val window = (view.context as? Activity)?.window ?: return@onDispose
            val controller = WindowCompat.getInsetsController(window, view)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    BackHandler {
        when {
            showToc -> viewModel.toggleToc()
            showSettings -> viewModel.toggleSettings()
            showBars -> viewModel.toggleBars()
            else -> onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (isLoading || book?.bookId != bookId) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar for Expanded Screens
                if (useSidebar) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showToc,
                        enter = slideInHorizontally() + fadeIn(),
                        exit = slideOutHorizontally() + fadeOut(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(320.dp)
                                .background(MaterialTheme.colorScheme.surface)
                                .statusBarsPadding()
                                .padding(16.dp),
                        ) {
                            // Back button
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.padding(bottom = 16.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "All Books",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            publication?.let { pub ->
                                TableOfContents(
                                    toc = pub.tableOfContents,
                                    onLinkSelected = { viewModel.onTocItemSelected(it) },
                                )
                            }
                        }
                    }
                }

                // Main Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    // Reader content (Always Full Screen)
                    androidx.fragment.compose.AndroidFragment<ReaderFragment>(
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Overlays
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Click outside to dismiss settings panel
                        if (showSettings) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(45f)
                                    .clickable(
                                        interactionSource = remember {
                                            androidx.compose.foundation.interaction
                                                .MutableInteractionSource()
                                        },
                                        indication = null,
                                    ) { viewModel.toggleSettings() },
                            )
                        }

                        // Actions Panel (Bottom Bar or Settings) - Fixed at Bottom Overlay
                        ReaderActionsPanel(
                            viewModel = viewModel,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(60f),
                        )

                        // Top Bar (Overlay)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showBars && (!useSidebar || !showToc) && !showSettings,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .zIndex(40f),
                        ) {
                            ReaderTopBar(
                                title = book?.title ?: "",
                                onBack = onBack,
                                onSearch = { /* TODO */ },
                            )
                        }

                        // Compact Overlay TOC (For phone/compact screens)
                        if (showToc && !useSidebar) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(320.dp)
                                    .zIndex(50f)
                                    .align(Alignment.CenterStart),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                            ) {
                                publication?.let { pub ->
                                    TableOfContents(
                                        toc = pub.tableOfContents,
                                        onLinkSelected = { viewModel.onTocItemSelected(it) },
                                    )
                                }
                            }
                        }

                        // Dimmed background to dismiss TOC
                        if (showToc) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(48f)
                                    .background(
                                        if (!useSidebar) {
                                            Color.Black.copy(alpha = 0.3f)
                                        } else {
                                            Color.Transparent
                                        },
                                    ).clickable { viewModel.toggleToc() },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopBar(
    title: String,
    onBack: () -> Unit,
    onSearch: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularIconButton(
                icon = R.drawable.ic_arrow_back,
                label = stringResource(R.string.back),
                onClick = onBack,
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                maxLines = 1,
            )

            CircularIconButton(
                icon = R.drawable.ic_search,
                label = "Search",
                onClick = onSearch,
            )
        }
    }
}

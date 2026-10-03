package com.kimothorick.soloshelf.ui.screens.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.components.ToggleableButtonGroup
import com.kimothorick.soloshelf.ui.components.ToggleableButtonItem
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerScreen(
    bookId: Long,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    var showTimerSheet by remember { mutableStateOf(false) }
    var showChaptersSheet by remember { mutableStateOf(false) }

    val seedColor = rememberSeedColor(uiState.coverPath)
    val dynamicColorScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = true,
        style = PaletteStyle.Vibrant,
    )

    MaterialTheme(colorScheme = dynamicColorScheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        seedColor.copy(alpha = 0.5f),
                        dynamicColorScheme.surface,
                        dynamicColorScheme.surfaceContainer,
                        dynamicColorScheme.surfaceContainerLowest,
                    ),
                ),
            ),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            shape = MaterialTheme.shapes.extraExtraLarge,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f),
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Dismiss",
                            )
                        }
                    },
                    title = { Text(text = "Now Playing", style = SoloShelfTheme.typography.titleSmall) },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Book Title & Author
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = uiState.bookTitle.ifEmpty { "Audiobook Title" },
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = uiState.author.ifEmpty { "Author Name" },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = uiState.totalRemainingTimeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Cover Artwork
                AsyncImage(
                    model = uiState.coverPath,
                    contentDescription = null,
                    modifier = Modifier.size(310.dp).clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.book_icon),
                    error = painterResource(R.drawable.book_icon),
                )

                Spacer(modifier = Modifier.weight(1f))

                // Chapter Title
                Text(
                    text = uiState.currentChapterTitle.ifEmpty { "Chapter 1" },
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Seekable Linear Determinate Progress Indicator
                var isDragging by remember { mutableStateOf(false) }
                var dragProgress by remember { mutableFloatStateOf(0f) }
                val displayProgress = if (isDragging) dragProgress else uiState.progress

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val p = (offset.x / size.width).coerceIn(0f, 1f)
                                viewModel.seekTo(p)
                            }
                        }.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                },
                                onDragEnd = {
                                    viewModel.seekTo(dragProgress)
                                    isDragging = false
                                },
                                onDragCancel = {
                                    isDragging = false
                                },
                                onHorizontalDrag = { change, _ ->
                                    change.consume()
                                    dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    LinearProgressIndicator(
                        progress = { displayProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                }

                // Timers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = uiState.currentTimeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    )

                    Text(
                        text = uiState.remainingTimeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                MainMediaControls(
                    isPlaying = uiState.isPlaying,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onRewindClick = { viewModel.rewind10s() },
                    onForwardClick = { viewModel.forward10s() },
                    onNextClick = { viewModel.nextTrack() },
                    onPreviousClick = { viewModel.previousTrack() },
                )
                Spacer(modifier = Modifier.weight(0.8f))

                BottomBarMediaControls(
                    playbackSpeed = uiState.playbackSpeed,
                    sleepTimerMinutes = uiState.sleepTimerMinutes,
                    onSpeedClick = { viewModel.cyclePlaybackSpeed() },
                    onTimerClick = { showTimerSheet = true },
                    onBookmarkClick = { viewModel.addBookmark() },
                    onChaptersClick = { showChaptersSheet = true },
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
        }

        // Sleep Timer Sheet
        if (showTimerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTimerSheet = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                ) {
                    Text(
                        text = "Sleep Timer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val timerOptions = listOf(
                        "Off" to null,
                        "5 minutes" to 5,
                        "10 minutes" to 10,
                        "15 minutes" to 15,
                        "30 minutes" to 30,
                        "45 minutes" to 45,
                        "60 minutes" to 60,
                    )
                    timerOptions.forEach { (label, minutes) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            trailingContent = {
                                RadioButton(
                                    selected = uiState.sleepTimerMinutes == minutes,
                                    onClick = null,
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable {
                                viewModel.setSleepTimer(minutes)
                                showTimerSheet = false
                            },
                        )
                    }
                }
            }
        }

        // Chapters Sheet
        if (showChaptersSheet) {
            var selectedTab by remember { mutableStateOf(0) }
            ModalBottomSheet(
                onDismissRequest = { showChaptersSheet = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                ) {
                    Text(
                        text = "Table of Contents",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    val totalDurationSeconds = uiState.tracks.sumOf { it.duration }
                    val totalDurationFormatted =
                        com.kimothorick.soloshelf.util.TimeUtils
                            .formatDuration(totalDurationSeconds * 1000L)
                    val playedSeconds = uiState.tracks
                        .take(uiState.currentTrackIndex)
                        .sumOf { it.duration } + (
                        uiState.progress * (
                            uiState.tracks.getOrNull(uiState.currentTrackIndex)?.duration
                                ?: 0
                        )
                    ).toLong()
                    val progressPercent =
                        if (totalDurationSeconds > 0) {
                            ((playedSeconds * 100) / totalDurationSeconds).coerceIn(
                                0,
                                100,
                            )
                        } else {
                            0
                        }

                    Text(
                        text = "$progressPercent% completed • Total runtime $totalDurationFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    )

                    // Pill Tabs row using ToggleableButtonGroup
                    val items = listOf(
                        ToggleableButtonItem("Chapters (${uiState.tracks.size})", selectedTab == 0, 0),
                        ToggleableButtonItem("Bookmarks", selectedTab == 1, 1),
                    )

                    ToggleableButtonGroup(
                        items = items,
                        onItemSelected = { item ->
                            selectedTab = item.identifier as Int
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    when (selectedTab) {
                        0 -> {
                            if (uiState.tracks.isEmpty()) {
                                Text(
                                    text = "No chapters found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    itemsIndexed(uiState.tracks) { index, track ->
                                        val isSelected = index == uiState.currentTrackIndex
                                        val durationMin = maxOf(1, track.duration / 60)
                                        Card(
                                            onClick = {
                                                viewModel.selectTrack(index)
                                                showChaptersSheet = false
                                            },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) {
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.4f,
                                                    )
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                },
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    modifier = Modifier.weight(1f),
                                                ) {
                                                    Text(
                                                        text = String.format(java.util.Locale.US, "%02d", index + 1),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    Column {
                                                        Text(
                                                            text = track.title,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                        Text(
                                                            text = if (isSelected) "Playing • $durationMin min" else "$durationMin min",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    }
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.selectTrack(index)
                                                        showChaptersSheet = false
                                                    },
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected && uiState.isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                                                        contentDescription = null,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No bookmarks yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainMediaControls(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier.fillMaxWidth().background(color = Color.Transparent),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPreviousClick()
            },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(28.dp),
            )
        }

        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onRewindClick()
            },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Replay10,
                contentDescription = "Rewind 10s",
                modifier = Modifier.size(28.dp),
            )
        }

        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPlayPauseClick()
            },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier.size(72.dp),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.fillMaxSize(),
            )
        }

        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onForwardClick()
            },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Forward10,
                contentDescription = "Forward 10s",
                modifier = Modifier.size(28.dp),
            )
        }

        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onNextClick()
            },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomBarMediaControls(
    playbackSpeed: Float,
    sleepTimerMinutes: Int?,
    onSpeedClick: () -> Unit,
    onTimerClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onChaptersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .clip(shape = CircleShape)
            .background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSpeedClick()
            },
            modifier = Modifier.size(
                IconButtonDefaults.mediumContainerSize(
                    widthOption = IconButtonDefaults.IconButtonWidthOption.Wide,
                ),
            ),
        ) {
            val speedText = if (playbackSpeed % 1f == 0f) "${playbackSpeed.toInt()}x" else "${playbackSpeed}x"
            AnimatedContent(
                targetState = speedText,
                transitionSpec = {
                    (fadeIn() + scaleIn() + slideInVertically()).togetherWith(
                        (fadeOut() + scaleOut() + slideOutVertically { it / 2 }),
                    )
                },
                label = "SpeedText",
            ) { targetSpeed ->
                Text(
                    text = targetSpeed,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        IconButton(
            onClick = onTimerClick,
            modifier = Modifier.size(
                IconButtonDefaults.mediumContainerSize(
                    widthOption = IconButtonDefaults.IconButtonWidthOption.Wide,
                ),
            ),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = "Sleep Timer",
            )
        }

        IconButton(
            onClick = onBookmarkClick,
            modifier = Modifier.size(
                IconButtonDefaults.mediumContainerSize(
                    widthOption = IconButtonDefaults.IconButtonWidthOption.Wide,
                ),
            ),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.BookmarkBorder,
                contentDescription = "Bookmark",
            )
        }

        IconButton(
            onClick = onChaptersClick,
            modifier = Modifier.size(
                IconButtonDefaults.mediumContainerSize(
                    widthOption = IconButtonDefaults.IconButtonWidthOption.Wide,
                ),
            ),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.FormatListBulleted,
                contentDescription = "Chapters",
            )
        }
    }
}

@Composable
private fun rememberSeedColor(
    coverPath: String?,
): Color {
    val context = LocalContext.current
    var seedColor by remember { mutableStateOf(Color(0xFF006688)) }

    LaunchedEffect(coverPath) {
        if (!coverPath.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val bitmap = getBitmapFromPath(context, coverPath)
                    if (bitmap != null) {
                        val extracted = calculateDominantColor(bitmap)
                        withContext(Dispatchers.Main) {
                            seedColor = extracted
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
    return seedColor
}

private fun getBitmapFromPath(
    context: Context,
    path: String,
): Bitmap? =
    try {
        if (path.startsWith("content://")) {
            val uri = Uri.parse(path)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } else {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                BitmapFactory.decodeFile(path)
            }
        }
    } catch (_: Exception) {
        null
    }

private fun calculateDominantColor(
    bitmap: Bitmap,
): Color {
    val width = 32
    val height = 32
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

    var maxSat = -1f
    var bestColor = Color(0xFF006688)

    var totalR = 0L
    var totalG = 0L
    var totalB = 0L
    var validPixelCount = 0

    for (x in 0 until width) {
        for (y in 0 until height) {
            val pixel = scaledBitmap.getPixel(x, y)
            val alpha = (pixel shr 24) and 0xFF
            if (alpha < 128) continue

            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            totalR += r
            totalG += g
            totalB += b
            validPixelCount++

            val hsv = FloatArray(3)
            android.graphics.Color.RGBToHSV(r, g, b, hsv)
            val saturation = hsv[1]
            val value = hsv[2]

            if (saturation > maxSat && value in 0.2f..0.9f) {
                maxSat = saturation
                bestColor = Color(r, g, b)
            }
        }
    }

    if (maxSat < 0.12f && validPixelCount > 0) {
        val avgR = (totalR / validPixelCount).toInt()
        val avgG = (totalG / validPixelCount).toInt()
        val avgB = (totalB / validPixelCount).toInt()
        bestColor = Color(avgR, avgG, avgB)
    }

    if (scaledBitmap != bitmap) {
        scaledBitmap.recycle()
    }

    return bestColor
}

@Preview
@Composable
fun MainMediaControlsPreview(
    modifier: Modifier = Modifier,
) {
    SoloShelfTheme {
        MainMediaControls(
            isPlaying = false,
            onPlayPauseClick = {},
            onRewindClick = {},
            onForwardClick = {},
            onNextClick = {},
            onPreviousClick = {},
            modifier = modifier,
        )
    }
}

@Preview
@Composable
fun BottomBarMediaControlsPreview(
    modifier: Modifier = Modifier,
) {
    SoloShelfTheme {
        BottomBarMediaControls(
            playbackSpeed = 1.0f,
            sleepTimerMinutes = null,
            onSpeedClick = {},
            onTimerClick = {},
            onBookmarkClick = {},
            onChaptersClick = {},
            modifier = modifier,
        )
    }
}

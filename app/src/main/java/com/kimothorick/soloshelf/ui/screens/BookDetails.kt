package com.kimothorick.soloshelf.ui.screens

import android.content.Context
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.ui.components.ChapterItem
import com.kimothorick.soloshelf.ui.components.CircularIconButton
import com.kimothorick.soloshelf.ui.components.CollapsingLayout
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.CombinedPreviews
import com.kimothorick.soloshelf.ui.tooling.PreviewData
import com.kimothorick.soloshelf.util.FileUtils
import com.kimothorick.soloshelf.util.NumberUtils
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.Link
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun BookDetails(
    bookId: Long,
    onBack: () -> Unit,
    onNavigateToReader: (Long) -> Unit,
    onNavigateToPlayer: (Long) -> Unit,
    viewModel: BookDetailsViewModel = hiltViewModel(),
) {
    val bookAndProgress by viewModel.bookAndProgress.collectAsStateWithLifecycle()
    val tableOfContents by viewModel.tableOfContents.collectAsStateWithLifecycle()

    LaunchedEffect(bookId) {
        viewModel.setBookId(bookId)
    }

    bookAndProgress?.let { currentBookAndProgress ->
        BookDetailsScreen(
            bookAndProgress = currentBookAndProgress,
            tableOfContents = tableOfContents,
            onBack = onBack,
            onNavigateToReader = onNavigateToReader,
            onNavigateToPlayer = onNavigateToPlayer,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailsScreen(
    bookAndProgress: BookAndProgress,
    tableOfContents: List<Link>,
    onBack: () -> Unit,
    onNavigateToReader: (Long) -> Unit,
    onNavigateToPlayer: (Long) -> Unit,
    modifier: Modifier = Modifier,
    fileSizeOverride: String? = null,
    modifiedDateOverride: String? = null,
) {
    val book = bookAndProgress.book
    val progress = bookAndProgress.progress

    val tabs = listOf(
        stringResource(R.string.about),
        stringResource(R.string.contents),
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val contentsLazyListState = rememberLazyListState()
    val size = SplitButtonDefaults.MediumContainerHeight
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val totalBottomSpacing = size + bottomInset + 16.dp

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    CircularIconButton(
                        modifier = Modifier.padding(start = 12.dp),
                        icon = R.drawable.ic_arrow_back,
                        label = stringResource(R.string.back),
                        onClick = onBack,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = SoloShelfTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            CollapsingLayout(
                modifier = Modifier.padding(
                    innerPadding,
                ),
                header = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        val coverWidth = 220.dp
                        val coverHeight = if (book.isAudiobook) 220.dp else 320.dp
                        AsyncImage(
                            model = remember(book.coverPath) {
                                book.coverPath?.let { path ->
                                    if (path.startsWith("/") && !path.startsWith("file://")) "file://$path" else path
                                }
                            },
                            contentDescription = book.title,
                            modifier = Modifier
                                .size(width = coverWidth, height = coverHeight)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.book_icon),
                            error = painterResource(R.drawable.book_icon),
                            onError = { state ->
                                Log.e(
                                    "BookDetails",
                                    "Error loading cover image for ${book.title}: ${state.result.throwable.message}",
                                )
                            },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                body = {
                    Column(modifier = Modifier.fillMaxSize()) {
                        PrimaryScrollableTabRow(
                            containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
                            selectedTabIndex = pagerState.currentPage,
                            divider = {
                                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                            },
                            indicator = {
                                TabRowDefaults.PrimaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                                    width = 32.dp,
                                    shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                                )
                            },
                            edgePadding = 0.dp,
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium,
                                        )
                                    },
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top,
                        ) { page ->
                            when (page) {
                                0 -> {
                                    BookDetailsContent(
                                        bookAndProgress = bookAndProgress,
                                        fileSizeOverride = fileSizeOverride,
                                        modifiedDateOverride = modifiedDateOverride,
                                        bottomPadding = totalBottomSpacing,
                                        // modifier = Modifier.padding(top = 16.dp),
                                    )
                                }

                                1 -> {
                                    if (tableOfContents.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = stringResource(R.string.no_contents_available),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            state = contentsLazyListState,
                                            contentPadding = PaddingValues(bottom = totalBottomSpacing),
                                        ) {
                                            items(tableOfContents) { link ->
                                                ChapterItem(link = link)
                                                HorizontalDivider(
                                                    thickness = 0.5.dp,
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                        alpha = 0.5f,
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            )

            val progressText = if (progress == null) {
                stringResource(R.string.start_reading)
            } else {
                stringResource(R.string.continue_reading_progress, progress.readPercentage.toInt())
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        ),
                    ).padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                SplitButtonLayout(
                    leadingButton = {
                        SplitButtonDefaults.TonalLeadingButton(
                            onClick = {
                                if (book.isAudiobook) {
                                    onNavigateToPlayer(book.bookId)
                                } else {
                                    onNavigateToReader(book.bookId)
                                }
                            },
                            modifier = Modifier.heightIn(size),
                            shapes = SplitButtonDefaults.leadingButtonShapesFor(size),
                            contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(size),
                        ) {
                            Text(
                                text = progressText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp),
                            )
                        }
                    },
                    trailingButton = {
                        val tooltipState = rememberTooltipState()
                        val description = "Toggle Button"
                        var checked by remember { mutableStateOf(false) }
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                positioning = TooltipAnchorPosition.Above,
                            ),
                            tooltip = {
                                PlainTooltip {
                                    Text(stringResource(R.string.add_to_collection))
                                }
                            },
                            state = tooltipState,
                        ) {
                            SplitButtonDefaults.TonalTrailingButton(
                                checked = checked,
                                onCheckedChange = { checked = it },
                                modifier =
                                    Modifier.heightIn(size).semantics {
                                        stateDescription = if (checked) "Expanded" else "Collapsed"
                                        contentDescription = description
                                    },
                                shapes = SplitButtonDefaults.trailingButtonShapesFor(size),
                                contentPadding = SplitButtonDefaults.trailingButtonContentPaddingFor(size),
                            ) {
                                val rotation: Float by
                                    animateFloatAsState(
                                        targetValue = if (checked) 180f else 0f,
                                        label = "Trailing Icon Rotation",
                                    )
                                Icon(
                                    Icons.Rounded.KeyboardArrowUp,
                                    modifier =
                                        Modifier
                                            .size(SplitButtonDefaults.trailingButtonIconSizeFor(size))
                                            .graphicsLayer { this.rotationZ = rotation },
                                    contentDescription = "Localized description",
                                )
                            }
                        }
                    },
                    spacing = 2.dp,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookDetailsContent(
    bookAndProgress: BookAndProgress,
    modifier: Modifier = Modifier,
    fileSizeOverride: String? = null,
    modifiedDateOverride: String? = null,
    bottomPadding: Dp = 0.dp,
) {
    val book = bookAndProgress.book

    var isDescriptionTextExpanded by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        // Title and Author
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = book.author.ifBlank {
                stringResource(R.string.unknown_author)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Synopsis
        SectionHeader(stringResource(R.string.synopsis))
        Spacer(modifier = Modifier.height(8.dp))

        val synopsis = book.synopsis ?: stringResource(R.string.no_synopsis_available)

        Column(
            modifier = Modifier.animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing,
                ),
            ),
        ) {
            Text(
                text = AnnotatedString.fromHtml(synopsis.replace("\n", "<br/>")),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Start,
                maxLines = if (isDescriptionTextExpanded) Int.MAX_VALUE else 10,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            if (synopsis != stringResource(R.string.no_synopsis_available) && synopsis.length > 300) {
                Text(
                    text = if (isDescriptionTextExpanded) "Show less" else "Show more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { isDescriptionTextExpanded = !isDescriptionTextExpanded },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Details (Publisher, Language, Year, Identifier)
        SectionHeader(stringResource(R.string.details))
        Spacer(modifier = Modifier.height(8.dp))
        MetadataGrid(bookAndProgress)
        Spacer(modifier = Modifier.height(16.dp))

        // File Details
        SectionHeader(stringResource(R.string.file_details))
        Spacer(modifier = Modifier.height(12.dp))
        FileDetailsCard(
            book = book,
            fileSizeOverride = fileSizeOverride,
            modifiedDateOverride = modifiedDateOverride,
        )

        if (book.genres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            // Genres
            SectionHeader(stringResource(R.string.genres))
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                book.genres.forEach { genre ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(text = genre, style = MaterialTheme.typography.labelMedium) },
                        shape = RoundedCornerShape(12.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = null,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(bottomPadding))
    }
}

@Composable
private fun MetadataGrid(
    bookAndProgress: BookAndProgress,
) {
    val book = bookAndProgress.book
    val year = remember(book.publicationDate) {
        extractYear(book.publicationDate)
    }

    val pageCount = if (book.isAudiobook) {
        (bookAndProgress.audiobookDetails?.totalDurationMs ?: 0) / 1000 / 60 // show minutes for audiobooks?
    } else {
        bookAndProgress.ebookDetails?.pageCount ?: 0
    }

    val pagesLabel = if (book.isAudiobook) stringResource(R.string.duration) else stringResource(R.string.pages)
    val pagesValue = if (book.isAudiobook) {
        val totalSeconds = (bookAndProgress.audiobookDetails?.totalDurationMs ?: 0) / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    } else {
        NumberUtils.formatLocalizedInteger(pageCount.toInt())
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetadataGridItem(
                label = pagesLabel,
                value = pagesValue,
                modifier = Modifier.weight(1f),
            )
            if (book.isAudiobook) {
                MetadataGridItem(
                    label = "Narrator",
                    value = bookAndProgress.audiobookDetails?.narrator ?: stringResource(R.string.unknown),
                    modifier = Modifier.weight(1f),
                )
            } else {
                MetadataGridItem(
                    label = stringResource(R.string.language),
                    value = formatLanguage(book.language) ?: stringResource(R.string.unknown),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetadataGridItem(
                label = stringResource(R.string.year),
                value = year ?: stringResource(R.string.unknown),
                modifier = Modifier.weight(1f),
            )
            MetadataGridItem(
                label = stringResource(R.string.publisher),
                value = book.publisher ?: stringResource(R.string.unknown),
                modifier = Modifier.weight(1f),
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetadataGridItem(
                label = stringResource(R.string.identifier),
                value = book.isbn ?: stringResource(R.string.unknown),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetadataGridItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start,
    )
}

@Composable
private fun FileDetailsCard(
    book: Book,
    fileSizeOverride: String? = null,
    modifiedDateOverride: String? = null,
) {
    val context = LocalContext.current
    val isUri = remember(book.filePath) {
        book.filePath.startsWith("content://") || book.filePath.startsWith("file://")
    }

    val fileSize = fileSizeOverride ?: remember(book.filePath) {
        val size = if (isUri) {
            getFileSizeFromUri(context, book.filePath)
        } else {
            File(book.filePath).length()
        }
        formatFileSize(size)
    }

    val modifiedDate = modifiedDateOverride ?: remember(book.filePath) {
        val time = if (isUri) {
            getFileLastModifiedFromUri(context, book.filePath)
        } else {
            File(book.filePath).lastModified()
        }
        if (time > 0) formatDate(time) else null
    } ?: stringResource(R.string.unknown)

    val importedDate = remember(book.dateAdded) { formatDate(book.dateAdded) }
    val prettyPath = remember(book.filePath) { FileUtils.getPrettyPathFromUri(book.filePath) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            FileDetailItem(
                icon = R.drawable.ic_incomplete_circle,
                label = stringResource(R.string.size),
                value = fileSize,
                modifier = Modifier.weight(1f),
            )
            FileDetailItem(
                icon = R.drawable.ic_description,
                label = stringResource(R.string.format),
                value = book.format.uppercase(),
                modifier = Modifier.weight(1f),
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            FileDetailItem(
                icon = R.drawable.ic_calendar_today,
                label = stringResource(R.string.imported),
                value = importedDate,
                modifier = Modifier.weight(1f),
            )
            FileDetailItem(
                icon = R.drawable.ic_history,
                label = stringResource(R.string.modified),
                value = modifiedDate,
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        FileDetailItem(
            icon = R.drawable.ic_folder,
            label = stringResource(R.string.location),
            value = prettyPath,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FileDetailItem(
    icon: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun getFileSizeFromUri(
    context: Context,
    uriString: String,
): Long =
    try {
        val uri = uriString.toUri()
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                cursor.getLong(sizeIndex)
            } else {
                0L
            }
        } ?: 0L
    } catch (_: Exception) {
        0L
    }

private fun getFileLastModifiedFromUri(
    context: Context,
    uriString: String,
): Long =
    try {
        val uri = uriString.toUri()
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val dateIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            if (dateIndex != -1 && cursor.moveToFirst()) {
                cursor.getLong(dateIndex)
            } else {
                0L
            }
        } ?: 0L
    } catch (_: Exception) {
        0L
    }

private fun formatFileSize(
    size: Long,
): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1 -> String.format(Locale.US, "%.2f MB", mb)
        kb >= 1 -> String.format(Locale.US, "%.2f KB", kb)
        else -> String.format(Locale.US, "%d B", size)
    }
}

private fun formatDate(
    timestamp: Long,
): String {
    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    return date.format(formatter)
}

private fun extractYear(
    dateString: String?,
): String? {
    if (dateString.isNullOrBlank()) return null

    val formats = listOf(
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy-MM"),
        DateTimeFormatter.ofPattern("yyyy"),
    )

    for (formatter in formats) {
        try {
            return when (formatter) {
                DateTimeFormatter.ISO_OFFSET_DATE_TIME -> {
                    OffsetDateTime.parse(dateString, formatter).year.toString()
                }

                DateTimeFormatter.ISO_DATE_TIME -> {
                    ZonedDateTime.parse(dateString, formatter).year.toString()
                }

                else -> {
                    val accessor = formatter.parse(dateString)
                    if (accessor.isSupported(ChronoField.YEAR)) {
                        accessor.get(ChronoField.YEAR).toString()
                    } else {
                        null
                    }
                }
            }
        } catch (_: Exception) {
            continue
        }
    }
    return null
}

private fun formatLanguage(
    languageCode: String?,
): String? {
    if (languageCode.isNullOrBlank()) return null
    return Locale.forLanguageTag(languageCode).displayLanguage.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

@CombinedPreviews
@Composable
private fun BookDetailsPreview() {
    SoloShelfTheme {
        Surface {
            BookDetailsScreen(
                bookAndProgress = BookAndProgress(
                    book = PreviewData.sampleBook,
                    progress = null,
                    ebookDetails = null,
                    audiobookDetails = null,
                ),
                tableOfContents = PreviewData.sampleToc,
                onBack = {},
                onNavigateToReader = {},
                onNavigateToPlayer = {},
                fileSizeOverride = "2.4 MB",
                modifiedDateOverride = "Mar 10, 2024",
            )
        }
    }
}

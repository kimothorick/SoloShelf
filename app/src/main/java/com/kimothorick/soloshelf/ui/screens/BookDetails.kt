package com.kimothorick.soloshelf.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.ui.components.CircularIconButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookDetails(
    bookId: Long,
    onBack: () -> Unit,
    viewModel: BookDetailsViewModel = hiltViewModel(),
) {
    val book by viewModel.book.collectAsState()

    LaunchedEffect(bookId) {
        viewModel.setBookId(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    CircularIconButton(
                        modifier = Modifier.padding(start = 12.dp),
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = stringResource(R.string.back),
                        onClick = onBack,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        book?.let { currentBook ->
            BookDetailsContent(
                book = currentBook,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BookDetailsContent(
    book: Book,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Book Cover
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 300.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AsyncImage(
                model = book.coverPath ?: book.filePath,
                contentDescription = book.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.book_icon),
                error = painterResource(R.drawable.book_icon),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title and Author
        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "by ${book.author}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons using Row with weights to ensure visibility
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SplitButtonDefaults.LeadingButton(
                onClick = { /* Start reading */ },
                modifier = Modifier.height(56.dp).weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5D5D5B),
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(R.string.start_reading),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.size(SplitButtonDefaults.Spacing))

            SplitButtonDefaults.TrailingButton(
                onClick = { /* More options */ },
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5D5D5B),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.more),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Synopsis
        SectionHeader(stringResource(R.string.synopsis))
        Spacer(modifier = Modifier.height(12.dp))

        var isExpanded by remember { mutableStateOf(false) }
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
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Start,
                maxLines = if (isExpanded) Int.MAX_VALUE else 10,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            if (synopsis != stringResource(R.string.no_synopsis_available)) {
                Text(
                    text = if (isExpanded) "Show less" else "Show more",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { isExpanded = !isExpanded }
                        .align(Alignment.Start),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Details
        SectionHeader(stringResource(R.string.details))
        Spacer(modifier = Modifier.height(16.dp))
        DetailRow(stringResource(R.string.pages), book.totalPages.toString())
        DetailRow(
            stringResource(R.string.publication_date),
            formatDate(book.publicationDate) ?: stringResource(R.string.unknown),
        )
        DetailRow(
            stringResource(R.string.language),
            formatLanguage(book.language) ?: stringResource(R.string.unknown),
        )
        DetailRow(stringResource(R.string.file_type), book.format)

        if (book.genres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))

            // Genres
            SectionHeader(stringResource(R.string.genres))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                book.genres.forEach { genre ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(genre) },
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

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SectionHeader(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start,
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun formatLanguage(
    languageCode: String?,
): String? {
    if (languageCode.isNullOrBlank()) return null
    return try {
        Locale(languageCode)
            .getDisplayLanguage(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } catch (e: Exception) {
        languageCode
    }
}

private fun formatDate(
    dateString: String?,
): String? {
    if (dateString.isNullOrBlank() || dateString.contains("unknown", ignoreCase = true)) return null

    val inputFormatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE, // yyyy-MM-dd
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH),
    )

    val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())

    for (formatter in inputFormatters) {
        try {
            if (formatter == inputFormatters.last()) {
                // Special case for just the year
                val year = dateString.toIntOrNull() ?: continue
                return outputFormatter.format(LocalDate.of(year, 1, 1)).replace("January 1, ", "")
            }
            val date = LocalDate.parse(dateString, formatter)
            return date.format(outputFormatter)
        } catch (e: DateTimeParseException) {
            continue
        }
    }

    return dateString
}

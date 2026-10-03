package com.kimothorick.soloshelf.ui.screens.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.AudioProgress
import com.kimothorick.soloshelf.data.models.Track
import com.kimothorick.soloshelf.service.PlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

data class PlayerUiState(
    val bookId: Long? = null,
    val bookTitle: String = "",
    val author: String = "",
    val coverPath: String? = null,
    val currentChapterTitle: String = "",
    val currentTrackIndex: Int = 0,
    val tracks: List<Track> = emptyList(),
    val progress: Float = 0f,
    val currentTimeLabel: String = "0:00",
    val durationLabel: String = "0:00",
    val remainingTimeLabel: String = "0:00",
    val totalRemainingTimeLabel: String = "0:00",
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutes: Int? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var player: Player? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var tracks: List<Track> = emptyList()
    private var pendingBookId: Long? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                try {
                    val controller = future.get()
                    player = controller
                    setupPlayerListener(controller)
                    pendingBookId?.let { bookId ->
                        loadBookInternal(bookId)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PlayerViewModel", "Failed to connect MediaController", e)
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    private fun setupPlayerListener(
        player: Player,
    ) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(
                isPlaying: Boolean,
            ) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                    saveProgress()
                }
            }

            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int,
            ) {
                val index = player.currentMediaItemIndex
                if (index in tracks.indices) {
                    _uiState.update {
                        it.copy(
                            currentTrackIndex = index,
                            currentChapterTitle = tracks[index].title,
                        )
                    }
                    updateProgressState()
                    saveProgress()
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int,
            ) {
                updateProgressState()
                saveProgress()
            }

            override fun onPlaybackStateChanged(
                playbackState: Int,
            ) {
                updateProgressState()
            }

            override fun onPlayerError(
                error: androidx.media3.common.PlaybackException,
            ) {
                android.util.Log.e("PlayerViewModel", "Player Error: ${error.errorCodeName} - ${error.message}", error)
                _uiState.update { it.copy(isPlaying = false) }
            }
        })
    }

    fun loadBook(
        bookId: Long,
    ) {
        if (_uiState.value.bookId == bookId && player?.mediaItemCount != 0) {
            return
        }
        pendingBookId = bookId
        if (player != null) {
            loadBookInternal(bookId)
        }
    }

    private fun loadBookInternal(
        bookId: Long,
    ) {
        viewModelScope.launch {
            val details = bookRepository.getBookWithAudioDetails(bookId).first()
            if (details != null) {
                val loadedTracks = if (details.tracks.isNotEmpty()) {
                    details.tracks.sortedBy { it.trackNumber }
                } else {
                    listOf(
                        Track(
                            bookId = bookId,
                            filePath = details.book.filePath,
                            title = details.book.title,
                            duration = (details.audioDetails?.totalDurationMs?.div(1000))?.toInt() ?: 0,
                            trackNumber = 1,
                        ),
                    )
                }
                tracks = loadedTracks
                _uiState.update {
                    it.copy(
                        bookId = bookId,
                        bookTitle = details.book.title,
                        author = details.book.author,
                        coverPath = details.book.coverPath,
                        tracks = tracks,
                        isLoading = false,
                        currentChapterTitle = if (tracks.isNotEmpty()) tracks[0].title else "",
                    )
                }

                setupPlaylist(details.book, tracks, details.audioProgress)
            }
        }
    }

    private fun setupPlaylist(
        book: com.kimothorick.soloshelf.data.models.Book,
        tracks: List<Track>,
        audioProgress: AudioProgress?,
    ) {
        val activePlayer = player ?: return
        val mediaItems = tracks.map { track ->
            val rawPath = track.filePath
            val playableUri = if (isAbsoluteUri(rawPath)) rawPath else book.filePath
            val coverUri = book.coverPath?.let { path ->
                if (path.startsWith("content://") || path.startsWith("file://")) {
                    Uri.parse(path)
                } else {
                    Uri.fromFile(java.io.File(path))
                }
            }
            val clippingBuilder = MediaItem.ClippingConfiguration.Builder()
            if (track.startMs > 0) {
                clippingBuilder.setStartPositionMs(track.startMs)
            }
            if (track.endMs > track.startMs) {
                clippingBuilder.setEndPositionMs(track.endMs)
            }

            MediaItem
                .Builder()
                .setUri(playableUri)
                .setMediaId(track.trackId.toString())
                .setClippingConfiguration(clippingBuilder.build())
                .setMediaMetadata(
                    MediaMetadata
                        .Builder()
                        .setTitle(track.title)
                        .setArtist(book.author.ifBlank { "Unknown Author" })
                        .setAlbumTitle(book.title)
                        .setArtworkUri(coverUri)
                        .build(),
                ).build()
        }
        activePlayer.setMediaItems(mediaItems)
        activePlayer.prepare()

        if (audioProgress != null && audioProgress.currentTrackId != null) {
            val trackIndex = tracks.indexOfFirst { it.trackId == audioProgress.currentTrackId }.coerceAtLeast(0)
            activePlayer.seekTo(trackIndex, audioProgress.currentOffsetMs)
            _uiState.update {
                it.copy(
                    currentTrackIndex = trackIndex,
                    currentChapterTitle = if (trackIndex in tracks.indices) tracks[trackIndex].title else it.currentChapterTitle,
                )
            }
        }
        updateProgressState()
    }

    private fun isAbsoluteUri(
        path: String?,
    ): Boolean {
        if (path.isNullOrBlank()) return false
        return path.startsWith("content://") ||
            path.startsWith("file://") ||
            path.startsWith("http://") ||
            path.startsWith("https://")
    }

    fun togglePlayPause() {
        player?.let { p ->
            if (p.playbackState == Player.STATE_ENDED) {
                p.seekTo(0, 0L)
                p.prepare()
                p.play()
            } else if (p.playbackState == Player.STATE_IDLE) {
                p.prepare()
                p.play()
            } else if (p.isPlaying) {
                p.pause()
            } else {
                p.play()
            }
            updateProgressState()
            saveProgress()
        }
    }

    fun rewind10s() {
        player?.let { p ->
            p.seekTo(maxOf(0, p.currentPosition - 10000))
            updateProgressState()
            saveProgress()
        }
    }

    fun forward10s() {
        player?.let { p ->
            p.seekTo(p.currentPosition + 10000)
            updateProgressState()
            saveProgress()
        }
    }

    fun nextTrack() {
        player?.let { p ->
            p.seekToNextMediaItem()
            val nextIndex = p.currentMediaItemIndex
            if (nextIndex in tracks.indices) {
                _uiState.update {
                    it.copy(
                        currentTrackIndex = nextIndex,
                        currentChapterTitle = tracks[nextIndex].title,
                    )
                }
            }
            updateProgressState()
            saveProgress()
        }
    }

    fun previousTrack() {
        player?.let { p ->
            p.seekToPreviousMediaItem()
            val prevIndex = p.currentMediaItemIndex
            if (prevIndex in tracks.indices) {
                _uiState.update {
                    it.copy(
                        currentTrackIndex = prevIndex,
                        currentChapterTitle = tracks[prevIndex].title,
                    )
                }
            }
            updateProgressState()
            saveProgress()
        }
    }

    fun selectTrack(
        index: Int,
    ) {
        if (index in tracks.indices) {
            player?.seekTo(index, 0L)
            player?.play()
            _uiState.update {
                it.copy(
                    currentTrackIndex = index,
                    currentChapterTitle = tracks[index].title,
                )
            }
            updateProgressState()
            saveProgress()
        }
    }

    fun seekTo(
        progress: Float,
    ) {
        player?.let { p ->
            val position = (progress * p.duration).toLong()
            p.seekTo(position)
            updateProgressState()
            saveProgress()
        }
    }

    fun setPlaybackSpeed(
        speed: Float,
    ) {
        player?.playbackParameters = PlaybackParameters(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        val currentSpeed = _uiState.value.playbackSpeed
        val currentIndex = speeds.indexOf(currentSpeed)
        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % speeds.size else 0
        setPlaybackSpeed(speeds[nextIndex])
    }

    fun setSleepTimer(
        minutes: Int?,
    ) {
        sleepTimerJob?.cancel()
        _uiState.update { it.copy(sleepTimerMinutes = minutes) }

        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes.toLong() * 60 * 1000)
                player?.pause()
                _uiState.update { it.copy(sleepTimerMinutes = null) }
            }
        }
    }

    fun addBookmark() {
        // TODO: Implement bookmarks
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                updateProgressState()
                delay(1.seconds)
            }
        }
    }

    private fun updateProgressState() {
        player?.let { p ->
            if (p.duration > 0) {
                val progress = (p.currentPosition.toFloat() / p.duration).coerceIn(0f, 1f)
                val chapterRemainingMs = maxOf(0L, p.duration - p.currentPosition)

                val currentTrackIndex = p.currentMediaItemIndex
                val currentPos = p.currentPosition
                var totalRemainingMs = 0L
                for (i in tracks.indices) {
                    val track = tracks[i]
                    val trackDurationMs = if (track.duration > 0) track.duration * 1000L else p.duration
                    if (i > currentTrackIndex) {
                        totalRemainingMs += trackDurationMs
                    } else if (i == currentTrackIndex) {
                        totalRemainingMs += maxOf(0L, trackDurationMs - currentPos)
                    }
                }
                if (totalRemainingMs <= 0L) totalRemainingMs = chapterRemainingMs

                _uiState.update {
                    it.copy(
                        progress = progress,
                        currentTimeLabel = formatTime(p.currentPosition),
                        durationLabel = formatTime(p.duration),
                        remainingTimeLabel = formatTime(chapterRemainingMs),
                        totalRemainingTimeLabel = formatTotalRemainingTime(totalRemainingMs),
                    )
                }
                saveProgress()
            }
        }
    }

    private fun saveProgress() {
        val activePlayer = player ?: return
        val currentBookId = _uiState.value.bookId ?: return
        val currentIndex = activePlayer.currentMediaItemIndex
        if (currentIndex in tracks.indices) {
            val track = tracks[currentIndex]
            val currentPos = activePlayer.currentPosition
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    bookRepository.insertAudioProgress(
                        AudioProgress(
                            bookId = currentBookId,
                            currentTrackId = track.trackId,
                            currentOffsetMs = currentPos,
                            lastAccessed = System.currentTimeMillis(),
                        ),
                    )
                } catch (e: Exception) {
                    android.util.Log.e("PlayerViewModel", "Failed to save audiobook progress", e)
                }
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    private fun formatTime(
        ms: Long,
    ): String {
        val totalSeconds = maxOf(0L, ms / 1000)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }

    private fun formatRemainingTime(
        ms: Long,
    ): String {
        val totalSeconds = maxOf(0L, ms / 1000)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "-%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "-%d:%02d", minutes, seconds)
        }
    }

    private fun formatTotalRemainingTime(
        ms: Long,
    ): String {
        val totalSeconds = maxOf(0L, ms / 1000)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%dh %02dm %02ds left", hours, minutes, seconds)
        } else if (minutes > 0) {
            String.format(Locale.getDefault(), "%dm %02ds left", minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%ds left", seconds)
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveProgress()
        sleepTimerJob?.cancel()
        progressJob?.cancel()
        controllerFuture?.let { future ->
            MediaController.releaseFuture(future)
        }
        player = null
    }
}

package com.kimothorick.soloshelf.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: BookRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val allBooks: Flow<PagingData<BookAndProgress>> = userPreferencesRepository.sortOrder
        .flatMapLatest { sortOrder ->
            repository.getBooksWithProgress(sortOrder)
        }
        .cachedIn(viewModelScope)

    val recentBooks: Flow<PagingData<BookAndProgress>> = repository.getRecentBooksWithProgress()
        .cachedIn(viewModelScope)
}

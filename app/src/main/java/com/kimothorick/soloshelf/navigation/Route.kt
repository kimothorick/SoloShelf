package com.kimothorick.soloshelf.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey, Parcelable {
    @Serializable
    @Parcelize
    data object Library : Route, NavKey

    @Serializable
    @Parcelize
    data object Bookshelf : Route, NavKey

    @Serializable
    @Parcelize
    data object Audiobooks : Route, NavKey

    @Serializable
    @Parcelize
    data object Settings : Route, NavKey

    @Serializable
    @Parcelize
    data class BookDetails(val bookId: Long) : Route, NavKey
}

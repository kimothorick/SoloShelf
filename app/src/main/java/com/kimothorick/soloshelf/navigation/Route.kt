package com.kimothorick.soloshelf.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Library : Route, NavKey

    @Serializable
    data object Bookshelf : Route, NavKey

    @Serializable
    data object Audiobooks : Route, NavKey

    @Serializable
    data object Settings : Route, NavKey
}

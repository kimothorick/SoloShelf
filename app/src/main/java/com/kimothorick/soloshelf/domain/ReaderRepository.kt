package com.kimothorick.soloshelf.domain

import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.shared.publication.Publication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReaderRepository @Inject constructor() {
    var currentPublication: Publication? = null
    var currentNavigator: VisualNavigator? = null

    fun clear() {
        currentPublication = null
        currentNavigator = null
    }
}

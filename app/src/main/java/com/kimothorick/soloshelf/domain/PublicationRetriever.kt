/*
 * Copyright 2023 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package com.kimothorick.soloshelf.domain

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Error
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.streamer.PublicationOpener
import java.util.concurrent.ConcurrentHashMap

/**
 * A domain-level wrapper to handle opening Readium [Publication]s.
 * Inspired by the Readium Kotlin Toolkit test-app.
 */
class PublicationRetriever(
    private val context: Context,
    private val publicationOpener: PublicationOpener,
) {
    private val cachedPublications = ConcurrentHashMap<String, Publication>()

    suspend fun retrieve(
        asset: Asset,
    ): Try<Publication, Error> =
        withContext(Dispatchers.IO) {
            val cacheKey = asset.toString()
            cachedPublications[cacheKey]?.let { return@withContext Try.success(it) }

            publicationOpener
                .open(asset, allowUserInteraction = false)
                .onSuccess { publication ->
                    cachedPublications[cacheKey] = publication
                }
        }

    fun close(
        asset: Asset,
    ) {
        val cacheKey = asset.toString()
        cachedPublications.remove(cacheKey)?.close()
    }
}

package com.kimothorick.soloshelf.domain

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
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
    private val publicationOpener: PublicationOpener
) {
    private val cachedPublications = ConcurrentHashMap<String, Publication>()

    suspend fun retrieve(asset: Asset): Try<Publication, Exception> = withContext(Dispatchers.IO) {
        val cacheKey = asset.toString()
        cachedPublications[cacheKey]?.let { return@withContext Try.success(it) }

        publicationOpener.open(asset, allowUserInteraction = false)
            .map { publication ->
                cachedPublications[cacheKey] = publication
                publication
            }
            .mapFailure { Exception(it.toString()) }
    }

    fun close(asset: Asset) {
        val cacheKey = asset.toString()
        cachedPublications.remove(cacheKey)?.close()
    }
}

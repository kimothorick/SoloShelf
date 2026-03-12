package com.kimothorick.soloshelf.di

import android.content.Context
import androidx.room.Room
import coil3.ImageLoader
import coil3.util.DebugLogger
import com.kimothorick.soloshelf.data.BookDatabase
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.daos.BookDao
import com.kimothorick.soloshelf.domain.PublicationRetriever
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.http.HttpClient
import org.readium.r2.shared.util.pdf.PdfDocumentFactory
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideBookDatabase(
        @ApplicationContext context: Context,
    ): BookDatabase = BookDatabase.getDatabase(context)

    @Provides
    fun provideBookDao(
        database: BookDatabase,
    ): BookDao = database.bookDao()

    @Provides
    @Singleton
    fun provideBookRepository(
        bookDao: BookDao,
    ): BookRepository = BookRepository(bookDao)

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .logger(DebugLogger())
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = DefaultHttpClient()

    @Provides
    @Singleton
    fun provideAssetRetriever(
        @ApplicationContext context: Context,
        httpClient: HttpClient,
    ): AssetRetriever = AssetRetriever(context.contentResolver, httpClient)

    @Provides
    @Singleton
    fun providePdfDocumentFactory(
        @ApplicationContext context: Context,
    ): PdfDocumentFactory<*> = PdfiumDocumentFactory(context)

    @Provides
    @Singleton
    fun providePublicationOpener(
        @ApplicationContext context: Context,
        assetRetriever: AssetRetriever,
        httpClient: HttpClient,
        pdfDocumentFactory: PdfDocumentFactory<*>,
    ): PublicationOpener {
        val parser = DefaultPublicationParser(
            context,
            httpClient = httpClient,
            assetRetriever = assetRetriever,
            pdfFactory = pdfDocumentFactory
        )
        return PublicationOpener(parser)
    }

    @Provides
    @Singleton
    fun providePublicationRetriever(
        @ApplicationContext context: Context,
        publicationOpener: PublicationOpener,
    ): PublicationRetriever = PublicationRetriever(
        context,
        publicationOpener = publicationOpener,
    )
}

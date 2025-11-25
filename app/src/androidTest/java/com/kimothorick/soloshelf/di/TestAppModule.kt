package com.kimothorick.soloshelf.di

import android.content.Context
import androidx.room.Room
import com.kimothorick.soloshelf.data.BookDatabase
import com.kimothorick.soloshelf.data.BookRepository
import com.kimothorick.soloshelf.data.daos.BookDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideInMemoryBookDatabase(@ApplicationContext context: Context): BookDatabase {
        return Room.inMemoryDatabaseBuilder(
            context, BookDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Provides
    fun provideBookDao(database: BookDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    @Singleton
    fun provideBookRepository(bookDao: BookDao): BookRepository {
        return BookRepository(bookDao)
    }
}

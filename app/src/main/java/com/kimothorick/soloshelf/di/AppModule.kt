package com.kimothorick.soloshelf.di

import android.content.Context
import androidx.room.Room
import com.kimothorick.soloshelf.data.BookDatabase
import com.kimothorick.soloshelf.data.daos.BookDao
import com.kimothorick.soloshelf.data.BookRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBookDatabase(@ApplicationContext context: Context): BookDatabase {
        return Room.databaseBuilder(
            context,
            BookDatabase::class.java,
            "soloshelf_database"
        ).build()
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

package com.example.gamebooster.di

import android.content.Context
import androidx.room.Room
import com.example.gamebooster.data.db.AppDatabase
import com.example.gamebooster.data.db.dao.GameDetailsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideGameDetailsDao(database: AppDatabase): GameDetailsDao {
        return database.gameDetailsDao()
    }
}

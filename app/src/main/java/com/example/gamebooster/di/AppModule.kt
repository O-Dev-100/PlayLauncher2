package com.example.gamebooster.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.Room
import com.example.gamebooster.data.AppDatabase
import com.example.gamebooster.data.BoostHistoryDao
import com.example.gamebooster.data.InstalledAppsRepository
import com.example.gamebooster.data.remote.RawgApiService
import com.example.gamebooster.data.remote.GameDetailsRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("gamebooster_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gamebooster_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBoostHistoryDao(db: AppDatabase): BoostHistoryDao {
        return db.boostHistoryDao()
    }

    @Provides
    @Singleton
    fun provideInstalledAppsRepository(@ApplicationContext context: Context): InstalledAppsRepository {
        return InstalledAppsRepository(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BASIC)
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.rawg.io/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRawgApi(retrofit: Retrofit): RawgApiService {
        return retrofit.create(RawgApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGameDetailsRepository(api: RawgApiService): GameDetailsRepository {
        val repo = GameDetailsRepository(api)
        GameDetailsRepository.init(repo)
        return repo
    }
}
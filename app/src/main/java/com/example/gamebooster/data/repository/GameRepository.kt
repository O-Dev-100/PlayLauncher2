package com.example.gamebooster.data.repository

import com.example.gamebooster.data.api.GameApiService
import com.example.gamebooster.data.db.dao.GameDetailsDao
import com.example.gamebooster.data.model.GameDetails
import com.example.gamebooster.data.model.GameSearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameApiService: GameApiService,
    private val gameDetailsDao: GameDetailsDao
) {
    companion object {
        private const val CACHE_DURATION = TimeUnit.DAYS.toMillis(7) // Cache for 7 days
    }

    suspend fun getGameDetails(packageName: String, forceRefresh: Boolean = false): Flow<Result<GameDetails>> = flow {
        try {
            // Check cache first if not forcing refresh
            if (!forceRefresh) {
                val cachedDetails = gameDetailsDao.getGameDetails(packageName)
                if (cachedDetails != null && 
                    System.currentTimeMillis() - cachedDetails.lastUpdated < CACHE_DURATION) {
                    emit(Result.success(cachedDetails))
                    return@flow
                }
            }
            
            // Fetch from API if no cache or cache is stale
            val response = gameApiService.getGameDetails(packageName)
            if (response.isSuccessful) {
                val gameDetails = response.body()
                if (gameDetails != null) {
                    // Save to cache
                    gameDetailsDao.insertGameDetails(gameDetails)
                    emit(Result.success(gameDetails))
                } else {
                    emit(Result.failure(Exception("No data received")))
                }
            } else {
                emit(Result.failure(Exception("API error: ${response.code()} - ${response.message()}")))
            }
        } catch (e: Exception) {
            // If there's an error, try to return cached data if available
            val cachedDetails = gameDetailsDao.getGameDetails(packageName)
            if (cachedDetails != null) {
                emit(Result.success(cachedDetails))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    suspend fun searchGames(query: String): Flow<Result<List<GameSearchResult>>> = flow {
        try {
            val response = gameApiService.searchGames(query)
            if (response.isSuccessful) {
                val searchResults = response.body()?.results ?: emptyList()
                emit(Result.success(searchResults))
            } else {
                emit(Result.failure(Exception("Search failed: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

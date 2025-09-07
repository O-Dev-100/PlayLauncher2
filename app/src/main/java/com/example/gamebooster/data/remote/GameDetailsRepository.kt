package com.example.gamebooster.data.remote

import com.example.gamebooster.BuildConfig
import com.example.gamebooster.data.remote.model.RawgGameDetails
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDetailsRepository @Inject constructor(
    private val rawgApiService: RawgApiService
) {
    suspend fun fetchGameDetails(gameName: String): Result<RawgGameDetails> {
        return try {
            val searchResp = rawgApiService.searchGames(BuildConfig.RAWG_API_KEY, gameName)
            if (!searchResp.isSuccessful || searchResp.body() == null || searchResp.body()!!.results.isEmpty()) {
                return Result.failure(IllegalStateException("Game not found"))
            }
            val id = searchResp.body()!!.results.first().id
            val detailsResp = rawgApiService.getGameDetails(id, BuildConfig.RAWG_API_KEY)
            if (!detailsResp.isSuccessful || detailsResp.body() == null) {
                return Result.failure(IllegalStateException("Details not available"))
            }
            Result.success(detailsResp.body()!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        // Simple Service Locator fallback for ViewModel direct access without DI where needed
        @Volatile private var instance: GameDetailsRepository? = null
        fun init(repo: GameDetailsRepository) { instance = repo }
        fun get(): GameDetailsRepository = instance ?: throw IllegalStateException("GameDetailsRepository not initialized")
    }
}



package com.example.gamebooster.data.api

import com.example.gamebooster.data.model.GameDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GameApiService {
    @GET("games/details/{packageName}")
    suspend fun getGameDetails(
        @Path("packageName") packageName: String,
        @Query("lang") language: String = "en",
        @Query("country") country: String = "us"
    ): Response<GameDetails>

    @GET("games/search")
    suspend fun searchGames(
        @Query("query") query: String,
        @Query("num") count: Int = 20,
        @Query("lang") language: String = "en",
        @Query("country") country: String = "us"
    ): Response<GameSearchResponse>
}

data class GameSearchResponse(
    val count: Int,
    val results: List<GameSearchResult>
)

data class GameSearchResult(
    val id: String,
    val title: String,
    val summary: String,
    val installs: String,
    val score: Double,
    val scoreText: String,
    val price: Double,
    val free: Boolean,
    val currency: String,
    val priceText: String,
    val size: String,
    val androidVersion: String,
    val genre: String,
    val genreId: String,
    val icon: String,
    val headerImage: String?,
    val released: String?,
    val updated: Long,
    val version: String?,
    val url: String
)

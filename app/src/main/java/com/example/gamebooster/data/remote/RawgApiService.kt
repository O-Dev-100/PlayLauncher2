package com.example.gamebooster.data.remote

import com.example.gamebooster.data.remote.model.GameSearchResponse
import com.example.gamebooster.data.remote.model.RawgGameDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApiService {
    @GET("games")
    suspend fun searchGames(
        @Query("key") apiKey: String,
        @Query("search") query: String
    ): Response<GameSearchResponse>

    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String
    ): Response<RawgGameDetails>
}



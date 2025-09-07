package com.example.gamebooster.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gamebooster.data.model.GameDetails

@Dao
interface GameDetailsDao {
    @Query("SELECT * FROM game_details WHERE packageName = :packageName")
    suspend fun getGameDetails(packageName: String): GameDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameDetails(gameDetails: GameDetails)

    @Query("DELETE FROM game_details WHERE packageName = :packageName")
    suspend fun deleteGameDetails(packageName: String)

    @Query("SELECT * FROM game_details WHERE lastUpdated < :timestamp")
    suspend fun getStaleGameDetails(timestamp: Long): List<GameDetails>

    @Query("DELETE FROM game_details WHERE lastUpdated < :timestamp")
    suspend fun deleteStaleGameDetails(timestamp: Long)
}

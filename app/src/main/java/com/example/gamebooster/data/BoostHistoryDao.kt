package com.example.gamebooster.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gamebooster.model.BoostHistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface BoostHistoryDao {
    @Insert
    suspend fun insertBoost(entry: BoostHistoryEntry)

    @Query("SELECT * FROM boost_history ORDER BY timestamp DESC")
    fun getAllBoosts(): Flow<List<BoostHistoryEntry>>

    @Query("DELETE FROM boost_history")
    suspend fun clearHistory()
}
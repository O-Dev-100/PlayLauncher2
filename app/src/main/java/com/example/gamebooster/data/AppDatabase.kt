package com.example.gamebooster.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gamebooster.model.BoostHistoryEntry

@Database(entities = [BoostHistoryEntry::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boostHistoryDao(): BoostHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE boost_history ADD COLUMN optimizationScore INTEGER DEFAULT 0 NOT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // ← Esto permite borrar y recrear si falla la migración
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

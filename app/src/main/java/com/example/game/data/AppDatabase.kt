package com.example.game.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.game.data.dao.GameDao
import com.example.game.data.entity.AchievementEntity
import com.example.game.data.entity.LevelProgressEntity
import com.example.game.data.entity.PlayerProfileEntity

@Database(
    entities = [LevelProgressEntity::class, PlayerProfileEntity::class, AchievementEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blocky_buddies_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

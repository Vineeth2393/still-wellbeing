package com.stillwellbeing.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailySummaryEntity::class], version = 1, exportSchema = false)
abstract class StillDatabase : RoomDatabase() {
    abstract fun dailySummaryDao(): DailySummaryDao

    companion object {
        @Volatile private var instance: StillDatabase? = null

        fun getInstance(context: Context): StillDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    StillDatabase::class.java,
                    "still.db"
                ).build().also { instance = it }
            }
    }
}

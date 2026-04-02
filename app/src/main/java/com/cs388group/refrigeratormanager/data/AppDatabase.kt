package com.cs388group.refrigeratormanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cs388group.refrigeratormanager.data.FoodItem

@Database(entities = [FoodItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "refrigerator_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.example.studentmanagement.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.studentmanagement.utils.Constants

/**
 * Room database singleton.
 *
 * To access the DAO, use:
 *   AppDatabase.getInstance(context).studentDao()
 *
 * Database version must be incremented and a migration provided whenever
 * the schema (entities) changes.
 */
@Database(
    entities = [StudentEntity::class],
    version = Constants.DB_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    companion object {
        // Volatile ensures all threads see the same instance immediately.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DB_NAME
                )
                    // fallbackToDestructiveMigration is acceptable during development.
                    // Before production release, replace with proper Migration objects.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

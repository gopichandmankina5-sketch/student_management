package com.example.studentmanagement.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.studentmanagement.utils.Constants

/**
 * Room database singleton.
 *
 * To access DAOs, use:
 *   AppDatabase.getInstance(context).studentDao()
 *   AppDatabase.getInstance(context).assignmentDao()
 *   AppDatabase.getInstance(context).notificationDao()
 *
 * Database version must be incremented and a migration provided whenever
 * the schema (entities) changes.
 *
 * Version history:
 *   v1 — Member 1: students table
 *   v2 — Member 3: assignments + notifications tables
 */
@Database(
    entities = [
        StudentEntity::class,
        AssignmentEntity::class,
        NotificationEntity::class
    ],
    version = Constants.DB_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun notificationDao(): NotificationDao

    companion object {
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

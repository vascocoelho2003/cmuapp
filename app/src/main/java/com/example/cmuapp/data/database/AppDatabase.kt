package com.example.cmuapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cmuapp.data.dao.EstablishmentDao
import com.example.cmuapp.data.dao.ReviewDao
import com.example.cmuapp.data.dao.UserDao
import com.example.cmuapp.data.dao.UserVisitedDao
import com.example.cmuapp.data.entities.Establishment
import com.example.cmuapp.data.entities.Review
import com.example.cmuapp.data.entities.UserEntity
import com.example.cmuapp.data.entities.UserVisitedEntity

@Database(
    entities = [UserEntity::class, Establishment::class, Review::class, UserVisitedEntity::class],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun establishmentDao(): EstablishmentDao
    abstract fun reviewDao(): ReviewDao
    abstract fun userVisitedDao(): UserVisitedDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

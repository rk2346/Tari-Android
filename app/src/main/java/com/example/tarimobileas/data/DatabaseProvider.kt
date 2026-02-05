package com.example.tarimobileas.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase =
        db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tari_db"
            ).fallbackToDestructiveMigration()
                .build()
            db = instance
            instance
        }
}

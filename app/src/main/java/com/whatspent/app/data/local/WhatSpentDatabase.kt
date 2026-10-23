package com.whatspent.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.whatspent.app.data.local.dao.ChatMetadataDao
import com.whatspent.app.data.local.dao.ChatSessionDao
import com.whatspent.app.data.local.entity.ChatMetadataEntity
import com.whatspent.app.data.local.entity.ChatSessionEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WhatSpentDatabase : RoomDatabase() {

    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMetadataDao(): ChatMetadataDao

    companion object {
        private const val DATABASE_NAME = "whatspent.db"

        @Volatile
        private var INSTANCE: WhatSpentDatabase? = null

        fun getInstance(context: Context): WhatSpentDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WhatSpentDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

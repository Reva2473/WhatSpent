package com.chatpulse.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chatpulse.app.data.local.dao.ChatMetadataDao
import com.chatpulse.app.data.local.dao.ChatSessionDao
import com.chatpulse.app.data.local.entity.ChatMetadataEntity
import com.chatpulse.app.data.local.entity.ChatSessionEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChatPulseDatabase : RoomDatabase() {

    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMetadataDao(): ChatMetadataDao

    companion object {
        private const val DATABASE_NAME = "chatpulse.db"

        @Volatile
        private var INSTANCE: ChatPulseDatabase? = null

        fun getInstance(context: Context): ChatPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChatPulseDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

package com.whatspent.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_sessions",
    indices = [
        Index(value = ["chatName"]),
        Index(value = ["date"]),
        Index(value = ["chatName", "date"])
    ]
)
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val chatName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val date: String // Format: yyyy-MM-dd
)

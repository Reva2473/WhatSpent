package com.whatspent.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_metadata")
data class ChatMetadataEntity(
    @PrimaryKey
    val chatName: String,
    val isExcluded: Boolean = false,
    val dailyLimitMinutes: Int? = null,
    val customAlias: String? = null,
    val colorTag: Int? = null
)

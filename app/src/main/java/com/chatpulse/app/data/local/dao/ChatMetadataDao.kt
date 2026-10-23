package com.chatpulse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chatpulse.app.data.local.entity.ChatMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMetadataDao {

    @Query("SELECT * FROM chat_metadata")
    fun observeAllMetadata(): Flow<List<ChatMetadataEntity>>

    @Query("SELECT * FROM chat_metadata")
    suspend fun getAllMetadataSync(): List<ChatMetadataEntity>

    @Query("SELECT * FROM chat_metadata WHERE chatName = :chatName LIMIT 1")
    fun observeMetadataForChat(chatName: String): Flow<ChatMetadataEntity?>

    @Query("SELECT * FROM chat_metadata WHERE chatName = :chatName LIMIT 1")
    suspend fun getMetadataForChatSync(chatName: String): ChatMetadataEntity?

    @Query("SELECT chatName FROM chat_metadata WHERE isExcluded = 1")
    fun observeExcludedChats(): Flow<List<String>>

    @Query("SELECT chatName FROM chat_metadata WHERE isExcluded = 1")
    suspend fun getExcludedChatsSync(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetadata(metadata: ChatMetadataEntity)

    @Query("DELETE FROM chat_metadata WHERE chatName = :chatName")
    suspend fun deleteMetadataForChat(chatName: String)

    @Query("DELETE FROM chat_metadata")
    suspend fun deleteAllMetadata()
}

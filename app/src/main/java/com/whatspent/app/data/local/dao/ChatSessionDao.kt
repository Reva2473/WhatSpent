package com.whatspent.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.whatspent.app.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

data class ChatAggregationTuple(
    val chatName: String,
    val totalDurationMs: Long,
    val sessionCount: Int
)

data class DateDurationTuple(
    val date: String,
    val totalDurationMs: Long
)

@Dao
interface ChatSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("SELECT * FROM chat_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): ChatSessionEntity?

    @Query("SELECT * FROM chat_sessions WHERE date = :date ORDER BY startTime DESC")
    fun observeSessionsByDate(date: String): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE date = :date ORDER BY startTime ASC")
    fun observeSessionsChronological(date: String): Flow<List<ChatSessionEntity>>

    @Query("SELECT COALESCE(SUM(durationMs), 0) FROM chat_sessions WHERE date = :date")
    fun observeTotalDurationForDate(date: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(durationMs), 0) FROM chat_sessions WHERE date = :date")
    suspend fun getTotalDurationForDateSync(date: String): Long

    @Query("SELECT COALESCE(SUM(durationMs), 0) FROM chat_sessions WHERE chatName = :chatName AND date = :date")
    suspend fun getChatDurationForDateSync(chatName: String, date: String): Long

    @Query("SELECT COALESCE(SUM(durationMs), 0) FROM chat_sessions WHERE chatName = :chatName AND date = :date")
    fun observeChatDurationForDate(chatName: String, date: String): Flow<Long>

    @Query("SELECT chatName, SUM(durationMs) as totalDurationMs, COUNT(id) as sessionCount FROM chat_sessions WHERE date = :date GROUP BY chatName ORDER BY totalDurationMs DESC")
    fun observeChatAggregationsForDate(date: String): Flow<List<ChatAggregationTuple>>

    @Query("SELECT chatName, SUM(durationMs) as totalDurationMs, COUNT(id) as sessionCount FROM chat_sessions WHERE date = :date GROUP BY chatName ORDER BY totalDurationMs DESC")
    suspend fun getChatAggregationsForDateSync(date: String): List<ChatAggregationTuple>

    @Query("SELECT date, SUM(durationMs) as totalDurationMs FROM chat_sessions WHERE date >= :startDate AND date <= :endDate GROUP BY date ORDER BY date ASC")
    fun observeDailyTotalsInRange(startDate: String, endDate: String): Flow<List<DateDurationTuple>>

    @Query("SELECT date, SUM(durationMs) as totalDurationMs FROM chat_sessions WHERE chatName = :chatName AND date >= :startDate AND date <= :endDate GROUP BY date ORDER BY date ASC")
    fun observeChatDailyTotalsInRange(chatName: String, startDate: String, endDate: String): Flow<List<DateDurationTuple>>

    @Query("SELECT * FROM chat_sessions WHERE chatName = :chatName AND date >= :startDate ORDER BY startTime DESC")
    fun observeSessionsForChatSince(chatName: String, startDate: String): Flow<List<ChatSessionEntity>>

    @Query("SELECT DISTINCT date FROM chat_sessions ORDER BY date DESC LIMIT 30")
    fun observeAvailableDates(): Flow<List<String>>

    @Query("DELETE FROM chat_sessions WHERE chatName = :chatName")
    suspend fun deleteSessionsForChat(chatName: String)

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()
}

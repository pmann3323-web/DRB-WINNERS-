package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.db.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("UPDATE chat_messages SET isRead = 1 WHERE userId = :userId AND isAdmin = 1")
    suspend fun markUserMessagesAsRead(userId: String)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE userId = :userId AND isAdmin = 0")
    suspend fun markAdminMessagesAsRead(userId: String)
}

package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String, // The player's user ID associated with this chat
    val senderId: String, // "admin" or user's ID
    val senderName: String,
    val receiverId: String, // "admin" or user's ID
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false,
    val isRead: Boolean = false
)

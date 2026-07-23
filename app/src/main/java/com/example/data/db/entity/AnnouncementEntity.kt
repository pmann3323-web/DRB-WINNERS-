package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

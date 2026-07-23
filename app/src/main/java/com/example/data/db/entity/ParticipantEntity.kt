package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participants")
data class ParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val userId: String,
    val userName: String,
    val teamName: String,
    val inGameId: String = "",
    val entryFeePaid: Double = 0.0,
    val joinedAt: Long = System.currentTimeMillis(),
    val status: String = "JOINED" // "JOINED", "CANCELLED"
)

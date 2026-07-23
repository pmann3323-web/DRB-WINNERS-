package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val roundName: String, // e.g. "Quarter Final", "Semi Final", "Final", "Round 1"
    val matchNumber: Int,
    val team1Name: String,
    val team2Name: String,
    val team1Score: Int = 0,
    val team2Score: Int = 0,
    val winnerName: String = "",
    val status: String = "SCHEDULED", // SCHEDULED, LIVE, COMPLETED
    val startTime: String,
    val venueOrMap: String = "Main Arena"
)

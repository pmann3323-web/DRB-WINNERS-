package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val name: String,
    val captainName: String,
    val contactEmail: String = "",
    val membersCount: Int = 1,
    val seed: Int = 0,
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val points: Int = 0,
    val netRunRateOrDiff: Float = 0f
)

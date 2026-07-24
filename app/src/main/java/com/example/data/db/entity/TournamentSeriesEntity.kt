package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournament_series")
data class TournamentSeriesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val gameType: String = "Free Fire", // Free Fire, BGMI / PUBG
    val matchMode: String = "Squad", // Solo, Duo, Squad
    val totalQualifiersCount: Int = 6, // e.g., 6 or 7 qualifier matches
    val topQualifyPerRoom: Int = 2, // Top N squads qualify per room
    val entryFeePerSquad: Double = 100.0,
    val totalPrizePool: String = "₹20,000",
    val firstPrize: Double = 10000.0,
    val secondPrize: Double = 5000.0,
    val thirdPrize: Double = 2500.0,
    val perKillPrize: Double = 50.0,
    val status: String = "QUALIFIERS_IN_PROGRESS", // "QUALIFIERS_IN_PROGRESS", "FINAL_READY", "COMPLETED"
    val finalTournamentId: Long = 0L,
    val winnerTeamName: String = "",
    val winnerCaptain: String = "",
    val winnerKills: Int = 0,
    val winnerPoints: Int = 0,
    val secondTeamName: String = "",
    val thirdTeamName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

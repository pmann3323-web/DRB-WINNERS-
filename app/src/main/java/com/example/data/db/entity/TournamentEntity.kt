package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val gameType: String, // Cricket, Football, BGMI/Esports, Chess, Badminton, Valorant, Basketball
    val format: String, // Single Elimination, Double Elimination, Round Robin, League
    val status: String, // UPCOMING, LIVE, COMPLETED
    val startDate: String,
    val endDate: String = "",
    val totalTeams: Int,
    val currentTeamsCount: Int = 0,
    val entryFee: Double = 50.0,
    val prizePool: String,
    val bannerResId: Int = 0,
    val bannerUrl: String = "",
    val description: String,
    val rules: String = "1. Fair play is mandatory.\n2. All players must check-in 15 minutes prior to match time.\n3. Referee decisions are final.",
    val registrationOpen: Boolean = true,
    val maxPlayers: Int = 100,
    val roomId: String = "BGMI-8899",
    val roomPassword: String = "PASS777",
    val matchStartTimeMillis: Long = System.currentTimeMillis() + 600000L,
    val matchMode: String = "Squad", // Solo, Duo, Squad
    val firstPrize: Double = 300.0,
    val secondPrize: Double = 150.0,
    val thirdPrize: Double = 50.0,
    val perKillPrize: Double = 10.0,
    val seriesId: Long = 0L,
    val stage: String = "STANDALONE", // "QUALIFIER", "FINAL", "STANDALONE"
    val qualifierNumber: Int = 1,
    val topQualifyCount: Int = 2
)

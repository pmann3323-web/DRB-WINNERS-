package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qualified_squads")
data class QualifiedSquadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,
    val qualifierTournamentId: Long,
    val qualifierRoomName: String = "",
    val squadName: String,
    val captainName: String,
    val userId: String = "",
    val inGameId: String = "",
    val qualifierRank: Int = 1,
    val killsCount: Int = 0,
    val points: Int = 0,
    val isConfirmedForFinal: Boolean = true,
    val qualifiedAt: Long = System.currentTimeMillis()
)

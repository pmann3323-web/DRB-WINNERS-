package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.db.entity.ParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants WHERE tournamentId = :tournamentId AND status = 'JOINED'")
    fun getParticipantsForTournament(tournamentId: Long): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants WHERE userId = :userId AND status = 'JOINED'")
    fun getJoinedTournamentsForUser(userId: String): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants WHERE tournamentId = :tournamentId AND userId = :userId AND status = 'JOINED' LIMIT 1")
    suspend fun getParticipant(tournamentId: Long, userId: String): ParticipantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ParticipantEntity): Long

    @Query("UPDATE participants SET status = 'CANCELLED' WHERE tournamentId = :tournamentId AND userId = :userId")
    suspend fun cancelParticipant(tournamentId: Long, userId: String)
}

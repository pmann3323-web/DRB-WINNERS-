package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams WHERE tournamentId = :tournamentId ORDER BY points DESC, wins DESC, netRunRateOrDiff DESC")
    fun getTeamsForTournament(tournamentId: Long): Flow<List<TeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity): Long

    @Update
    suspend fun updateTeam(team: TeamEntity)

    @Delete
    suspend fun deleteTeam(team: TeamEntity)

    @Query("SELECT COUNT(*) FROM teams WHERE tournamentId = :tournamentId")
    suspend fun getTeamsCountForTournament(tournamentId: Long): Int
}

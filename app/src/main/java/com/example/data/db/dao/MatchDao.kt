package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY matchNumber ASC")
    fun getMatchesForTournament(tournamentId: Long): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Delete
    suspend fun deleteMatch(match: MatchEntity)
}

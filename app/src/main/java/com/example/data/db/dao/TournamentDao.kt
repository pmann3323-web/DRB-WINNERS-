package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.TournamentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY id DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    fun getTournamentById(id: Long): Flow<TournamentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity): Long

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Delete
    suspend fun deleteTournament(tournament: TournamentEntity)

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteTournamentById(id: Long)

    @Query("UPDATE tournaments SET roomId = :roomId, roomPassword = :roomPassword WHERE id = :tournamentId")
    suspend fun updateRoomCredentials(tournamentId: Long, roomId: String, roomPassword: String)
}

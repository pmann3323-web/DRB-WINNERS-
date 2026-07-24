package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.QualifiedSquadEntity
import com.example.data.db.entity.TournamentSeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentSeriesDao {
    @Query("SELECT * FROM tournament_series ORDER BY id DESC")
    fun getAllSeries(): Flow<List<TournamentSeriesEntity>>

    @Query("SELECT * FROM tournament_series WHERE id = :seriesId")
    fun getSeriesById(seriesId: Long): Flow<TournamentSeriesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: TournamentSeriesEntity): Long

    @Update
    suspend fun updateSeries(series: TournamentSeriesEntity)

    @Delete
    suspend fun deleteSeries(series: TournamentSeriesEntity)

    // Qualified Squads
    @Query("SELECT * FROM qualified_squads WHERE seriesId = :seriesId ORDER BY id ASC")
    fun getQualifiedSquadsForSeries(seriesId: Long): Flow<List<QualifiedSquadEntity>>

    @Query("SELECT * FROM qualified_squads ORDER BY id DESC")
    fun getAllQualifiedSquads(): Flow<List<QualifiedSquadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQualifiedSquad(squad: QualifiedSquadEntity): Long

    @Update
    suspend fun updateQualifiedSquad(squad: QualifiedSquadEntity)

    @Delete
    suspend fun deleteQualifiedSquad(squad: QualifiedSquadEntity)

    @Query("DELETE FROM qualified_squads WHERE id = :id")
    suspend fun deleteQualifiedSquadById(id: Long)
}

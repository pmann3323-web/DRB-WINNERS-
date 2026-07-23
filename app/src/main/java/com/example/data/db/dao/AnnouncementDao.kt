package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.db.entity.AnnouncementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements WHERE tournamentId = :tournamentId ORDER BY timestamp DESC")
    fun getAnnouncementsForTournament(tournamentId: Long): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)
}

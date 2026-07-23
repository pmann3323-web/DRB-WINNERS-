package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.AdEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdDao {
    @Query("SELECT * FROM advertisements WHERE type = 'SPLASH' AND isEnabled = 1 LIMIT 1")
    fun getActiveSplashAd(): Flow<AdEntity?>

    @Query("SELECT * FROM advertisements WHERE type = 'BANNER' AND isEnabled = 1")
    fun getActiveBannerAds(): Flow<List<AdEntity>>

    @Query("SELECT * FROM advertisements ORDER BY id DESC")
    fun getAllAds(): Flow<List<AdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: AdEntity): Long

    @Update
    suspend fun updateAd(ad: AdEntity)

    @Query("DELETE FROM advertisements WHERE id = :id")
    suspend fun deleteAdById(id: Long)
}

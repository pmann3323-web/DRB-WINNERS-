package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.db.entity.AdminSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminSettingDao {

    @Query("SELECT value FROM admin_settings WHERE `key` = :key")
    fun getSettingValue(key: String): Flow<String?>

    @Query("SELECT * FROM admin_settings")
    fun getAllSettings(): Flow<List<AdminSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AdminSettingEntity)
}

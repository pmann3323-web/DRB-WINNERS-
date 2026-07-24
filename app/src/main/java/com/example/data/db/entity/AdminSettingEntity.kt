package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_settings")
data class AdminSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "advertisements")
data class AdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "SPLASH" or "BANNER"
    val imageUrl: String = "",
    val imageResId: Int = 0,
    val targetUrl: String = "",
    val isEnabled: Boolean = true,
    val displayDurationSeconds: Int = 5,
    val startDate: String = "",
    val endDate: String = ""
)

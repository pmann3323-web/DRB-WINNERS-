package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "user_1",
    val name: String = "Mann Patel",
    val email: String = "mannpatel9094@gmail.com",
    val phoneNumber: String = "+91 9876543210",
    val profilePic: String = "",
    val walletBalance: Double = 500.0,
    val referralCode: String = "SIDHUMOSEWALA",
    val referredBy: String = "",
    val isBanned: Boolean = false,
    val role: String = "USER", // "USER" or "ADMIN"
    val joinedAt: Long = System.currentTimeMillis(),
    val totalEarnings: Double = 1250.0,
    val tournamentsWon: Int = 3
)

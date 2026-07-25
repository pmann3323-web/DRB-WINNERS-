package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_1",
    val userName: String = "Player One",
    val type: String, // "DEPOSIT", "WITHDRAWAL", "ENTRY_FEE", "WINNING", "REFUND", "BONUS"
    val amount: Double,
    val utrNumber: String = "",
    val upiId: String = "",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

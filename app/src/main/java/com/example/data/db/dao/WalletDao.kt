package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): WalletTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: WalletTransactionEntity)
}

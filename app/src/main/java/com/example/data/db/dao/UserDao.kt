package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY totalEarnings DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE id = :userId")
    suspend fun updateWalletBalance(userId: String, amount: Double)

    @Query("UPDATE users SET isBanned = :isBanned WHERE id = :userId")
    suspend fun setUserBannedStatus(userId: String, isBanned: Boolean)
}

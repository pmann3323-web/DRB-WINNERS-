package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.dao.AdDao
import com.example.data.db.dao.AnnouncementDao
import com.example.data.db.dao.MatchDao
import com.example.data.db.dao.NotificationDao
import com.example.data.db.dao.ParticipantDao
import com.example.data.db.dao.TeamDao
import com.example.data.db.dao.TournamentDao
import com.example.data.db.dao.UserDao
import com.example.data.db.dao.WalletDao
import com.example.data.db.entity.AdEntity
import com.example.data.db.entity.AnnouncementEntity
import com.example.data.db.entity.MatchEntity
import com.example.data.db.entity.NotificationEntity
import com.example.data.db.entity.ParticipantEntity
import com.example.data.db.entity.TeamEntity
import com.example.data.db.entity.TournamentEntity
import com.example.data.db.entity.UserEntity
import com.example.data.db.entity.WalletTransactionEntity

@Database(
    entities = [
        TournamentEntity::class,
        TeamEntity::class,
        MatchEntity::class,
        AnnouncementEntity::class,
        UserEntity::class,
        WalletTransactionEntity::class,
        AdEntity::class,
        NotificationEntity::class,
        ParticipantEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TournamentDatabase : RoomDatabase() {
    abstract fun tournamentDao(): TournamentDao
    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun adDao(): AdDao
    abstract fun notificationDao(): NotificationDao
    abstract fun participantDao(): ParticipantDao

    companion object {
        @Volatile
        private var INSTANCE: TournamentDatabase? = null

        fun getDatabase(context: Context): TournamentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TournamentDatabase::class.java,
                    "tournament_hub_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

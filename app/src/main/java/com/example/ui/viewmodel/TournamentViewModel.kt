package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.TournamentDatabase
import com.example.data.db.entity.AdEntity
import com.example.data.db.entity.AnnouncementEntity
import com.example.data.db.entity.MatchEntity
import com.example.data.db.entity.NotificationEntity
import com.example.data.db.entity.ParticipantEntity
import com.example.data.db.entity.TeamEntity
import com.example.data.db.entity.TournamentEntity
import com.example.data.db.entity.UserEntity
import com.example.data.db.entity.WalletTransactionEntity
import com.example.data.repository.TournamentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TournamentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TournamentRepository

    val currentUserId = MutableStateFlow("user_1")
    val isAdminMode = MutableStateFlow(false)

    val searchQuery = MutableStateFlow("")
    val selectedGameFilter = MutableStateFlow("All")
    val selectedTournamentId = MutableStateFlow<Long?>(null)

    val currentUser: StateFlow<UserEntity?>
    val allTournaments: StateFlow<List<TournamentEntity>>
    val activeSplashAd: StateFlow<AdEntity?>
    val activeBannerAds: StateFlow<List<AdEntity>>
    val userTransactions: StateFlow<List<WalletTransactionEntity>>
    val pendingTransactions: StateFlow<List<WalletTransactionEntity>>
    val allTransactions: StateFlow<List<WalletTransactionEntity>>
    val allAds: StateFlow<List<AdEntity>>
    val allUsers: StateFlow<List<UserEntity>>
    val joinedTournaments: StateFlow<List<ParticipantEntity>>
    val userNotifications: StateFlow<List<NotificationEntity>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTournament: StateFlow<TournamentEntity?>

    @OptIn(ExperimentalCoroutinesApi::class)
    val teamsForSelectedTournament: StateFlow<List<TeamEntity>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val matchesForSelectedTournament: StateFlow<List<MatchEntity>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val announcementsForSelectedTournament: StateFlow<List<AnnouncementEntity>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val participantsForSelectedTournament: StateFlow<List<ParticipantEntity>>

    init {
        val db = TournamentDatabase.getDatabase(application)
        repository = TournamentRepository(
            db.tournamentDao(),
            db.teamDao(),
            db.matchDao(),
            db.announcementDao(),
            db.userDao(),
            db.walletDao(),
            db.adDao(),
            db.notificationDao(),
            db.participantDao()
        )

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        currentUser = currentUserId.flatMapLatest { id ->
            repository.getUser(id)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        activeSplashAd = repository.activeSplashAd.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        activeBannerAds = repository.activeBannerAds.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userTransactions = currentUserId.flatMapLatest { id ->
            repository.getUserTransactions(id)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        pendingTransactions = repository.pendingTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allAds = repository.allAds.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allUsers = repository.allUsers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        joinedTournaments = currentUserId.flatMapLatest { id ->
            repository.getJoinedTournamentsForUser(id)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userNotifications = currentUserId.flatMapLatest { id ->
            repository.getUserNotifications(id)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTournaments = combine(
            repository.allTournaments,
            searchQuery,
            selectedGameFilter
        ) { tournaments, query, filter ->
            tournaments.filter { t ->
                val matchesQuery = query.isEmpty() || t.title.contains(query, ignoreCase = true) || t.gameType.contains(query, ignoreCase = true)
                val matchesFilter = filter == "All" || t.gameType.equals(filter, ignoreCase = true)
                matchesQuery && matchesFilter
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        selectedTournament = selectedTournamentId.flatMapLatest { id ->
            if (id != null) repository.getTournamentById(id) else flowOf(null)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        teamsForSelectedTournament = selectedTournamentId.flatMapLatest { id ->
            if (id != null) repository.getTeamsForTournament(id) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        matchesForSelectedTournament = selectedTournamentId.flatMapLatest { id ->
            if (id != null) repository.getMatchesForTournament(id) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        announcementsForSelectedTournament = selectedTournamentId.flatMapLatest { id ->
            if (id != null) repository.getAnnouncementsForTournament(id) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        participantsForSelectedTournament = selectedTournamentId.flatMapLatest { id ->
            if (id != null) repository.getParticipantsForTournament(id) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun selectTournament(id: Long?) {
        selectedTournamentId.value = id
    }

    fun toggleAdminMode() {
        isAdminMode.value = !isAdminMode.value
        currentUserId.value = if (isAdminMode.value) "admin_1" else "user_1"
    }

    fun joinTournament(
        tournamentId: Long,
        teamName: String,
        inGameId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val userName = user?.name ?: "Player"
            val result = repository.joinTournament(
                tournamentId = tournamentId,
                userId = currentUserId.value,
                userName = userName,
                teamName = teamName,
                inGameId = inGameId
            )
            onResult(result.first, result.second)
        }
    }

    fun cancelTournamentJoin(
        tournamentId: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.cancelTournamentJoin(tournamentId, currentUserId.value)
            onResult(result.first, result.second)
        }
    }

    fun submitDepositRequest(
        amount: Double,
        utrNumber: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            repository.submitDepositRequest(
                userId = currentUserId.value,
                userName = user?.name ?: "User",
                amount = amount,
                utrNumber = utrNumber
            )
            onDone()
        }
    }

    fun submitWithdrawalRequest(
        amount: Double,
        upiId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val result = repository.submitWithdrawalRequest(
                userId = currentUserId.value,
                userName = user?.name ?: "User",
                amount = amount,
                upiId = upiId
            )
            onResult(result.first, result.second)
        }
    }

    fun processWalletRequest(transactionId: Long, approve: Boolean, adminNote: String = "") {
        viewModelScope.launch {
            repository.processWalletRequest(transactionId, approve, adminNote)
        }
    }

    fun manualWalletAdjustment(userId: String, amount: Double, note: String) {
        viewModelScope.launch {
            repository.manualWalletAdjustment(userId, amount, note)
        }
    }

    fun toggleUserBan(userId: String, currentBanned: Boolean) {
        viewModelScope.launch {
            repository.toggleUserBan(userId, !currentBanned)
        }
    }

    fun updateUserProfile(name: String, email: String, profilePic: String) {
        viewModelScope.launch {
            repository.updateUserProfile(currentUserId.value, name, email, profilePic)
        }
    }

    fun createTournament(
        title: String,
        gameType: String,
        format: String,
        totalTeams: Int,
        entryFee: Double,
        prizePool: String,
        startDate: String,
        description: String,
        rules: String,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newId = repository.createTournament(
                title, gameType, format, totalTeams, prizePool, startDate, description, rules, entryFee
            )
            onCreated(newId)
        }
    }

    fun updateTournament(tournament: TournamentEntity) {
        viewModelScope.launch {
            repository.updateTournament(tournament)
        }
    }

    fun addOrUpdateAd(ad: AdEntity) {
        viewModelScope.launch {
            repository.addOrUpdateAd(ad)
        }
    }

    fun deleteAd(adId: Long) {
        viewModelScope.launch {
            repository.deleteAd(adId)
        }
    }

    fun sendBroadcastNotification(title: String, message: String) {
        viewModelScope.launch {
            repository.sendBroadcastNotification(title, message)
        }
    }

    fun registerTeam(
        tournamentId: Long,
        teamName: String,
        captainName: String,
        contactEmail: String,
        membersCount: Int,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repository.registerTeam(tournamentId, teamName, captainName, contactEmail, membersCount)
            onDone()
        }
    }

    fun updateMatchScore(
        matchId: Long,
        team1Score: Int,
        team2Score: Int,
        isCompleted: Boolean,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateMatchScore(matchId, team1Score, team2Score, isCompleted)
            onDone()
        }
    }

    fun addAnnouncement(tournamentId: Long, title: String, content: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addAnnouncement(tournamentId, title, content)
            onDone()
        }
    }

    fun deleteTournament(id: Long) {
        viewModelScope.launch {
            repository.deleteTournament(id)
            if (selectedTournamentId.value == id) {
                selectedTournamentId.value = null
            }
        }
    }
}

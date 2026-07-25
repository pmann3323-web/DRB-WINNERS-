package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.TournamentDatabase
import com.example.data.db.entity.AdEntity
import com.example.data.db.entity.AnnouncementEntity
import com.example.data.db.entity.ChatMessageEntity
import com.example.data.db.entity.MatchEntity
import com.example.data.db.entity.NotificationEntity
import com.example.data.db.entity.ParticipantEntity
import com.example.data.db.entity.QualifiedSquadEntity
import com.example.data.db.entity.TeamEntity
import com.example.data.db.entity.TournamentEntity
import com.example.data.db.entity.TournamentSeriesEntity
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

    val currentUserId = MutableStateFlow("user_guest")
    val isUserLoggedIn = MutableStateFlow(false)
    val isAdminMode = MutableStateFlow(false)

    val searchQuery = MutableStateFlow("")
    val selectedGameFilter = MutableStateFlow("All")
    val selectedTournamentId = MutableStateFlow<Long?>(null)

    val currentUser: StateFlow<UserEntity?>
    val allTournaments: StateFlow<List<TournamentEntity>>
    val allSeries: StateFlow<List<TournamentSeriesEntity>>
    val allQualifiedSquads: StateFlow<List<QualifiedSquadEntity>>
    val activeSplashAd: StateFlow<AdEntity?>
    val activeBannerAds: StateFlow<List<AdEntity>>
    val userTransactions: StateFlow<List<WalletTransactionEntity>>
    val pendingTransactions: StateFlow<List<WalletTransactionEntity>>
    val allTransactions: StateFlow<List<WalletTransactionEntity>>
    val allAds: StateFlow<List<AdEntity>>
    val allUsers: StateFlow<List<UserEntity>>
    val joinedTournaments: StateFlow<List<ParticipantEntity>>
    val userNotifications: StateFlow<List<NotificationEntity>>
    val allChatMessages: StateFlow<List<ChatMessageEntity>>
    val userChatMessages: StateFlow<List<ChatMessageEntity>>
    val depositUpiId: StateFlow<String>

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
            db.participantDao(),
            db.tournamentSeriesDao(),
            db.chatMessageDao(),
            db.adminSettingDao()
        )

        allSeries = repository.allSeries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allQualifiedSquads = repository.allQualifiedSquads.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        val prefs = application.getSharedPreferences("drb_auth_prefs", android.content.Context.MODE_PRIVATE)
        val savedUserId = prefs.getString("user_id", null)
        val savedLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (savedLoggedIn && !savedUserId.isNullOrEmpty()) {
            currentUserId.value = savedUserId
            isUserLoggedIn.value = true
        } else {
            currentUserId.value = "user_guest"
            isUserLoggedIn.value = false
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

        allChatMessages = repository.allChatMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userChatMessages = currentUserId.flatMapLatest { id ->
            repository.getChatMessagesForUser(id)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        depositUpiId = repository.depositUpiId.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "drbwinners@upi"
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

    fun updateUserProfile(name: String, email: String, profilePic: String, phoneNumber: String = "") {
        viewModelScope.launch {
            repository.updateUserProfile(currentUserId.value, name, email, profilePic, phoneNumber)
        }
    }

    private fun saveUserSession(userId: String) {
        val prefs = getApplication<Application>().getSharedPreferences("drb_auth_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_logged_in", true).putString("user_id", userId).apply()
        currentUserId.value = userId
        isUserLoggedIn.value = true
    }

    fun loginWithPhone(phoneNumber: String, name: String = "Player", onDone: () -> Unit) {
        viewModelScope.launch {
            val cleanPhone = phoneNumber.trim()
            val userId = "user_phone_${cleanPhone.filter { it.isDigit() }}"
            repository.loginOrRegisterUser(
                userId = userId,
                name = if (name.isNotEmpty()) name else "Pro Gamer",
                email = "user_${cleanPhone.takeLast(4)}@tournamenthub.com",
                phoneNumber = cleanPhone
            )
            saveUserSession(userId)
            onDone()
        }
    }

    fun loginWithGoogle(accountName: String, accountEmail: String, photoUrl: String = "", profilePic: String = "", onDone: () -> Unit) {
        viewModelScope.launch {
            val cleanEmail = accountEmail.trim().lowercase()
            val cleanName = if (accountName.isNotBlank()) accountName else cleanEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
            
            // Sync user with Firebase Authentication API
            val firebasePass = "GoogleAuth#2026!"
            var firebaseResult = com.example.data.auth.FirebaseAuthManager.signUpWithEmail(cleanEmail, firebasePass)
            if (!firebaseResult.isSuccess) {
                firebaseResult = com.example.data.auth.FirebaseAuthManager.signInWithEmail(cleanEmail, firebasePass)
            }
            
            val firebaseUid = firebaseResult.localId
            val userId = if (!firebaseUid.isNullOrBlank()) "user_firebase_$firebaseUid" else "user_google_${cleanEmail.replace("@", "_").replace(".", "_")}"

            repository.loginOrRegisterUser(
                userId = userId,
                name = cleanName,
                email = cleanEmail,
                phoneNumber = "",
                profilePic = profilePic
            )
            saveUserSession(userId)
            onDone()
        }
    }

    fun checkEmailVerificationStatus(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanEmail = email.trim()
            val cleanPass = password.trim()
            if (cleanEmail.isBlank()) {
                onResult(false, "Please enter your email address")
                return@launch
            }
            val result = com.example.data.auth.FirebaseAuthManager.signInWithEmail(cleanEmail, cleanPass)
            if (result.isSuccess && result.idToken != null) {
                val isVerified = com.example.data.auth.FirebaseAuthManager.checkEmailVerified(result.idToken)
                if (isVerified) {
                    val userId = "user_firebase_${result.localId ?: result.email?.replace("@", "_")?.replace(".", "_")}"
                    repository.loginOrRegisterUser(
                        userId = userId,
                        name = result.email?.substringBefore("@") ?: "Player",
                        email = result.email ?: cleanEmail,
                        phoneNumber = ""
                    )
                    saveUserSession(userId)
                    onResult(true, "Email verified successfully!")
                } else {
                    onResult(false, "Email is not verified yet. Please check your inbox and click the link.")
                }
            } else {
                onResult(false, "Email is not verified yet. Please check your inbox or sign in.")
            }
        }
    }

    fun resendVerificationEmail(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanEmail = email.trim()
            val cleanPass = password.trim()
            val result = com.example.data.auth.FirebaseAuthManager.signInWithEmail(cleanEmail, cleanPass)
            if (result.isSuccess && result.idToken != null) {
                val res = com.example.data.auth.FirebaseAuthManager.sendEmailVerification(result.idToken)
                if (res.isSuccess) {
                    onResult(true, "Verification link sent to $cleanEmail!")
                } else {
                    onResult(false, "Failed to send verification email.")
                }
            } else {
                onResult(false, "Could not send verification email. Please check your details.")
            }
        }
    }

    enum class FirebaseAuthStatus {
        SUCCESS,
        UNVERIFIED_EMAIL,
        ERROR
    }

    fun firebaseSignInWithEmail(
        email: String,
        password: String,
        onResult: (FirebaseAuthStatus, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = com.example.data.auth.FirebaseAuthManager.signInWithEmail(email, password)
            if (result.isSuccess && result.email != null && result.idToken != null) {
                val isVerified = com.example.data.auth.FirebaseAuthManager.checkEmailVerified(result.idToken)
                if (isVerified) {
                    val userId = "user_firebase_${result.localId ?: result.email.replace("@", "_").replace(".", "_")}"
                    repository.loginOrRegisterUser(
                        userId = userId,
                        name = result.email.substringBefore("@"),
                        email = result.email,
                        phoneNumber = ""
                    )
                    saveUserSession(userId)
                    onResult(FirebaseAuthStatus.SUCCESS, null, result.email)
                } else {
                    // Send verification email again if not verified
                    com.example.data.auth.FirebaseAuthManager.sendEmailVerification(result.idToken)
                    onResult(FirebaseAuthStatus.UNVERIFIED_EMAIL, null, result.email)
                }
            } else {
                onResult(FirebaseAuthStatus.ERROR, result.errorMessage ?: "Email or password is incorrect", null)
            }
        }
    }

    fun firebaseSignUpWithEmail(
        email: String,
        password: String,
        onResult: (FirebaseAuthStatus, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = com.example.data.auth.FirebaseAuthManager.signUpWithEmail(email, password)
            if (result.isSuccess && result.email != null && result.idToken != null) {
                com.example.data.auth.FirebaseAuthManager.sendEmailVerification(result.idToken)
                val userId = "user_firebase_${result.localId ?: result.email.replace("@", "_").replace(".", "_")}"
                repository.loginOrRegisterUser(
                    userId = userId,
                    name = result.email.substringBefore("@"),
                    email = result.email,
                    phoneNumber = ""
                )
                // DO NOT sign them in automatically!
                onResult(FirebaseAuthStatus.UNVERIFIED_EMAIL, null, result.email)
            } else {
                onResult(FirebaseAuthStatus.ERROR, result.errorMessage ?: "User already exists. Please sign in", null)
            }
        }
    }

    fun firebaseSignUpFull(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        password: String,
        profilePic: String = "",
        onResult: (FirebaseAuthStatus, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = com.example.data.auth.FirebaseAuthManager.signUpWithEmail(email, password)
            if (result.isSuccess && result.email != null && result.idToken != null) {
                com.example.data.auth.FirebaseAuthManager.sendEmailVerification(result.idToken)
                val userId = "user_firebase_${result.localId ?: result.email.replace("@", "_").replace(".", "_")}"
                val displayName = fullName.ifBlank { username.ifBlank { result.email.substringBefore("@") } }
                repository.loginOrRegisterUser(
                    userId = userId,
                    name = displayName,
                    email = result.email,
                    phoneNumber = phone.ifBlank { "+91 9876543210" }
                )
                // DO NOT sign them in automatically!
                onResult(FirebaseAuthStatus.UNVERIFIED_EMAIL, null, result.email)
            } else {
                onResult(FirebaseAuthStatus.ERROR, result.errorMessage ?: "User already exists. Please sign in", null)
            }
        }
    }

    fun firebaseSendPasswordResetEmail(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = com.example.data.auth.FirebaseAuthManager.sendPasswordResetEmail(email)
            onResult(result.isSuccess, result.errorMessage)
        }
    }

    fun loginWithEmail(email: String, name: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val cleanEmail = email.trim()
            val userId = "user_email_${cleanEmail.replace("@", "_").replace(".", "_")}"
            repository.loginOrRegisterUser(
                userId = userId,
                name = name.ifEmpty { cleanEmail.substringBefore("@") },
                email = cleanEmail,
                phoneNumber = "+91 9876543210"
            )
            saveUserSession(userId)
            onDone()
        }
    }

    fun logoutUser() {
        val prefs = getApplication<Application>().getSharedPreferences("drb_auth_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_logged_in", false).remove("user_id").apply()
        currentUserId.value = "user_guest"
        isUserLoggedIn.value = false
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
        roomId: String = "",
        roomPassword: String = "",
        matchStartTimeMillis: Long = System.currentTimeMillis() + 600000L,
        matchMode: String = "Squad",
        firstPrize: Double = 300.0,
        secondPrize: Double = 150.0,
        thirdPrize: Double = 50.0,
        perKillPrize: Double = 10.0,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newId = repository.createTournament(
                title = title,
                gameType = gameType,
                format = format,
                totalTeams = totalTeams,
                prizePool = prizePool,
                startDate = startDate,
                description = description,
                rules = rules,
                entryFee = entryFee,
                roomId = roomId,
                roomPassword = roomPassword,
                matchStartTimeMillis = matchStartTimeMillis,
                matchMode = matchMode,
                firstPrize = firstPrize,
                secondPrize = secondPrize,
                thirdPrize = thirdPrize,
                perKillPrize = perKillPrize
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

    fun createTournamentSeries(
        title: String,
        gameType: String = "Free Fire",
        matchMode: String = "Squad",
        qualifiersCount: Int = 6,
        topQualifyPerRoom: Int = 2,
        entryFeePerSquad: Double = 100.0,
        prizePool: String = "₹20,000",
        firstPrize: Double = 10000.0,
        secondPrize: Double = 5000.0,
        thirdPrize: Double = 2500.0,
        perKillPrize: Double = 50.0,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val seriesId = repository.createTournamentSeries(
                title = title,
                gameType = gameType,
                matchMode = matchMode,
                qualifiersCount = qualifiersCount,
                topQualifyPerRoom = topQualifyPerRoom,
                entryFeePerSquad = entryFeePerSquad,
                prizePool = prizePool,
                firstPrize = firstPrize,
                secondPrize = secondPrize,
                thirdPrize = thirdPrize,
                perKillPrize = perKillPrize
            )
            onCreated(seriesId)
        }
    }

    fun addQualifiedSquad(
        seriesId: Long,
        qualifierTournamentId: Long,
        squadName: String,
        captainName: String,
        userId: String = "",
        inGameId: String = "",
        qualifierRank: Int = 1,
        killsCount: Int = 0,
        points: Int = 0,
        onAdded: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addQualifiedSquad(
                seriesId = seriesId,
                qualifierTournamentId = qualifierTournamentId,
                squadName = squadName,
                captainName = captainName,
                userId = userId,
                inGameId = inGameId,
                qualifierRank = qualifierRank,
                killsCount = killsCount,
                points = points
            )
            onAdded()
        }
    }

    fun deleteQualifiedSquad(id: Long) {
        viewModelScope.launch {
            repository.deleteQualifiedSquad(id)
        }
    }

    fun generateFinalTournament(seriesId: Long, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val finalId = repository.generateFinalTournament(seriesId)
            onCreated(finalId)
        }
    }

    fun declareSeriesWinners(
        seriesId: Long,
        winnerTeamName: String,
        winnerCaptain: String,
        winnerKills: Int,
        secondTeamName: String,
        thirdTeamName: String,
        onDeclared: () -> Unit
    ) {
        viewModelScope.launch {
            repository.declareSeriesWinners(
                seriesId = seriesId,
                winnerTeamName = winnerTeamName,
                winnerCaptain = winnerCaptain,
                winnerKills = winnerKills,
                secondTeamName = secondTeamName,
                thirdTeamName = thirdTeamName
            )
            onDeclared()
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

    fun sendUserChatMessage(messageText: String) {
        if (messageText.isBlank()) return
        viewModelScope.launch {
            val user = currentUser.value
            val userId = currentUserId.value
            val userName = user?.name ?: "Player"
            repository.sendChatMessage(
                userId = userId,
                senderId = userId,
                senderName = userName,
                receiverId = "admin",
                message = messageText.trim(),
                isAdmin = false
            )
        }
    }

    fun sendAdminChatMessage(targetUserId: String, messageText: String) {
        if (messageText.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage(
                userId = targetUserId,
                senderId = "admin",
                senderName = "Tournament Admin",
                receiverId = targetUserId,
                message = messageText.trim(),
                isAdmin = true
            )
        }
    }

    fun markChatAsRead(userId: String, isAdminPerspective: Boolean) {
        viewModelScope.launch {
            repository.markChatAsReadForUser(userId, isAdminPerspective)
        }
    }

    fun updateDepositUpiId(newUpiId: String) {
        viewModelScope.launch {
            repository.updateDepositUpiId(newUpiId)
        }
    }

    fun updateRoomCredentials(tournamentId: Long, roomId: String, roomPassword: String) {
        viewModelScope.launch {
            repository.updateTournamentRoomCredentials(tournamentId, roomId, roomPassword)
        }
    }
}

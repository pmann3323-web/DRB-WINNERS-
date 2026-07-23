package com.example.data.repository

import com.example.R
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TournamentRepository(
    private val tournamentDao: TournamentDao,
    private val teamDao: TeamDao,
    private val matchDao: MatchDao,
    private val announcementDao: AnnouncementDao,
    private val userDao: UserDao,
    private val walletDao: WalletDao,
    private val adDao: AdDao,
    private val notificationDao: NotificationDao,
    private val participantDao: ParticipantDao
) {
    val allTournaments: Flow<List<TournamentEntity>> = tournamentDao.getAllTournaments()
    val activeSplashAd: Flow<AdEntity?> = adDao.getActiveSplashAd()
    val activeBannerAds: Flow<List<AdEntity>> = adDao.getActiveBannerAds()
    val allAds: Flow<List<AdEntity>> = adDao.getAllAds()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val pendingTransactions: Flow<List<WalletTransactionEntity>> = walletDao.getPendingTransactions()
    val allTransactions: Flow<List<WalletTransactionEntity>> = walletDao.getAllTransactions()

    fun getUser(userId: String): Flow<UserEntity?> = userDao.getUserById(userId)

    fun getUserTransactions(userId: String): Flow<List<WalletTransactionEntity>> =
        walletDao.getTransactionsForUser(userId)

    fun getUserNotifications(userId: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)

    fun getTournamentById(id: Long): Flow<TournamentEntity?> = tournamentDao.getTournamentById(id)

    fun getTeamsForTournament(tournamentId: Long): Flow<List<TeamEntity>> = teamDao.getTeamsForTournament(tournamentId)

    fun getMatchesForTournament(tournamentId: Long): Flow<List<MatchEntity>> = matchDao.getMatchesForTournament(tournamentId)

    fun getAnnouncementsForTournament(tournamentId: Long): Flow<List<AnnouncementEntity>> = announcementDao.getAnnouncementsForTournament(tournamentId)

    fun getParticipantsForTournament(tournamentId: Long): Flow<List<ParticipantEntity>> =
        participantDao.getParticipantsForTournament(tournamentId)

    fun getJoinedTournamentsForUser(userId: String): Flow<List<ParticipantEntity>> =
        participantDao.getJoinedTournamentsForUser(userId)

    suspend fun createTournament(
        title: String,
        gameType: String,
        format: String,
        totalTeams: Int,
        prizePool: String,
        startDate: String,
        description: String,
        rules: String,
        entryFee: Double = 50.0,
        maxPlayers: Int = 100
    ): Long {
        val newTournament = TournamentEntity(
            title = title,
            gameType = gameType,
            format = format,
            status = "UPCOMING",
            startDate = startDate,
            totalTeams = totalTeams,
            currentTeamsCount = 0,
            entryFee = entryFee,
            prizePool = prizePool,
            bannerResId = R.drawable.img_tournament_hero_1784788925449,
            description = description,
            rules = rules,
            maxPlayers = maxPlayers,
            registrationOpen = true
        )
        return tournamentDao.insertTournament(newTournament)
    }

    suspend fun updateTournament(tournament: TournamentEntity) {
        tournamentDao.updateTournament(tournament)
    }

    suspend fun joinTournament(
        tournamentId: Long,
        userId: String,
        userName: String,
        teamName: String,
        inGameId: String
    ): Pair<Boolean, String> {
        val user = userDao.getUserById(userId).first() ?: return Pair(false, "User not found")
        if (user.isBanned) return Pair(false, "Your account is currently restricted.")

        val tournament = tournamentDao.getTournamentById(tournamentId).first() ?: return Pair(false, "Tournament not found")
        if (!tournament.registrationOpen || tournament.status == "COMPLETED") {
            return Pair(false, "Registrations are closed for this tournament.")
        }

        val existing = participantDao.getParticipant(tournamentId, userId)
        if (existing != null) {
            return Pair(false, "You have already joined this tournament.")
        }

        if (user.walletBalance < tournament.entryFee) {
            return Pair(false, "Insufficient wallet balance. Please add funds to join.")
        }

        // Deduct entry fee
        userDao.updateWalletBalance(userId, -tournament.entryFee)

        // Add wallet transaction record
        walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                userName = userName,
                type = "ENTRY_FEE",
                amount = tournament.entryFee,
                status = "APPROVED",
                note = "Joined tournament: ${tournament.title}"
            )
        )

        // Add participant record
        participantDao.insertParticipant(
            ParticipantEntity(
                tournamentId = tournamentId,
                userId = userId,
                userName = userName,
                teamName = teamName,
                inGameId = inGameId,
                entryFeePaid = tournament.entryFee,
                status = "JOINED"
            )
        )

        // Increment team/participant count
        val updatedCount = tournament.currentTeamsCount + 1
        tournamentDao.updateTournament(
            tournament.copy(currentTeamsCount = updatedCount)
        )

        // Register team entry in teamDao
        registerTeam(tournamentId, teamName, userName, user.email, 1)

        return Pair(true, "Successfully joined the tournament!")
    }

    suspend fun cancelTournamentJoin(tournamentId: Long, userId: String): Pair<Boolean, String> {
        val tournament = tournamentDao.getTournamentById(tournamentId).first() ?: return Pair(false, "Tournament not found")
        if (tournament.status != "UPCOMING") {
            return Pair(false, "Cannot cancel after tournament has started.")
        }

        val participant = participantDao.getParticipant(tournamentId, userId)
            ?: return Pair(false, "Participant record not found.")

        // Refund entry fee
        userDao.updateWalletBalance(userId, participant.entryFeePaid)

        // Add transaction log
        walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                userName = participant.userName,
                type = "REFUND",
                amount = participant.entryFeePaid,
                status = "APPROVED",
                note = "Refund for cancelled entry: ${tournament.title}"
            )
        )

        participantDao.cancelParticipant(tournamentId, userId)

        // Decrement team count
        val newCount = (tournament.currentTeamsCount - 1).coerceAtLeast(0)
        tournamentDao.updateTournament(tournament.copy(currentTeamsCount = newCount))

        return Pair(true, "Tournament entry cancelled and entry fee refunded to wallet.")
    }

    suspend fun submitDepositRequest(
        userId: String,
        userName: String,
        amount: Double,
        utrNumber: String
    ): Long {
        return walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                userName = userName,
                type = "DEPOSIT",
                amount = amount,
                utrNumber = utrNumber,
                upiId = "mannpatel9094@fam",
                status = "PENDING",
                note = "Deposit request via UPI UTR: $utrNumber"
            )
        )
    }

    suspend fun submitWithdrawalRequest(
        userId: String,
        userName: String,
        amount: Double,
        upiId: String
    ): Pair<Boolean, String> {
        val user = userDao.getUserById(userId).first() ?: return Pair(false, "User not found")
        if (user.walletBalance < amount) {
            return Pair(false, "Insufficient balance for withdrawal.")
        }

        // Deduct temporarily pending withdrawal approval
        userDao.updateWalletBalance(userId, -amount)

        walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                userName = userName,
                type = "WITHDRAWAL",
                amount = amount,
                upiId = upiId,
                status = "PENDING",
                note = "Withdrawal request to $upiId"
            )
        )

        return Pair(true, "Withdrawal request submitted for Admin verification.")
    }

    suspend fun processWalletRequest(transactionId: Long, approve: Boolean, adminNote: String = "") {
        val txn = walletDao.getTransactionById(transactionId) ?: return
        val newStatus = if (approve) "APPROVED" else "REJECTED"

        walletDao.updateTransaction(txn.copy(status = newStatus, note = adminNote.ifEmpty { txn.note }))

        if (txn.type == "DEPOSIT" && approve) {
            // Credit user balance
            userDao.updateWalletBalance(txn.userId, txn.amount)
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = txn.userId,
                    title = "Deposit Approved! ₹${txn.amount.toInt()}",
                    message = "Your deposit request (UTR: ${txn.utrNumber}) of ₹${txn.amount} has been verified and added to your wallet."
                )
            )
        } else if (txn.type == "DEPOSIT" && !approve) {
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = txn.userId,
                    title = "Deposit Request Declined",
                    message = "Your deposit request (UTR: ${txn.utrNumber}) could not be verified. Please check UTR and retry."
                )
            )
        } else if (txn.type == "WITHDRAWAL" && !approve) {
            // Refund the deducted amount on rejection
            userDao.updateWalletBalance(txn.userId, txn.amount)
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = txn.userId,
                    title = "Withdrawal Rejected",
                    message = "Your withdrawal request of ₹${txn.amount} was rejected and refunded to your wallet."
                )
            )
        } else if (txn.type == "WITHDRAWAL" && approve) {
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = txn.userId,
                    title = "Withdrawal Processed",
                    message = "Your payout of ₹${txn.amount} to UPI ID ${txn.upiId} has been successfully sent."
                )
            )
        }
    }

    suspend fun manualWalletAdjustment(userId: String, amount: Double, note: String) {
        userDao.updateWalletBalance(userId, amount)
        val user = userDao.getUserById(userId).first()
        walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                userName = user?.name ?: "User",
                type = if (amount >= 0) "BONUS" else "ENTRY_FEE",
                amount = Math.abs(amount),
                status = "APPROVED",
                note = "Admin Adjustment: $note"
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Wallet Balance Updated",
                message = "Admin adjusted your balance by ₹${amount.toInt()}. Reason: $note"
            )
        )
    }

    suspend fun toggleUserBan(userId: String, isBanned: Boolean) {
        userDao.setUserBannedStatus(userId, isBanned)
    }

    suspend fun updateUserProfile(userId: String, name: String, email: String, profilePic: String) {
        val user = userDao.getUserById(userId).first() ?: return
        userDao.updateUser(user.copy(name = name, email = email, profilePic = profilePic))
    }

    suspend fun addOrUpdateAd(ad: AdEntity): Long {
        return adDao.insertAd(ad)
    }

    suspend fun deleteAd(adId: Long) {
        adDao.deleteAdById(adId)
    }

    suspend fun sendBroadcastNotification(title: String, message: String) {
        notificationDao.insertNotification(
            NotificationEntity(userId = "ALL", title = title, message = message)
        )
    }

    suspend fun registerTeam(
        tournamentId: Long,
        teamName: String,
        captainName: String,
        contactEmail: String,
        membersCount: Int
    ): Long {
        val tournament = tournamentDao.getTournamentById(tournamentId).first() ?: return -1L
        
        val team = TeamEntity(
            tournamentId = tournamentId,
            name = teamName,
            captainName = captainName,
            contactEmail = contactEmail,
            membersCount = membersCount,
            seed = tournament.currentTeamsCount + 1
        )
        val teamId = teamDao.insertTeam(team)

        val updatedCount = tournament.currentTeamsCount + 1
        val newStatus = if (updatedCount >= tournament.totalTeams && tournament.status == "UPCOMING") "LIVE" else tournament.status
        tournamentDao.updateTournament(tournament.copy(currentTeamsCount = updatedCount, status = newStatus))

        if (updatedCount >= tournament.totalTeams) {
            generateFixturesForTournament(tournamentId)
        }

        return teamId
    }

    suspend fun updateMatchScore(
        matchId: Long,
        team1Score: Int,
        team2Score: Int,
        isCompleted: Boolean
    ) {
        val match = matchDao.getMatchById(matchId) ?: return
        var winner = ""
        val status = if (isCompleted) "COMPLETED" else "LIVE"

        if (isCompleted) {
            winner = when {
                team1Score > team2Score -> match.team1Name
                team2Score > team1Score -> match.team2Name
                else -> "DRAW"
            }
        }

        val updatedMatch = match.copy(
            team1Score = team1Score,
            team2Score = team2Score,
            winnerName = winner,
            status = status
        )
        matchDao.updateMatch(updatedMatch)

        val tournament = tournamentDao.getTournamentById(match.tournamentId).first()
        if (tournament != null && isCompleted) {
            updateTeamStatsAfterMatch(match.tournamentId, match.team1Name, match.team2Name, team1Score, team2Score)
            
            if (tournament.format.contains("Elimination") || tournament.format.contains("Knockout")) {
                advanceWinnerInKnockout(match.tournamentId, match.roundName, winner)
            }
        }
    }

    private suspend fun updateTeamStatsAfterMatch(
        tournamentId: Long,
        team1Name: String,
        team2Name: String,
        team1Score: Int,
        team2Score: Int
    ) {
        val teams = teamDao.getTeamsForTournament(tournamentId).first()
        val t1 = teams.find { it.name.equals(team1Name, ignoreCase = true) }
        val t2 = teams.find { it.name.equals(team2Name, ignoreCase = true) }

        if (t1 != null) {
            val wins = if (team1Score > team2Score) t1.wins + 1 else t1.wins
            val losses = if (team1Score < team2Score) t1.losses + 1 else t1.losses
            val draws = if (team1Score == team2Score) t1.draws + 1 else t1.draws
            val points = wins * 3 + draws * 1
            val diff = t1.netRunRateOrDiff + (team1Score - team2Score)
            teamDao.updateTeam(t1.copy(matchesPlayed = t1.matchesPlayed + 1, wins = wins, losses = losses, draws = draws, points = points, netRunRateOrDiff = diff))
        }

        if (t2 != null) {
            val wins = if (team2Score > team1Score) t2.wins + 1 else t2.wins
            val losses = if (team2Score < team1Score) t2.losses + 1 else t2.losses
            val draws = if (team1Score == team2Score) t2.draws + 1 else t2.draws
            val points = wins * 3 + draws * 1
            val diff = t2.netRunRateOrDiff + (team2Score - team1Score)
            teamDao.updateTeam(t2.copy(matchesPlayed = t2.matchesPlayed + 1, wins = wins, losses = losses, draws = draws, points = points, netRunRateOrDiff = diff))
        }
    }

    private suspend fun advanceWinnerInKnockout(tournamentId: Long, currentRound: String, winnerName: String) {
        if (winnerName.isEmpty() || winnerName == "DRAW") return
        val allMatches = matchDao.getMatchesForTournament(tournamentId).first()
        
        val targetRound = when (currentRound) {
            "Quarter Final" -> "Semi Final"
            "Semi Final" -> "Final"
            else -> null
        } ?: return

        val nextRoundMatches = allMatches.filter { it.roundName == targetRound }
        for (nextMatch in nextRoundMatches) {
            if (nextMatch.team1Name == "TBD" || nextMatch.team1Name.isEmpty()) {
                matchDao.updateMatch(nextMatch.copy(team1Name = winnerName))
                break
            } else if (nextMatch.team2Name == "TBD" || nextMatch.team2Name.isEmpty()) {
                matchDao.updateMatch(nextMatch.copy(team2Name = winnerName))
                break
            }
        }
    }

    suspend fun generateFixturesForTournament(tournamentId: Long) {
        val teams = teamDao.getTeamsForTournament(tournamentId).first()
        if (teams.isEmpty()) return

        val matches = mutableListOf<MatchEntity>()
        var matchCount = 1

        if (teams.size >= 4) {
            val is8 = teams.size >= 8
            val firstRoundName = if (is8) "Quarter Final" else "Semi Final"

            for (i in 0 until teams.size step 2) {
                if (i + 1 < teams.size) {
                    matches.add(
                        MatchEntity(
                            tournamentId = tournamentId,
                            roundName = firstRoundName,
                            matchNumber = matchCount++,
                            team1Name = teams[i].name,
                            team2Name = teams[i + 1].name,
                            startTime = "Today, 18:00",
                            venueOrMap = "Main Arena"
                        )
                    )
                }
            }

            if (is8) {
                matches.add(MatchEntity(tournamentId = tournamentId, roundName = "Semi Final", matchNumber = matchCount++, team1Name = "TBD", team2Name = "TBD", startTime = "Tomorrow, 16:00"))
                matches.add(MatchEntity(tournamentId = tournamentId, roundName = "Semi Final", matchNumber = matchCount++, team1Name = "TBD", team2Name = "TBD", startTime = "Tomorrow, 18:00"))
            }

            matches.add(MatchEntity(tournamentId = tournamentId, roundName = "Final", matchNumber = matchCount++, team1Name = "TBD", team2Name = "TBD", startTime = "Sunday, 20:00"))

            matchDao.insertMatches(matches)
        }
    }

    suspend fun addAnnouncement(tournamentId: Long, title: String, content: String) {
        announcementDao.insertAnnouncement(
            AnnouncementEntity(
                tournamentId = tournamentId,
                title = title,
                content = content
            )
        )
    }

    suspend fun deleteTournament(id: Long) {
        tournamentDao.deleteTournamentById(id)
    }

    suspend fun seedInitialDataIfEmpty() {
        val currentUser = userDao.getUserById("user_1").first()
        if (currentUser == null) {
            userDao.insertUser(
                UserEntity(
                    id = "user_1",
                    name = "Mann Patel",
                    email = "mannpatel9094@gmail.com",
                    walletBalance = 500.0,
                    referralCode = "MANN9094",
                    role = "USER",
                    totalEarnings = 1250.0,
                    tournamentsWon = 3
                )
            )
            userDao.insertUser(
                UserEntity(
                    id = "admin_1",
                    name = "Admin Master",
                    email = "admin@tournamenthub.com",
                    walletBalance = 10000.0,
                    referralCode = "ADMIN007",
                    role = "ADMIN"
                )
            )
        }

        val existingAds = adDao.getAllAds().first()
        if (existingAds.isEmpty()) {
            adDao.insertAd(
                AdEntity(
                    title = "Season 5 Championship Grand Opener",
                    type = "SPLASH",
                    imageResId = R.drawable.img_splash_ad_1784790267758,
                    isEnabled = true,
                    displayDurationSeconds = 5,
                    targetUrl = "https://tournamenthub.app"
                )
            )
            adDao.insertAd(
                AdEntity(
                    title = "Win ₹2,50,000 in BGMI Pro Showdown!",
                    type = "BANNER",
                    imageResId = R.drawable.img_tournament_hero_1784788925449,
                    isEnabled = true,
                    startDate = "2026-07-20",
                    endDate = "2026-08-10"
                )
            )
        }

        val existingNotifications = notificationDao.getNotificationsForUser("user_1").first()
        if (existingNotifications.isEmpty()) {
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = "ALL",
                    title = "Welcome to Tournament Hub!",
                    message = "Join tournaments, compete with top squads, and claim cash prize pools into your wallet!"
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = "user_1",
                    title = "Wallet Welcome Bonus",
                    message = "₹500 welcome bonus credited to your wallet balance."
                )
            )
        }

        val currentList = tournamentDao.getAllTournaments().first()
        if (currentList.isNotEmpty()) return

        // Seed Tournament 1: BGMI Pro Showdown (LIVE)
        val t1Id = tournamentDao.insertTournament(
            TournamentEntity(
                title = "BGMI Pro Showdown 2026",
                gameType = "BGMI/Esports",
                format = "Single Elimination",
                status = "LIVE",
                startDate = "22 July 2026",
                totalTeams = 8,
                currentTeamsCount = 8,
                entryFee = 100.0,
                prizePool = "₹2,50,000",
                bannerResId = R.drawable.img_tournament_hero_1784788925449,
                description = "The ultimate BGMI battleground tournament featuring top Esports squads competing for glory and massive prize pool.",
                rules = "1. Erangel & Miramar maps.\n2. Point system: 15 pts for Winner, 1 pt per kill.\n3. Screen recording mandatory for all squad captains."
            )
        )

        val t1Teams = listOf(
            TeamEntity(tournamentId = t1Id, name = "GodLike Esports", captainName = "Jonathan", membersCount = 4, seed = 1, matchesPlayed = 2, wins = 2, losses = 0, points = 6, netRunRateOrDiff = 42f),
            TeamEntity(tournamentId = t1Id, name = "Team XSpark", captainName = "Scout", membersCount = 4, seed = 2, matchesPlayed = 2, wins = 1, losses = 1, points = 3, netRunRateOrDiff = 18f),
            TeamEntity(tournamentId = t1Id, name = "Soul Warriors", captainName = "Mortal", membersCount = 4, seed = 3, matchesPlayed = 2, wins = 1, losses = 1, points = 3, netRunRateOrDiff = 12f),
            TeamEntity(tournamentId = t1Id, name = "Blind Esports", captainName = "Manya", membersCount = 4, seed = 4, matchesPlayed = 2, wins = 1, losses = 1, points = 3, netRunRateOrDiff = 5f),
            TeamEntity(tournamentId = t1Id, name = "Entity Gaming", captainName = "Gamlaboy", membersCount = 4, seed = 5, matchesPlayed = 1, wins = 0, losses = 1, points = 0, netRunRateOrDiff = -10f),
            TeamEntity(tournamentId = t1Id, name = "Global Esports", captainName = "Mavi", membersCount = 4, seed = 6, matchesPlayed = 1, wins = 0, losses = 1, points = 0, netRunRateOrDiff = -15f),
            TeamEntity(tournamentId = t1Id, name = "Orangutan", captainName = "Ash", membersCount = 4, seed = 7, matchesPlayed = 1, wins = 0, losses = 1, points = 0, netRunRateOrDiff = -20f),
            TeamEntity(tournamentId = t1Id, name = "Reckoning", captainName = "Punk", membersCount = 4, seed = 8, matchesPlayed = 1, wins = 0, losses = 1, points = 0, netRunRateOrDiff = -22f)
        )
        t1Teams.forEach { teamDao.insertTeam(it) }

        val t1Matches = listOf(
            MatchEntity(tournamentId = t1Id, roundName = "Quarter Final", matchNumber = 1, team1Name = "GodLike Esports", team2Name = "Entity Gaming", team1Score = 22, team2Score = 8, winnerName = "GodLike Esports", status = "COMPLETED", startTime = "Today 16:00", venueOrMap = "Erangel"),
            MatchEntity(tournamentId = t1Id, roundName = "Quarter Final", matchNumber = 2, team1Name = "Team XSpark", team2Name = "Global Esports", team1Score = 18, team2Score = 12, winnerName = "Team XSpark", status = "COMPLETED", startTime = "Today 17:00", venueOrMap = "Miramar"),
            MatchEntity(tournamentId = t1Id, roundName = "Quarter Final", matchNumber = 3, team1Name = "Soul Warriors", team2Name = "Orangutan", team1Score = 15, team2Score = 10, winnerName = "Soul Warriors", status = "COMPLETED", startTime = "Today 18:00", venueOrMap = "Sanhok"),
            MatchEntity(tournamentId = t1Id, roundName = "Quarter Final", matchNumber = 4, team1Name = "Blind Esports", team2Name = "Reckoning", team1Score = 14, team2Score = 9, winnerName = "Blind Esports", status = "COMPLETED", startTime = "Today 19:00", venueOrMap = "Vikendi"),
            MatchEntity(tournamentId = t1Id, roundName = "Semi Final", matchNumber = 5, team1Name = "GodLike Esports", team2Name = "Team XSpark", team1Score = 0, team2Score = 0, winnerName = "", status = "LIVE", startTime = "Now Playing", venueOrMap = "Erangel Arena"),
            MatchEntity(tournamentId = t1Id, roundName = "Semi Final", matchNumber = 6, team1Name = "Soul Warriors", team2Name = "Blind Esports", team1Score = 0, team2Score = 0, winnerName = "", status = "SCHEDULED", startTime = "Tonight 21:00", venueOrMap = "Miramar"),
            MatchEntity(tournamentId = t1Id, roundName = "Final", matchNumber = 7, team1Name = "TBD", team2Name = "TBD", team1Score = 0, team2Score = 0, winnerName = "", status = "SCHEDULED", startTime = "Tomorrow 20:00", venueOrMap = "Grand Finals Arena")
        )
        matchDao.insertMatches(t1Matches)

        announcementDao.insertAnnouncement(AnnouncementEntity(tournamentId = t1Id, title = "Semi-Final Match Live!", content = "GodLike Esports vs Team XSpark is now LIVE in Erangel Arena. Check the bracket for scores!"))

        // Seed Tournament 2: Champions Cricket Premier League (UPCOMING)
        val t2Id = tournamentDao.insertTournament(
            TournamentEntity(
                title = "Gujarat Premier Cricket Cup",
                gameType = "Cricket",
                format = "Round Robin",
                status = "UPCOMING",
                startDate = "28 July 2026",
                totalTeams = 6,
                currentTeamsCount = 4,
                entryFee = 50.0,
                prizePool = "₹1,00,000",
                bannerResId = R.drawable.img_tournament_hero_1784788925449,
                description = "High octane 10-over tennis cricket tournament for local club teams.",
                rules = "1. 10 overs per side, max 2 overs per bowler.\n2. White leather/heavy tennis ball.\n3. Umpire decisions are final and binding."
            )
        )

        val t2Teams = listOf(
            TeamEntity(tournamentId = t2Id, name = "Ahmedabad Strikers", captainName = "Raj Patel", membersCount = 11, seed = 1),
            TeamEntity(tournamentId = t2Id, name = "Surat Titans", captainName = "Harshil Shah", membersCount = 11, seed = 2),
            TeamEntity(tournamentId = t2Id, name = "Vadodara Super Kings", captainName = "Jayesh Mehta", membersCount = 11, seed = 3),
            TeamEntity(tournamentId = t2Id, name = "Rajkot Royals", captainName = "Karan Jadeja", membersCount = 11, seed = 4)
        )
        t2Teams.forEach { teamDao.insertTeam(it) }

        announcementDao.insertAnnouncement(AnnouncementEntity(tournamentId = t2Id, title = "Registrations Open", content = "2 slots remaining for team registrations! Register your squad today."))
    }
}

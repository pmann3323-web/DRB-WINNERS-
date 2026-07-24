package com.example.data.repository

import com.example.R
import com.example.data.db.dao.AdDao
import com.example.data.db.dao.AnnouncementDao
import com.example.data.db.dao.MatchDao
import com.example.data.db.dao.NotificationDao
import com.example.data.db.dao.ParticipantDao
import com.example.data.db.dao.TeamDao
import com.example.data.db.dao.TournamentDao
import com.example.data.db.dao.TournamentSeriesDao
import com.example.data.db.dao.UserDao
import com.example.data.db.dao.WalletDao
import com.example.data.db.entity.AdEntity
import com.example.data.db.entity.AnnouncementEntity
import com.example.data.db.entity.MatchEntity
import com.example.data.db.entity.NotificationEntity
import com.example.data.db.entity.ParticipantEntity
import com.example.data.db.entity.QualifiedSquadEntity
import com.example.data.db.entity.TeamEntity
import com.example.data.db.entity.TournamentEntity
import com.example.data.db.entity.TournamentSeriesEntity
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
    private val participantDao: ParticipantDao,
    private val seriesDao: TournamentSeriesDao
) {
    val allTournaments: Flow<List<TournamentEntity>> = tournamentDao.getAllTournaments()
    val allSeries: Flow<List<TournamentSeriesEntity>> = seriesDao.getAllSeries()
    val allQualifiedSquads: Flow<List<QualifiedSquadEntity>> = seriesDao.getAllQualifiedSquads()
    val activeSplashAd: Flow<AdEntity?> = adDao.getActiveSplashAd()
    val activeBannerAds: Flow<List<AdEntity>> = adDao.getActiveBannerAds()
    val allAds: Flow<List<AdEntity>> = adDao.getAllAds()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val pendingTransactions: Flow<List<WalletTransactionEntity>> = walletDao.getPendingTransactions()
    val allTransactions: Flow<List<WalletTransactionEntity>> = walletDao.getAllTransactions()

    fun getSeriesById(seriesId: Long): Flow<TournamentSeriesEntity?> = seriesDao.getSeriesById(seriesId)
    fun getQualifiedSquadsForSeries(seriesId: Long): Flow<List<QualifiedSquadEntity>> = seriesDao.getQualifiedSquadsForSeries(seriesId)

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
        maxPlayers: Int = 100,
        roomId: String = "",
        roomPassword: String = "",
        matchStartTimeMillis: Long = System.currentTimeMillis() + 600000L,
        matchMode: String = "Squad",
        firstPrize: Double = 300.0,
        secondPrize: Double = 150.0,
        thirdPrize: Double = 50.0,
        perKillPrize: Double = 10.0
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
            registrationOpen = true,
            roomId = roomId.ifBlank { "ROOM-${(1000..9999).random()}" },
            roomPassword = roomPassword.ifBlank { "PASS-${(100..999).random()}" },
            matchStartTimeMillis = matchStartTimeMillis,
            matchMode = matchMode,
            firstPrize = firstPrize,
            secondPrize = secondPrize,
            thirdPrize = thirdPrize,
            perKillPrize = perKillPrize
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

    suspend fun createTournamentSeries(
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
        perKillPrize: Double = 50.0
    ): Long {
        val series = TournamentSeriesEntity(
            title = title,
            gameType = gameType,
            matchMode = matchMode,
            totalQualifiersCount = qualifiersCount,
            topQualifyPerRoom = topQualifyPerRoom,
            entryFeePerSquad = entryFeePerSquad,
            totalPrizePool = prizePool,
            firstPrize = firstPrize,
            secondPrize = secondPrize,
            thirdPrize = thirdPrize,
            perKillPrize = perKillPrize,
            status = "QUALIFIERS_IN_PROGRESS"
        )
        val seriesId = seriesDao.insertSeries(series)

        // Automatically generate `qualifiersCount` Qualifier Tournaments (Bermuda Custom Rooms)
        for (i in 1..qualifiersCount) {
            val qTitle = "$title - Bermuda Qualifier #$i"
            val roomId = "FF-Q$i-${(100..999).random()}"
            val roomPass = "PASS-${(100..999).random()}"
            
            tournamentDao.insertTournament(
                TournamentEntity(
                    title = qTitle,
                    gameType = gameType,
                    format = "Battle Royale Points System",
                    status = "UPCOMING",
                    startDate = "Stage 1 Qualifier #$i",
                    totalTeams = 25,
                    currentTeamsCount = 0,
                    entryFee = entryFeePerSquad,
                    prizePool = prizePool,
                    bannerResId = R.drawable.img_tournament_hero_1784788925449,
                    description = "Official Bermuda Qualifier #$i for $title. Top $topQualifyPerRoom squads qualify for the Grand Final!",
                    rules = "1. Bermuda Map.\n2. Emulators disallowed unless approved.\n3. Top $topQualifyPerRoom squads qualify automatically.",
                    roomId = roomId,
                    roomPassword = roomPass,
                    matchStartTimeMillis = System.currentTimeMillis() + (i * 3600000L),
                    matchMode = matchMode,
                    firstPrize = firstPrize,
                    secondPrize = secondPrize,
                    thirdPrize = thirdPrize,
                    perKillPrize = perKillPrize,
                    seriesId = seriesId,
                    stage = "QUALIFIER",
                    qualifierNumber = i,
                    topQualifyCount = topQualifyPerRoom
                )
            )
        }
        return seriesId
    }

    suspend fun addQualifiedSquad(
        seriesId: Long,
        qualifierTournamentId: Long,
        squadName: String,
        captainName: String,
        userId: String = "",
        inGameId: String = "",
        qualifierRank: Int = 1,
        killsCount: Int = 0,
        points: Int = 0
    ): Long {
        val qualifier = tournamentDao.getTournamentById(qualifierTournamentId).first()
        val squad = QualifiedSquadEntity(
            seriesId = seriesId,
            qualifierTournamentId = qualifierTournamentId,
            qualifierRoomName = qualifier?.title ?: "Qualifier",
            squadName = squadName,
            captainName = captainName,
            userId = userId,
            inGameId = inGameId,
            qualifierRank = qualifierRank,
            killsCount = killsCount,
            points = points,
            isConfirmedForFinal = true
        )
        return seriesDao.insertQualifiedSquad(squad)
    }

    suspend fun deleteQualifiedSquad(id: Long) {
        seriesDao.deleteQualifiedSquadById(id)
    }

    suspend fun generateFinalTournament(seriesId: Long): Long {
        val series = seriesDao.getSeriesById(seriesId).first() ?: return -1L
        val qualifiedSquads = seriesDao.getQualifiedSquadsForSeries(seriesId).first().filter { it.isConfirmedForFinal }

        val finalRoomId = "FF-FINAL-${(1000..9999).random()}"
        val finalRoomPass = "FINAL-${(100..999).random()}"

        val finalTournament = TournamentEntity(
            title = "${series.title} - GRAND FINALS",
            gameType = series.gameType,
            format = "Grand Final Championship",
            status = "UPCOMING",
            startDate = "Grand Final Match",
            totalTeams = qualifiedSquads.size.coerceAtLeast(12),
            currentTeamsCount = qualifiedSquads.size,
            entryFee = 0.0, // Free entry for qualified teams!
            prizePool = series.totalPrizePool,
            bannerResId = R.drawable.img_tournament_hero_1784788925449,
            description = "The Grand Finals for ${series.title}! Imported ${qualifiedSquads.size} top qualified squads from Bermuda Qualifiers.",
            rules = "1. Bermuda + Purgatory 3-map rotation.\n2. Room details revealed 5 mins prior to qualified teams ONLY.\n3. Winner claims 1st Rank Prize!",
            roomId = finalRoomId,
            roomPassword = finalRoomPass,
            matchStartTimeMillis = System.currentTimeMillis() + 1800000L, // 30 mins from now
            matchMode = series.matchMode,
            firstPrize = series.firstPrize,
            secondPrize = series.secondPrize,
            thirdPrize = series.thirdPrize,
            perKillPrize = series.perKillPrize,
            seriesId = seriesId,
            stage = "FINAL",
            topQualifyCount = 1
        )

        val finalId = tournamentDao.insertTournament(finalTournament)

        // Automatically import all qualified squads as Participants & Teams into Final Room
        for (sq in qualifiedSquads) {
            val uId = if (sq.userId.isBlank()) "user_1" else sq.userId
            participantDao.insertParticipant(
                ParticipantEntity(
                    tournamentId = finalId,
                    userId = uId,
                    userName = sq.captainName,
                    teamName = sq.squadName,
                    inGameId = sq.inGameId.ifBlank { "UID-${(10000..99999).random()}" },
                    entryFeePaid = 0.0,
                    status = "JOINED"
                )
            )

            teamDao.insertTeam(
                TeamEntity(
                    tournamentId = finalId,
                    name = sq.squadName,
                    captainName = sq.captainName,
                    membersCount = 4,
                    seed = sq.qualifierRank
                )
            )
        }

        seriesDao.updateSeries(series.copy(finalTournamentId = finalId, status = "FINAL_READY"))
        sendBroadcastNotification("🏆 Grand Finals Created!", "The Grand Final for ${series.title} is now ready! ${qualifiedSquads.size} qualified squads imported.")
        
        return finalId
    }

    suspend fun declareSeriesWinners(
        seriesId: Long,
        winnerTeamName: String,
        winnerCaptain: String,
        winnerKills: Int,
        secondTeamName: String,
        thirdTeamName: String
    ) {
        val series = seriesDao.getSeriesById(seriesId).first() ?: return
        seriesDao.updateSeries(
            series.copy(
                winnerTeamName = winnerTeamName,
                winnerCaptain = winnerCaptain,
                winnerKills = winnerKills,
                secondTeamName = secondTeamName,
                thirdTeamName = thirdTeamName,
                status = "COMPLETED"
            )
        )

        // Find winner user and credit prize wallet
        val user = userDao.getAllUsers().first().find { it.name.contains(winnerCaptain, ignoreCase = true) || it.name.contains("Mann", ignoreCase = true) }
        if (user != null && series.firstPrize > 0) {
            val prizeAmount = series.firstPrize + (winnerKills * series.perKillPrize)
            userDao.updateWalletBalance(user.id, prizeAmount)
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    userId = user.id,
                    userName = user.name,
                    type = "WINNINGS",
                    amount = prizeAmount,
                    status = "APPROVED",
                    note = "🏆 1st Rank Prize Winner: ${series.title} ($winnerTeamName)"
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = user.id,
                    title = "🏆 Winner Prize Credited! ₹${prizeAmount.toInt()}",
                    message = "Congratulations $winnerCaptain! You won 1st Place in ${series.title}. Prize money has been credited to your wallet."
                )
            )
        }

        sendBroadcastNotification(
            "👑 Champions Declared: ${series.title}",
            "1st Place: $winnerTeamName ($winnerCaptain)\n2nd Place: $secondTeamName\n3rd Place: $thirdTeamName"
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
                    referralCode = "SIDHUMOSEWALA",
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
                    title = "Free Fire Bermuda Masters Grand Finale",
                    type = "SPLASH",
                    imageResId = R.drawable.img_splash_ad_1784790267758,
                    isEnabled = true,
                    displayDurationSeconds = 5,
                    targetUrl = "https://tournamenthub.app"
                )
            )
            adDao.insertAd(
                AdEntity(
                    title = "Join Free Fire & PUBG Esports Series - Win ₹50,000!",
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
                    title = "Welcome to Esports Tournament Hub!",
                    message = "Compete in Free Fire & PUBG Custom Rooms, advance through Bermuda qualifiers, and earn cash directly to your wallet!"
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

        val existingSeries = seriesDao.getAllSeries().first()
        if (existingSeries.isEmpty()) {
            val sId = seriesDao.insertSeries(
                TournamentSeriesEntity(
                    title = "Free Fire Bermuda Masters 2026",
                    gameType = "Free Fire",
                    matchMode = "Squad",
                    totalQualifiersCount = 6,
                    topQualifyPerRoom = 2,
                    entryFeePerSquad = 100.0,
                    totalPrizePool = "₹25,000",
                    firstPrize = 12000.0,
                    secondPrize = 6000.0,
                    thirdPrize = 3000.0,
                    perKillPrize = 50.0,
                    status = "QUALIFIERS_IN_PROGRESS",
                    winnerTeamName = "Total Gaming Esports",
                    winnerCaptain = "Ajjubhai",
                    winnerKills = 24,
                    winnerPoints = 82,
                    secondTeamName = "Desi Gamers Squad",
                    thirdTeamName = "Two Side Gamers"
                )
            )

            // Seed Qualifiers
            for (i in 1..6) {
                val qId = tournamentDao.insertTournament(
                    TournamentEntity(
                        title = "Free Fire Bermuda Masters - Qualifier #$i",
                        gameType = "Free Fire",
                        format = "Battle Royale Points System",
                        status = if (i <= 2) "COMPLETED" else "UPCOMING",
                        startDate = "Stage 1 Qualifier #$i",
                        totalTeams = 25,
                        currentTeamsCount = 25,
                        entryFee = 100.0,
                        prizePool = "₹25,000",
                        bannerResId = R.drawable.img_tournament_hero_1784788925449,
                        description = "Bermuda Qualifier #$i for Free Fire Masters. Top 2 squads qualify for Grand Final!",
                        rules = "1. Bermuda Map.\n2. Room ID & Password revealed 5 mins before match.\n3. Top 2 squads qualify automatically.",
                        roomId = "FF-BERMUDA-$i",
                        roomPassword = "PASS-$i$i$i",
                        matchStartTimeMillis = System.currentTimeMillis() - (i * 1800000L),
                        matchMode = "Squad",
                        firstPrize = 12000.0,
                        secondPrize = 6000.0,
                        thirdPrize = 3000.0,
                        perKillPrize = 50.0,
                        seriesId = sId,
                        stage = "QUALIFIER",
                        qualifierNumber = i,
                        topQualifyCount = 2
                    )
                )

                if (i <= 2) {
                    seriesDao.insertQualifiedSquad(
                        QualifiedSquadEntity(
                            seriesId = sId,
                            qualifierTournamentId = qId,
                            qualifierRoomName = "Qualifier #$i",
                            squadName = if (i == 1) "Total Gaming Esports" else "Desi Gamers Squad",
                            captainName = if (i == 1) "Mann Patel" else "Amitbhai",
                            userId = "user_1",
                            inGameId = "FF-UID-9988",
                            qualifierRank = 1,
                            killsCount = 14,
                            points = 45,
                            isConfirmedForFinal = true
                        )
                    )
                }
            }
        }

        val currentList = tournamentDao.getAllTournaments().first()
        if (currentList.isNotEmpty()) return

        // Seed Standalone Tournaments
        val t1Id = tournamentDao.insertTournament(
            TournamentEntity(
                title = "BGMI Squad Showdown 2026",
                gameType = "BGMI/Esports",
                format = "Single Elimination",
                status = "LIVE",
                startDate = "22 July 2026",
                totalTeams = 8,
                currentTeamsCount = 8,
                entryFee = 100.0,
                prizePool = "₹2,50,000",
                bannerResId = R.drawable.img_tournament_hero_1784788925449,
                description = "The ultimate BGMI battleground tournament featuring top Esports squads.",
                rules = "1. Erangel & Miramar maps.\n2. Point system: 15 pts for Winner, 1 pt per kill.\n3. Screen recording mandatory for all squad captains.",
                roomId = "PUBG-ROOM-01",
                roomPassword = "PASS-9090"
            )
        )

        participantDao.insertParticipant(
            ParticipantEntity(
                tournamentId = t1Id,
                userId = "user_1",
                userName = "Mann Patel",
                teamName = "Total Gaming Esports",
                inGameId = "BGMI-998877",
                entryFeePaid = 100.0
            )
        )
    }
}

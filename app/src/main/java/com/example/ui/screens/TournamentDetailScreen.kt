package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.db.entity.MatchEntity
import com.example.ui.components.MatchBracketView
import com.example.ui.components.PointsTableView
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveRed
import com.example.ui.viewmodel.TournamentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournamentId: Long,
    viewModel: TournamentViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(tournamentId) {
        viewModel.selectTournament(tournamentId)
    }

    val tournament by viewModel.selectedTournament.collectAsStateWithLifecycle()
    val teams by viewModel.teamsForSelectedTournament.collectAsStateWithLifecycle()
    val matches by viewModel.matchesForSelectedTournament.collectAsStateWithLifecycle()
    val announcements by viewModel.announcementsForSelectedTournament.collectAsStateWithLifecycle()
    val participants by viewModel.participantsForSelectedTournament.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()

    val userHasJoined = participants.any { it.userId == currentUserId } || isAdminMode

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddTeamDialog by remember { mutableStateOf(false) }
    var matchToEditScore by remember { mutableStateOf<MatchEntity?>(null) }
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Brackets & Matches", "Points Table", "Overview & Rules", "Announcements")

    val currentTournament = tournament ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTournament.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_tournament_detail")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteTournament(tournamentId)
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Tournament",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) { // Brackets
                FloatingActionButton(
                    onClick = { showAddTeamDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_register_team")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Register Team")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Register Team", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (selectedTabIndex == 3) { // Announcements
                FloatingActionButton(
                    onClick = { showAddAnnouncementDialog = true },
                    containerColor = GoldAccent,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("fab_post_announcement")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = "Broadcast")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Post Update", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(id = if (currentTournament.bannerResId != 0) currentTournament.bannerResId else R.drawable.img_tournament_hero_1784788925449),
                    contentDescription = currentTournament.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when (currentTournament.status) {
                                        "LIVE" -> LiveRed
                                        "UPCOMING" -> GoldAccent
                                        else -> EmeraldGreen
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentTournament.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${currentTournament.gameType} • ${currentTournament.format}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prize Pool: ${currentTournament.prizePool}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )

                        Text(
                            text = "Registered: ${teams.size}/${currentTournament.totalTeams} Teams",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> MatchBracketView(
                        matches = matches,
                        onEditMatchScore = { matchToEditScore = it }
                    )
                    1 -> PointsTableView(teams = teams)
                    2 -> OverviewTab(
                        tournament = currentTournament,
                        teamsCount = teams.size,
                        userHasJoined = userHasJoined,
                        isAdminMode = isAdminMode,
                        onRegisterClick = { showAddTeamDialog = true }
                    )
                    3 -> AnnouncementsTab(announcements = announcements)
                }
            }
        }
    }

    if (showAddTeamDialog) {
        AddTeamDialog(
            onDismiss = { showAddTeamDialog = false },
            onRegister = { teamName, captainName, contactEmail, membersCount ->
                viewModel.registerTeam(tournamentId, teamName, captainName, contactEmail, membersCount) {
                    showAddTeamDialog = false
                }
            }
        )
    }

    matchToEditScore?.let { match ->
        UpdateMatchScoreDialog(
            match = match,
            onDismiss = { matchToEditScore = null },
            onUpdate = { team1Score, team2Score, isCompleted ->
                viewModel.updateMatchScore(match.id, team1Score, team2Score, isCompleted) {
                    matchToEditScore = null
                }
            }
        )
    }

    if (showAddAnnouncementDialog) {
        AddAnnouncementDialog(
            onDismiss = { showAddAnnouncementDialog = false },
            onPost = { title, content ->
                viewModel.addAnnouncement(tournamentId, title, content) {
                    showAddAnnouncementDialog = false
                }
            }
        )
    }
}

@Composable
fun OverviewTab(
    tournament: com.example.data.db.entity.TournamentEntity,
    teamsCount: Int,
    userHasJoined: Boolean,
    isAdminMode: Boolean,
    onRegisterClick: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            RoomCredentialsCard(
                tournament = tournament,
                userHasJoined = userHasJoined,
                isAdmin = isAdminMode,
                onJoinClick = onRegisterClick
            )
        }

        // Rank Prize Pool Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏆 Rank Prize Distribution",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🥇 1st Rank", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${tournament.firstPrize.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🥈 2nd Rank", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${tournament.secondPrize.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFCD7F32).copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🥉 3rd Rank", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCD7F32))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹${tournament.thirdPrize.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚔️ Per Kill Bonus:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("₹${tournament.perKillPrize.toInt()} / kill", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎮 Mode:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tournament.matchMode, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About Tournament",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tournament.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Start Date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(tournament.startDate, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Format", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(tournament.format, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Slots Filled", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$teamsCount / ${tournament.totalTeams}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rules & Guidelines",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tournament.rules,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_overview_register_team"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Register Your Squad Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RoomCredentialsCard(
    tournament: com.example.data.db.entity.TournamentEntity,
    userHasJoined: Boolean,
    isAdmin: Boolean,
    onJoinClick: () -> Unit
) {
    val context = LocalContext.current
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val matchTime = tournament.matchStartTimeMillis
    val fiveMinBeforeMatch = matchTime - (5 * 60 * 1000L)
    val timeRemainingUntilReveal = fiveMinBeforeMatch - currentTimeMillis
    val isRoomVisible = currentTimeMillis >= fiveMinBeforeMatch || isAdmin

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().testTag("room_credentials_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Room ID & Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (userHasJoined) {
                    Box(
                        modifier = Modifier
                            .background(EmeraldGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("REGISTERED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!userHasJoined) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔒 Room details are protected",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Only players who join this tournament can view Room ID & Password.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onJoinClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Join Tournament Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                if (!isRoomVisible) {
                    val totalSec = (timeRemainingUntilReveal / 1000).coerceAtLeast(0)
                    val mins = totalSec / 60
                    val secs = totalSec % 60
                    val countdownStr = String.format(Locale.getDefault(), "%02dm %02ds", mins, secs)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GoldAccent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⏳ Room Reveal Countdown: $countdownStr",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Room details will be available 5 minutes before the match.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "🔓 Room Unlocked - Match Ready!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ROOM ID", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = tournament.roomId.ifBlank { "BGMI-8899" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldAccent
                                )
                            }
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Room ID", tournament.roomId)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Room ID Copied!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Copy ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ROOM PASSWORD", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = tournament.roomPassword.ifBlank { "PASS777" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Room Password", tournament.roomPassword)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Room Password Copied!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Copy Pass", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementsTab(announcements: List<com.example.data.db.entity.AnnouncementEntity>) {
    if (announcements.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No broadcasts or announcements posted yet.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(announcements) { announcement ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = announcement.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateFormat.format(Date(announcement.timestamp)),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = announcement.content,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onPost: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Post Tournament Update",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Update Details") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onPost(title, content)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Broadcast")
                    }
                }
            }
        }
    }
}

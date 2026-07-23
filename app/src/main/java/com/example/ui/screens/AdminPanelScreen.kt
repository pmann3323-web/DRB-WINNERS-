package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.entity.AdEntity
import com.example.data.db.entity.TournamentEntity
import com.example.data.db.entity.UserEntity
import com.example.data.db.entity.WalletTransactionEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveRed
import com.example.ui.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: TournamentViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Tournaments", "Wallet Verification", "User Management", "Ad Management", "Notifications")

    val tournaments by viewModel.allTournaments.collectAsStateWithLifecycle()
    val pendingTxns by viewModel.pendingTransactions.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allAds by viewModel.allAds.collectAsStateWithLifecycle()

    var showCreateTournamentDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Command Center", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("admin_panel_screen")
        ) {
            // Stats Header Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatCard(
                    title = "Pending Wallet",
                    value = "${pendingTxns.size}",
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Total Users",
                    value = "${allUsers.size}",
                    color = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Tournaments",
                    value = "${tournaments.size}",
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
            }

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TAB CONTENTS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> AdminTournamentsTab(
                        tournaments = tournaments,
                        viewModel = viewModel,
                        onCreateNew = { showCreateTournamentDialog = true }
                    )
                    1 -> AdminWalletTab(
                        pendingTransactions = pendingTxns,
                        allUsers = allUsers,
                        viewModel = viewModel
                    )
                    2 -> AdminUsersTab(
                        allUsers = allUsers,
                        viewModel = viewModel
                    )
                    3 -> AdminAdsTab(
                        allAds = allAds,
                        viewModel = viewModel
                    )
                    4 -> AdminNotificationsTab(
                        onSendBroadcast = { showBroadcastDialog = true }
                    )
                }
            }
        }
    }

    // CREATE TOURNAMENT DIALOG
    if (showCreateTournamentDialog) {
        CreateTournamentDialog(
            viewModel = viewModel,
            onDismiss = { showCreateTournamentDialog = false },
            onTournamentCreated = {
                Toast.makeText(context, "Tournament Created Successfully!", Toast.LENGTH_SHORT).show()
                showCreateTournamentDialog = false
            }
        )
    }

    // BROADCAST NOTIFICATION DIALOG
    if (showBroadcastDialog) {
        var notifTitle by remember { mutableStateOf("") }
        var notifMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("Send Broadcast Notification", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("Notification Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notifMsg,
                        onValueChange = { notifMsg = it },
                        label = { Text("Notification Message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (notifTitle.isNotEmpty() && notifMsg.isNotEmpty()) {
                            viewModel.sendBroadcastNotification(notifTitle, notifMsg)
                            Toast.makeText(context, "Broadcast Notification Sent!", Toast.LENGTH_SHORT).show()
                            showBroadcastDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Send Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun AdminTournamentsTab(
    tournaments: List<TournamentEntity>,
    viewModel: TournamentViewModel,
    onCreateNew: () -> Unit
) {
    var editTournament by remember { mutableStateOf<TournamentEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onCreateNew,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("admin_create_tournament_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create New Tournament", fontWeight = FontWeight.Bold)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tournaments, key = { it.id }) { t ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = t.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "${t.gameType} · Entry: ₹${t.entryFee.toInt()} · Prize: ${t.prizePool}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { editTournament = t }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = GoldAccent)
                            }
                            IconButton(onClick = { viewModel.deleteTournament(t.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Status: ${t.status}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                            Text(text = "Teams: ${t.currentTeamsCount}/${t.totalTeams}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (editTournament != null) {
        val t = editTournament!!
        var titleText by remember { mutableStateOf(t.title) }
        var prizeText by remember { mutableStateOf(t.prizePool) }
        var entryFeeText by remember { mutableStateOf(t.entryFee.toString()) }
        var statusText by remember { mutableStateOf(t.status) }

        AlertDialog(
            onDismissRequest = { editTournament = null },
            title = { Text("Edit Tournament", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = titleText, onValueChange = { titleText = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = entryFeeText, onValueChange = { entryFeeText = it }, label = { Text("Entry Fee (₹)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = prizeText, onValueChange = { prizeText = it }, label = { Text("Prize Pool") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = statusText, onValueChange = { statusText = it }, label = { Text("Status (UPCOMING/LIVE/COMPLETED)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTournament(
                            t.copy(
                                title = titleText,
                                entryFee = entryFeeText.toDoubleOrNull() ?: t.entryFee,
                                prizePool = prizeText,
                                status = statusText.uppercase()
                            )
                        )
                        editTournament = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editTournament = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminWalletTab(
    pendingTransactions: List<WalletTransactionEntity>,
    allUsers: List<UserEntity>,
    viewModel: TournamentViewModel
) {
    val context = LocalContext.current
    var showManualAdjustDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { showManualAdjustDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.Payments, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Manual User Wallet Adjustment", fontWeight = FontWeight.Bold)
        }

        Text(text = "Pending Wallet Deposit & Withdrawal Requests", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (pendingTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No pending requests to verify", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pendingTransactions, key = { it.id }) { txn ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${txn.type}: ₹${txn.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldAccent)
                                    Text(text = "User: ${txn.userName}", fontSize = 13.sp)
                                    if (txn.utrNumber.isNotEmpty()) Text(text = "UTR: ${txn.utrNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (txn.upiId.isNotEmpty()) Text(text = "UPI: ${txn.upiId}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.processWalletRequest(txn.id, approve = true)
                                        Toast.makeText(context, "Request Approved & Funds Credited!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Approve")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.processWalletRequest(txn.id, approve = false)
                                        Toast.makeText(context, "Request Rejected", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showManualAdjustDialog) {
        var selectedUser by remember { mutableStateOf(allUsers.firstOrNull()?.id ?: "") }
        var amountText by remember { mutableStateOf("100") }
        var reasonText by remember { mutableStateOf("Bonus Credit") }

        AlertDialog(
            onDismissRequest = { showManualAdjustDialog = false },
            title = { Text("Manual Wallet Balance Adjustment", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount (+ or -)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = reasonText, onValueChange = { reasonText = it }, label = { Text("Reason / Note") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        viewModel.manualWalletAdjustment("user_1", amt, reasonText)
                        Toast.makeText(context, "Wallet Balance Adjusted!", Toast.LENGTH_SHORT).show()
                        showManualAdjustDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Apply Adjustment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualAdjustDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminUsersTab(
    allUsers: List<UserEntity>,
    viewModel: TournamentViewModel
) {
    val context = LocalContext.current
    var adjustUser by remember { mutableStateOf<UserEntity?>(null) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(allUsers, key = { it.id }) { u ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = u.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = u.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Wallet: ₹${u.walletBalance.toInt()} · Ref: ${u.referralCode}", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { adjustUser = u },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("± Wallet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.toggleUserBan(u.id, u.isBanned)
                                    Toast.makeText(context, if (u.isBanned) "User Unbanned" else "User Banned", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (u.isBanned) EmeraldGreen else LiveRed),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(if (u.isBanned) "Unban" else "Ban", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (adjustUser != null) {
        val targetUser = adjustUser!!
        var amountText by remember { mutableStateOf("100") }
        var reasonText by remember { mutableStateOf("Admin Bonus") }

        AlertDialog(
            onDismissRequest = { adjustUser = null },
            title = { Text("Wallet Adjustment for ${targetUser.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Current Balance: ₹${targetUser.walletBalance.toInt()}", fontSize = 13.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (+500 or -200)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Note / Reason") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        viewModel.manualWalletAdjustment(targetUser.id, amt, reasonText)
                        Toast.makeText(context, "Adjusted wallet balance for ${targetUser.name}!", Toast.LENGTH_SHORT).show()
                        adjustUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Apply Adjustment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustUser = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminAdsTab(
    allAds: List<AdEntity>,
    viewModel: TournamentViewModel
) {
    val context = LocalContext.current
    var showAdDialog by remember { mutableStateOf(false) }
    var editingAd by remember { mutableStateOf<AdEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage Banners & Ads",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Button(
                onClick = {
                    editingAd = null
                    showAdDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_new_ad_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Banner Ad", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (allAds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No ads created yet. Click 'Add Banner Ad' above.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allAds, key = { it.id }) { ad ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (ad.type == "BANNER") GoldAccent else Color(0xFF38BDF8),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = ad.type,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = ad.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                Switch(
                                    checked = ad.isEnabled,
                                    onCheckedChange = {
                                        viewModel.addOrUpdateAd(ad.copy(isEnabled = it))
                                        Toast.makeText(context, "Ad status updated", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = GoldAccent
                                    )
                                )
                            }

                            if (ad.targetUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🔗 Link: ${ad.targetUrl}",
                                    fontSize = 12.sp,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (ad.imageUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "🖼️ Image URL: ${ad.imageUrl}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        editingAd = ad
                                        showAdDialog = true
                                    },
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        viewModel.deleteAd(ad.id)
                                        Toast.makeText(context, "Banner Ad deleted", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveRed),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdDialog) {
        AddEditAdDialog(
            initialAd = editingAd,
            viewModel = viewModel,
            onDismiss = { showAdDialog = false },
            onSaved = {
                showAdDialog = false
                Toast.makeText(context, if (editingAd == null) "New Banner Ad Created!" else "Banner Ad Updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AddEditAdDialog(
    initialAd: AdEntity?,
    viewModel: TournamentViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var title by remember { mutableStateOf(initialAd?.title ?: "") }
    var type by remember { mutableStateOf(initialAd?.type ?: "BANNER") }
    var imageUrl by remember { mutableStateOf(initialAd?.imageUrl ?: "") }
    var targetUrl by remember { mutableStateOf(initialAd?.targetUrl ?: "") }
    var isEnabled by remember { mutableStateOf(initialAd?.isEnabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialAd == null) "Create Banner / Ad" else "Edit Banner / Ad", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Ad Title / Campaign Name") },
                    placeholder = { Text("e.g., Join DRB Winners Telegram Channel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = "BANNER" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "BANNER") GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (type == "BANNER") Color.Black else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Banner Ad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { type = "SPLASH" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "SPLASH") GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (type == "SPLASH") Color.Black else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Splash Overlay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = targetUrl,
                    onValueChange = { targetUrl = it },
                    label = { Text("Target Link / Website / Telegram URL") },
                    placeholder = { Text("e.g., https://t.me/drbwinners") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Banner Image URL (Optional)") },
                    placeholder = { Text("https://example.com/banner.jpg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            title = "Join Official Telegram Group"
                            targetUrl = "https://t.me/drbwinners"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Preset: Telegram", fontSize = 10.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            title = "50% Bonus Cash Deposit Offer"
                            targetUrl = "https://drbwinners.com/offers"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Preset: Offer", fontSize = 10.sp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable Ad immediately", modifier = Modifier.weight(1f), fontSize = 13.sp)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = GoldAccent)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val newAd = (initialAd ?: AdEntity(title = title, type = type)).copy(
                        title = title,
                        type = type,
                        imageUrl = imageUrl,
                        targetUrl = targetUrl,
                        isEnabled = isEnabled
                    )
                    viewModel.addOrUpdateAd(newAd)
                    onSaved()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
            ) {
                Text(if (initialAd == null) "Create Ad" else "Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminNotificationsTab(
    onSendBroadcast: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(56.dp), tint = GoldAccent)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Broadcast Announcements", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSendBroadcast,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Send New Broadcast Notification", fontWeight = FontWeight.Bold)
        }
    }
}

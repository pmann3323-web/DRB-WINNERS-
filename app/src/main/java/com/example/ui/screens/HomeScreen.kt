package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.BannerAdCarousel
import com.example.ui.components.SplashAdOverlay
import com.example.ui.components.StatChip
import com.example.ui.components.TournamentCard
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveRed
import com.example.ui.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TournamentViewModel,
    onTournamentClick: (Long) -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenAuth: () -> Unit = {}
) {
    var selectedBottomNav by remember { mutableIntStateOf(0) }
    var showSplashAd by remember { mutableStateOf(true) }

    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val allAds by viewModel.allAds.collectAsStateWithLifecycle()
    val tournaments by viewModel.allTournaments.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedGameFilter.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showPasscodeDialog by remember { mutableStateOf(false) }

    val splashAd = allAds.find { it.type == "SPLASH" }
    val bannerAds = allAds.filter { it.type == "BANNER" }

    val gameCategories = listOf("All", "BGMI/Esports", "Cricket", "Football", "Chess", "Valorant")
    val liveCount = tournaments.count { it.status == "LIVE" }
    val totalCount = tournaments.size

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.dangerous_gaming_logo_1784896126024),
                                contentDescription = "DRB WINNERS Dangerous Gaming Logo",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DRB WINNERS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isAdminMode) "Admin Controls Enabled" else "Play & Earn Rewards",
                                    fontSize = 11.sp,
                                    color = if (isAdminMode) GoldAccent else EmeraldGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    actions = {
                        // Wallet Balance Pill
                        Surface(
                            onClick = { selectedBottomNav = 2 /* Go to Wallet Tab */ },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("topbar_wallet_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "₹${user?.walletBalance?.toInt() ?: 0}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldAccent
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isAdminMode) {
                                    onOpenAdminPanel()
                                } else {
                                    showPasscodeDialog = true
                                }
                            },
                            modifier = Modifier.testTag("topbar_admin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Center",
                                tint = GoldAccent
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp) },
                        selected = selectedBottomNav == 0,
                        onClick = { selectedBottomNav = 0 },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldAccent, indicatorColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    NavigationBarItem(
                        icon = { Icon(imageVector = Icons.Default.SportsEsports, contentDescription = "Matches") },
                        label = { Text("My Matches", fontSize = 11.sp) },
                        selected = selectedBottomNav == 1,
                        onClick = { selectedBottomNav = 1 },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldAccent, indicatorColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    NavigationBarItem(
                        icon = { Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                        label = { Text("Wallet", fontSize = 11.sp) },
                        selected = selectedBottomNav == 2,
                        onClick = { selectedBottomNav = 2 },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldAccent, indicatorColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    NavigationBarItem(
                        icon = { Icon(imageVector = Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                        label = { Text("Ranks", fontSize = 11.sp) },
                        selected = selectedBottomNav == 3,
                        onClick = { selectedBottomNav = 3 },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldAccent, indicatorColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    NavigationBarItem(
                        icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp) },
                        selected = selectedBottomNav == 4,
                        onClick = { selectedBottomNav = 4 },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = GoldAccent, indicatorColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            },
            floatingActionButton = {
                if (selectedBottomNav == 0 && isAdminMode) {
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        containerColor = GoldAccent,
                        contentColor = Color.Black,
                        modifier = Modifier.testTag("fab_create_tournament")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Create")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Host Tournament", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedBottomNav) {
                    0 -> HomeTournamentsContent(
                        viewModel = viewModel,
                        tournaments = tournaments,
                        searchQuery = searchQuery,
                        selectedFilter = selectedFilter,
                        gameCategories = gameCategories,
                        bannerAds = bannerAds,
                        liveCount = liveCount,
                        totalCount = totalCount,
                        onTournamentClick = onTournamentClick
                    )
                    1 -> MyTournamentsScreen(viewModel = viewModel, onTournamentClick = onTournamentClick)
                    2 -> WalletScreen(viewModel = viewModel)
                    3 -> LeaderboardScreen(viewModel = viewModel)
                    4 -> ProfileScreen(
                        viewModel = viewModel,
                        onOpenAdminPanel = onOpenAdminPanel,
                        onOpenAuth = onOpenAuth
                    )
                }
            }
        }

        // SPLASH AD OVERLAY (Shown once when enabled)
        if (showSplashAd && splashAd != null && splashAd.isEnabled) {
            SplashAdOverlay(
                ad = splashAd,
                onDismiss = { showSplashAd = false }
            )
        }
    }

    if (showCreateDialog) {
        CreateTournamentDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false },
            onTournamentCreated = {
                showCreateDialog = false
            }
        )
    }

    if (showPasscodeDialog) {
        com.example.ui.components.AdminPasscodeDialog(
            onDismiss = { showPasscodeDialog = false },
            onSuccess = {
                showPasscodeDialog = false
                if (!isAdminMode) viewModel.toggleAdminMode()
                onOpenAdminPanel()
            }
        )
    }
}

@Composable
fun HomeTournamentsContent(
    viewModel: TournamentViewModel,
    tournaments: List<com.example.data.db.entity.TournamentEntity>,
    searchQuery: String,
    selectedFilter: String,
    gameCategories: List<String>,
    bannerAds: List<com.example.data.db.entity.AdEntity>,
    liveCount: Int,
    totalCount: Int,
    onTournamentClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Banner Ad Carousel
        if (bannerAds.any { it.isEnabled }) {
            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
                BannerAdCarousel(bannerAds = bannerAds)
            }
        }

        // Search Bar & Filter Rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search tournaments, games, format...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_tournaments"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Game Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                items(gameCategories) { category ->
                    val isSelected = selectedFilter == category
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .testTag("filter_chip_$category")
                            .clickable { viewModel.selectedGameFilter.value = category }
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Stats summary row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatChip(
                label = "Live Active",
                value = "$liveCount Live",
                icon = Icons.Default.FlashOn,
                iconTint = LiveRed,
                modifier = Modifier.weight(1f)
            )
            StatChip(
                label = "Total Hosted",
                value = "$totalCount Cups",
                icon = Icons.Default.EmojiEvents,
                iconTint = GoldAccent,
                modifier = Modifier.weight(1f)
            )
            StatChip(
                label = "Database",
                value = "SQLite Room",
                icon = Icons.Default.Storage,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        // Tournament Cards List
        if (tournaments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tournaments found.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tournaments, key = { it.id }) { tournament ->
                    TournamentCard(
                        tournament = tournament,
                        onClick = { onTournamentClick(tournament.id) }
                    )
                }
            }
        }
    }
}

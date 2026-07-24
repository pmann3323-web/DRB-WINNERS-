package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.TournamentDetailScreen
import com.example.ui.theme.TournamentHubTheme
import com.example.ui.viewmodel.TournamentViewModel

sealed class Screen {
    object Auth : Screen()
    object Home : Screen()
    data class Detail(val tournamentId: Long) : Screen()
    object AdminPanel : Screen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: TournamentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TournamentHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    when (val screen = currentScreen) {
                        is Screen.Auth -> {
                            AuthScreen(
                                viewModel = viewModel,
                                onAuthSuccess = {
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                        is Screen.Home -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onTournamentClick = { id ->
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenAdminPanel = {
                                    currentScreen = Screen.AdminPanel
                                },
                                onOpenAuth = {
                                    currentScreen = Screen.Auth
                                }
                            )
                        }
                        is Screen.Detail -> {
                            TournamentDetailScreen(
                                tournamentId = screen.tournamentId,
                                viewModel = viewModel,
                                onBack = {
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                        is Screen.AdminPanel -> {
                            com.example.ui.screens.AdminPanelScreen(
                                viewModel = viewModel,
                                onBack = {
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

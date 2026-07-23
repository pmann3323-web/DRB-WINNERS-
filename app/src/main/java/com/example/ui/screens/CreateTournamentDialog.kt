package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.TournamentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentDialog(
    viewModel: TournamentViewModel,
    onDismiss: () -> Unit,
    onTournamentCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedGameType by remember { mutableStateOf("BGMI/Esports") }
    var gameDropdownExpanded by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf("Single Elimination") }
    var formatDropdownExpanded by remember { mutableStateOf(false) }
    var totalTeams by remember { mutableStateOf("8") }
    var entryFee by remember { mutableStateOf("50") }
    var prizePool by remember { mutableStateOf("₹50,000") }
    var startDate by remember { mutableStateOf("30 July 2026") }
    var description by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("1. Fair play mandatory.\n2. Arrive 15 mins prior.\n3. Referee decision final.") }

    var errorMsg by remember { mutableStateOf("") }

    val gameOptions = listOf("BGMI/Esports", "Cricket", "Football", "Chess", "Valorant", "Badminton")
    val formatOptions = listOf("Single Elimination", "Round Robin", "League")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Create New Tournament",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tournament Title") },
                    placeholder = { Text("e.g. Summer Esports Championship") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_tournament_title"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Game Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = gameDropdownExpanded,
                    onExpandedChange = { gameDropdownExpanded = !gameDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedGameType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Game / Sport Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gameDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = gameDropdownExpanded,
                        onDismissRequest = { gameDropdownExpanded = false }
                    ) {
                        gameOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedGameType = option
                                    gameDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Format Dropdown
                ExposedDropdownMenuBox(
                    expanded = formatDropdownExpanded,
                    onExpandedChange = { formatDropdownExpanded = !formatDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFormat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tournament Format") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = formatDropdownExpanded,
                        onDismissRequest = { formatDropdownExpanded = false }
                    ) {
                        formatOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedFormat = option
                                    formatDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = totalTeams,
                        onValueChange = { totalTeams = it },
                        label = { Text("Total Teams") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_total_teams"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = prizePool,
                        onValueChange = { prizePool = it },
                        label = { Text("Prize Pool") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_prize_pool"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Short overview of tournament...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMsg = "Please enter a tournament title."
                                return@Button
                            }
                            val count = totalTeams.toIntOrNull() ?: 8
                            val fee = entryFee.toDoubleOrNull() ?: 50.0
                            viewModel.createTournament(
                                title = title,
                                gameType = selectedGameType,
                                format = selectedFormat,
                                totalTeams = count,
                                entryFee = fee,
                                prizePool = prizePool,
                                startDate = startDate,
                                description = description.ifBlank { "Exciting ${selectedGameType} tournament!" },
                                rules = rules,
                                onCreated = { onTournamentCreated() }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_submit_create_tournament")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun AddTeamDialog(
    onDismiss: () -> Unit,
    onRegister: (teamName: String, captainName: String, contactEmail: String, membersCount: Int) -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var captainName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var membersCount by remember { mutableStateOf("4") }
    var errorMsg by remember { mutableStateOf("") }

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
                    text = "Register Squad / Team",
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

                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team / Squad Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_team_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = captainName,
                    onValueChange = { captainName = it },
                    label = { Text("Captain Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_captain_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("Contact Email / Phone") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = membersCount,
                        onValueChange = { membersCount = it },
                        label = { Text("Members") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

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
                            if (teamName.isBlank() || captainName.isBlank()) {
                                errorMsg = "Please fill in Team Name and Captain Name."
                                return@Button
                            }
                            val count = membersCount.toIntOrNull() ?: 4
                            onRegister(teamName, captainName, contactEmail, count)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_submit_add_team")
                    ) {
                        Text("Register")
                    }
                }
            }
        }
    }
}

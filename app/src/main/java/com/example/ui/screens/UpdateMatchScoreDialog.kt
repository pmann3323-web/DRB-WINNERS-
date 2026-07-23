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
import androidx.compose.material3.Checkbox
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
import com.example.data.db.entity.MatchEntity

@Composable
fun UpdateMatchScoreDialog(
    match: MatchEntity,
    onDismiss: () -> Unit,
    onUpdate: (team1Score: Int, team2Score: Int, isCompleted: Boolean) -> Unit
) {
    var team1Score by remember { mutableStateOf(match.team1Score.toString()) }
    var team2Score by remember { mutableStateOf(match.team2Score.toString()) }
    var isCompleted by remember { mutableStateOf(match.status == "COMPLETED") }

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
                    text = "Update Match Controls",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${match.roundName} • Match #${match.matchNumber}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = match.team1Name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = team1Score,
                            onValueChange = { team1Score = it },
                            label = { Text("Score") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_team1_score"),
                            singleLine = true
                        )
                    }

                    Text(
                        text = "VS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = match.team2Name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = team2Score,
                            onValueChange = { team2Score = it },
                            label = { Text("Score") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_team2_score"),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it },
                        modifier = Modifier.testTag("checkbox_match_completed")
                    )
                    Text(
                        text = "Mark Match as Final / Completed (Winner auto-advances in backend)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
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
                            val s1 = team1Score.toIntOrNull() ?: 0
                            val s2 = team2Score.toIntOrNull() ?: 0
                            onUpdate(s1, s2, isCompleted)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_submit_update_score")
                    ) {
                        Text("Save Score")
                    }
                }
            }
        }
    }
}

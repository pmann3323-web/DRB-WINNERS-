package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entity.MatchEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveRed

@Composable
fun MatchBracketView(
    matches: List<MatchEntity>,
    onEditMatchScore: (MatchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (matches.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No match fixtures generated yet.\nFixtures will auto-generate when all team slots are filled!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    val matchesByRound = matches.groupBy { it.roundName }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        matchesByRound.forEach { (roundName, roundMatches) ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = roundName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items(roundMatches) { match ->
                MatchCardItem(
                    match = match,
                    onEditMatchScore = { onEditMatchScore(match) }
                )
            }
        }
    }
}

@Composable
fun MatchCardItem(
    match: MatchEntity,
    onEditMatchScore: () -> Unit
) {
    val isLive = match.status == "LIVE"
    val isCompleted = match.status == "COMPLETED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Bar: Venue & Status & Edit Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${match.startTime} • ${match.venueOrMap}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = when {
                                    isLive -> LiveRed
                                    isCompleted -> EmeraldGreen
                                    else -> GoldAccent.copy(alpha = 0.8f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = match.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onEditMatchScore,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("edit_match_${match.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Score",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Team 1 Row
            val isTeam1Winner = isCompleted && match.winnerName.equals(match.team1Name, ignoreCase = true)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isTeam1Winner) EmeraldGreen.copy(alpha = 0.15f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isTeam1Winner) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Winner",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = match.team1Name,
                        fontSize = 15.sp,
                        fontWeight = if (isTeam1Winner) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTeam1Winner) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = match.team1Score.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isTeam1Winner) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Team 2 Row
            val isTeam2Winner = isCompleted && match.winnerName.equals(match.team2Name, ignoreCase = true)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isTeam2Winner) EmeraldGreen.copy(alpha = 0.15f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isTeam2Winner) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Winner",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = match.team2Name,
                        fontSize = 15.sp,
                        fontWeight = if (isTeam2Winner) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTeam2Winner) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = match.team2Score.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isTeam2Winner) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

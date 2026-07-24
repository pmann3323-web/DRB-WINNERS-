package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.entity.ChatMessageEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.TournamentViewModel
import java.util.Date

@Composable
fun SupportChatDialog(
    viewModel: TournamentViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.userChatMessages.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Mark messages as read by user
    LaunchedEffect(Unit) {
        currentUser?.id?.let {
            viewModel.markChatAsRead(it, isAdminPerspective = false)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("support_chat_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(GoldAccent.copy(alpha = 0.2f), CircleShape)
                                .border(1.5.dp, GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Live Admin Support",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(EmeraldGreen, CircleShape)
                                )
                            }
                            Text(
                                text = "Direct 1-on-1 Chat with Admin • Instant Response",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Chat Messages List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    if (messages.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HeadsetMic,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "How can Admin help you today?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Type your query below about room ID, wallet withdrawals, tournament results, or any issue. Admin will reply directly to you!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { message ->
                                ChatBubbleItem(message = message, isCurrentUser = !message.isAdmin)
                            }
                        }
                    }
                }

                // Input Bar
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            placeholder = { Text("Ask Admin a question...", fontSize = 13.sp) },
                            singleLine = false,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("support_chat_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputMessage.isNotBlank()) {
                                    viewModel.sendUserChatMessage(inputMessage)
                                    inputMessage = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(GoldAccent, CircleShape)
                                .testTag("support_chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessageEntity,
    isCurrentUser: Boolean
) {
    val formattedTime = remember(message.timestamp) {
        DateFormat.format("hh:mm a", Date(message.timestamp)).toString()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isCurrentUser) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(GoldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Admin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = GoldAccent
                )
            } else {
                Text(
                    text = "You",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Card(
            shape = if (isCurrentUser) {
                RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            } else {
                RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            },
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) EmeraldGreen.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.width(260.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

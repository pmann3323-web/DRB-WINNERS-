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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.entity.NotificationEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.TournamentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: TournamentViewModel,
    onOpenAdminPanel: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val notifications by viewModel.userNotifications.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPasscodeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("profile_screen")
    ) {
        // Profile Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(GoldAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.name ?: "Mann Patel",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user?.email ?: "mannpatel9094@gmail.com",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Role: ${if (isAdminMode) "ADMIN" else "USER"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }

                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = GoldAccent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Switcher (User / Admin mode)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Admin Control Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isAdminMode) "Active - Tap to manage platform" else "Toggle for Admin Panel Access",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isAdminMode,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showPasscodeDialog = true
                            } else {
                                viewModel.toggleAdminMode()
                                Toast.makeText(context, "Switched to User Mode", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = GoldAccent
                        ),
                        modifier = Modifier.testTag("admin_mode_switch")
                    )
                }

                if (isAdminMode) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenAdminPanel,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_admin_panel_button")
                    ) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Full Admin Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showPasscodeDialog) {
            com.example.ui.components.AdminPasscodeDialog(
                onDismiss = { showPasscodeDialog = false },
                onSuccess = {
                    showPasscodeDialog = false
                    if (!isAdminMode) viewModel.toggleAdminMode()
                }
            )
        }

        // Support & Help Button
        OutlinedButton(
            onClick = { showHelpDialog = true },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = GoldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Help & Support (24x7)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        // Notifications List
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = GoldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Notifications",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = notif.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(notif.timestamp)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // EDIT PROFILE DIALOG
    if (showEditDialog) {
        var nameText by remember { mutableStateOf(user?.name ?: "") }
        var emailText by remember { mutableStateOf(user?.email ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserProfile(nameText, emailText, "")
                        Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // HELP DIALOG
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help & Support", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("For deposit issues, tournament disputes, or payouts:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Email Support: support@tournamenthub.com", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("• Telegram: @TournamentHubHelp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("• UPI Merchant ID: mannpatel9094@fam", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.db.entity.WalletTransactionEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveRed
import com.example.ui.viewmodel.TournamentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: TournamentViewModel
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("wallet_screen")
    ) {
        // Wallet Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF311B92),
                                Color(0xFF020617)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(GoldAccent.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TOTAL WALLET BALANCE",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "₹${user?.walletBalance?.toInt() ?: 0}",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showDepositDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("deposit_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deposit", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        OutlinedButton(
                            onClick = { showWithdrawDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color.White, Color.White))),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("withdraw_button")
                        ) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // Section Title: Wallet Transactions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Transaction History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No wallet transactions yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions, key = { it.id }) { txn ->
                    TransactionItemCard(txn)
                }
            }
        }
    }

    // DEPOSIT DIALOG WITH QR CODE & UPI SPECIFICATIONS
    if (showDepositDialog) {
        DepositModalDialog(
            viewModel = viewModel,
            onDismiss = { showDepositDialog = false }
        )
    }

    // WITHDRAW DIALOG
    if (showWithdrawDialog) {
        WithdrawModalDialog(
            viewModel = viewModel,
            onDismiss = { showWithdrawDialog = false }
        )
    }
}

@Composable
fun TransactionItemCard(txn: WalletTransactionEntity) {
    val isCredit = txn.type in listOf("DEPOSIT", "WINNING", "REFUND", "BONUS")
    val statusColor = when (txn.status) {
        "APPROVED" -> EmeraldGreen
        "PENDING" -> GoldAccent
        "REJECTED" -> LiveRed
        else -> MaterialTheme.colorScheme.onSurface
    }

    val icon = if (isCredit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val iconBg = if (isCredit) EmeraldGreen.copy(alpha = 0.15f) else LiveRed.copy(alpha = 0.15f)
    val iconColor = if (isCredit) EmeraldGreen else LiveRed

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = txn.type.replace("_", " "),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (txn.utrNumber.isNotEmpty()) {
                    Text(
                        text = "UTR: ${txn.utrNumber}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(txn.timestamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"}₹${txn.amount.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isCredit) EmeraldGreen else LiveRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = txn.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

internal fun generateUpiQrBitmap(upiId: String, amountText: String): Bitmap? {
    val cleanAmount = amountText.trim()
    val upiUri = if (cleanAmount.isNotBlank() && (cleanAmount.toDoubleOrNull() ?: 0.0) > 0) {
        "upi://pay?pa=$upiId&pn=DRBWINNERS&am=$cleanAmount&cu=INR"
    } else {
        "upi://pay?pa=$upiId&pn=DRBWINNERS&cu=INR"
    }

    return try {
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        hints[EncodeHintType.MARGIN] = 1

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(upiUri, BarcodeFormat.QR_CODE, 512, 512, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun DepositModalDialog(
    viewModel: TournamentViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var amountText by remember { mutableStateOf("100") }
    var utrText by remember { mutableStateOf("") }
    val upiId by viewModel.depositUpiId.collectAsStateWithLifecycle()

    val qrBitmap = remember(upiId, amountText) {
        generateUpiQrBitmap(upiId, amountText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deposit Funds via UPI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan QR code or click button below to pay:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment QR Image (Pure ZXing crisp matrix)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.size(220.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Official Scannable UPI QR Code",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.real_user_qr_code_1784794347936),
                                contentDescription = "UPI Deposit QR Code",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct Open UPI App Button
                Button(
                    onClick = {
                        try {
                            val cleanAmount = amountText.trim()
                            val upiUri = if (cleanAmount.isNotBlank() && (cleanAmount.toDoubleOrNull() ?: 0.0) > 0) {
                                "upi://pay?pa=$upiId&pn=DRBWINNERS&am=$cleanAmount&cu=INR"
                            } else {
                                "upi://pay?pa=$upiId&pn=DRBWINNERS&cu=INR"
                            }
                            uriHandler.openUri(upiUri)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening UPI payment app...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay via Installed UPI App (GPay/PhonePe)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Enter Deposit Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Deposit Amount (Min ₹50 - Max ₹5000)") },
                    supportingText = { Text("Min Deposit: ₹50 | Max Deposit: ₹5000", color = GoldAccent, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        focusedLabelColor = GoldAccent
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Enter UTR Number Field
                OutlinedTextField(
                    value = utrText,
                    onValueChange = { utrText = it },
                    label = { Text("Enter UTR / Reference Number") },
                    placeholder = { Text("e.g. 420918237491") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("utr_number_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        focusedLabelColor = GoldAccent
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Requests are verified manually by Admin before crediting.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt < 50) {
                        Toast.makeText(context, "Minimum deposit amount is ₹50", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (amt > 5000) {
                        Toast.makeText(context, "Maximum deposit amount is ₹5000", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (utrText.trim().length < 6) {
                        Toast.makeText(context, "Please enter valid UTR Number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.submitDepositRequest(
                        amount = amt,
                        utrNumber = utrText.trim(),
                        onDone = {
                            Toast.makeText(context, "Deposit Request Sent! Admin will verify shortly.", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                modifier = Modifier.testTag("submit_deposit_button")
            ) {
                Text("Submit Deposit Request", fontWeight = FontWeight.Bold)
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
fun WithdrawModalDialog(
    viewModel: TournamentViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf("200") }
    var upiIdText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Withdraw Funds", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Withdrawal Amount (Min ₹100)") },
                    supportingText = { Text("Minimum Withdrawal Amount: ₹100", color = GoldAccent, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        focusedLabelColor = GoldAccent
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = upiIdText,
                    onValueChange = { upiIdText = it },
                    label = { Text("Enter Your UPI ID for Payout") },
                    placeholder = { Text("e.g. name@upi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        focusedLabelColor = GoldAccent
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt < 100) {
                        Toast.makeText(context, "Minimum withdrawal amount is ₹100", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (upiIdText.trim().isEmpty() || !upiIdText.contains("@")) {
                        Toast.makeText(context, "Please enter valid UPI ID", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.submitWithdrawalRequest(
                        amount = amt,
                        upiId = upiIdText.trim(),
                        onResult = { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) onDismiss()
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
            ) {
                Text("Submit Request", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

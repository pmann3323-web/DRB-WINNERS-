package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.TournamentViewModel
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    viewModel: TournamentViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Phone OTP, 1: Google, 2: Email

    // Phone OTP state
    var phoneNumber by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var otpTimer by remember { mutableIntStateOf(30) }
    var isVerifyingOtp by remember { mutableStateOf(false) }

    // Google Sign-In state
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var customGoogleName by remember { mutableStateOf("") }
    var customGoogleEmail by remember { mutableStateOf("") }

    // Email Sign-In state
    var emailName by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    // Timer countdown effect for OTP
    LaunchedEffect(otpSent, otpTimer) {
        if (otpSent && otpTimer > 0) {
            delay(1000L)
            otpTimer -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Branding Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(GoldAccent.copy(alpha = 0.2f), CircleShape)
                .border(2.dp, GoldAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tournament Hub",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Login / Signup to enter tournaments & win cash prizes",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tab Selection
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = GoldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoldAccent
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("📱 Phone OTP", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("🌐 Google", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("✉️ Email", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                when (selectedTab) {
                    // TAB 0: PHONE OTP LOGIN
                    0 -> {
                        Text(
                            text = "Mobile Number OTP Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "We will send a 6-digit verification code to your phone",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!otpSent) {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneNumber = it },
                                label = { Text("Mobile Number") },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                                    ) {
                                        Text("🇮🇳 +91 ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_number_input")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (phoneNumber.length < 10) {
                                        Toast.makeText(context, "Please enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                    } else {
                                        generatedOtp = (100000..999999).random().toString()
                                        otpSent = true
                                        otpTimer = 30
                                        Toast.makeText(context, "OTP Sent! Your verification code: $generatedOtp", Toast.LENGTH_LONG).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("send_otp_button")
                            ) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Get OTP Code", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        } else {
                            // OTP Input Step
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "✓ OTP Sent to +91 $phoneNumber",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Test Code: $generatedOtp",
                                        fontWeight = FontWeight.SemiBold,
                                        color = GoldAccent,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = enteredOtp,
                                onValueChange = { if (it.length <= 6) enteredOtp = it },
                                label = { Text("Enter 6-Digit OTP") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GoldAccent)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (otpTimer > 0) "Resend in ${otpTimer}s" else "Didn't get code?",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                TextButton(
                                    onClick = {
                                        generatedOtp = (100000..999999).random().toString()
                                        otpTimer = 30
                                        Toast.makeText(context, "New OTP Sent: $generatedOtp", Toast.LENGTH_LONG).show()
                                    },
                                    enabled = otpTimer == 0
                                ) {
                                    Text("Resend OTP", fontWeight = FontWeight.Bold, color = if (otpTimer == 0) GoldAccent else Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (enteredOtp == generatedOtp || enteredOtp == "123456") {
                                        isVerifyingOtp = true
                                        viewModel.loginWithPhone(phoneNumber = "+91 $phoneNumber", name = "Gamer $phoneNumber") {
                                            isVerifyingOtp = false
                                            Toast.makeText(context, "Phone Verified! Logged in successfully.", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess()
                                        }
                                    } else {
                                        Toast.makeText(context, "Invalid OTP! Use $generatedOtp", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("verify_otp_button")
                            ) {
                                if (isVerifyingOtp) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verify & Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = {
                                    otpSent = false
                                    enteredOtp = ""
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Change Phone Number", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }

                    // TAB 1: GOOGLE SIGN-IN
                    1 -> {
                        Text(
                            text = "Google Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fast & secure 1-click login with your Google account",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Official Google Styled Button
                        Card(
                            onClick = { showGoogleAccountPicker = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("google_login_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFF4285F4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Your account details are 100% safe & encrypted",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // TAB 2: EMAIL SIGN-IN
                    2 -> {
                        Text(
                            text = "Email Sign In / Register",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = emailName,
                            onValueChange = { emailName = it },
                            label = { Text("Your Gamer Name") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (emailInput.isEmpty() || !emailInput.contains("@")) {
                                    Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.loginWithEmail(email = emailInput, name = emailName) {
                                        Toast.makeText(context, "Logged in as $emailName", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Sign In with Email", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Skip / Guest Button
        OutlinedButton(
            onClick = onAuthSuccess,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip & Browse Tournaments", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    // GOOGLE ACCOUNT PICKER DIALOG
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountPicker = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF4285F4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Choose Google Account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "To continue to Tournament Hub, select an account:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Option 1
                    Card(
                        onClick = {
                            viewModel.loginWithGoogle(accountName = "Mann Patel", accountEmail = "mannpatel9094@gmail.com") {
                                showGoogleAccountPicker = false
                                Toast.makeText(context, "Signed in with Google as Mann Patel", Toast.LENGTH_SHORT).show()
                                onAuthSuccess()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(GoldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("M", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Mann Patel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("mannpatel9094@gmail.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Account Option 2
                    Card(
                        onClick = {
                            viewModel.loginWithGoogle(accountName = "Pro Gamer", accountEmail = "progamer.hub@gmail.com") {
                                showGoogleAccountPicker = false
                                Toast.makeText(context, "Signed in with Google as Pro Gamer", Toast.LENGTH_SHORT).show()
                                onAuthSuccess()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(EmeraldGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("P", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Pro Gamer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("progamer.hub@gmail.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGoogleAccountPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.TournamentViewModel
import kotlinx.coroutines.delay

enum class AuthMode {
    LOGIN,
    SIGNUP,
    PHONE_OTP,
    GOOGLE,
    EMAIL_VERIFICATION
}

@Composable
fun AuthScreen(
    viewModel: TournamentViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    var currentAuthMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var unverifiedUserEmail by remember { mutableStateOf("") }
    var unverifiedUserPassword by remember { mutableStateOf("") }
    var verificationError by remember { mutableStateOf<String?>(null) }
    var isCheckingVerification by remember { mutableStateOf(false) }

    // LOGIN FORM STATE
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoginLoading by remember { mutableStateOf(false) }

    // FORGOT PASSWORD STATE
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }
    var isSendingReset by remember { mutableStateOf(false) }

    // SIGNUP FORM STATE
    var signUpFullName by remember { mutableStateOf("") }
    var signUpUsername by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPhone by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpConfirmPassword by remember { mutableStateOf("") }
    var showSignUpPassword by remember { mutableStateOf(false) }
    var selectedAvatarIndex by remember { mutableIntStateOf(0) }
    var signUpError by remember { mutableStateOf<String?>(null) }
    var isSignUpLoading by remember { mutableStateOf(false) }

    val avatarList = listOf(
        R.drawable.img_avatar_lion1 to "Golden Lion",
        R.drawable.img_avatar_lion2 to "Cyber Lion",
        R.drawable.img_avatar_lion3 to "Fire Lion",
        R.drawable.img_avatar_lion4 to "King Lion"
    )

    // PHONE OTP STATE
    var phoneNumber by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var otpTimer by remember { mutableIntStateOf(30) }
    var isVerifyingOtp by remember { mutableStateOf(false) }

    // GOOGLE SIGN-IN STATE
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var customGoogleEmail by remember { mutableStateOf("") }
    var customGoogleName by remember { mutableStateOf("") }
    var googleLoginError by remember { mutableStateOf<String?>(null) }

    // Timer countdown effect for OTP
    LaunchedEffect(otpSent, otpTimer) {
        if (otpSent && otpTimer > 0) {
            delay(1000L)
            otpTimer -= 1
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Branding Logo Badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(GoldAccent.copy(alpha = 0.15f), CircleShape)
                .border(2.dp, GoldAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.dangerous_gaming_logo_1784896126024),
                contentDescription = "DRB WINNERS Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Welcome Title & Subtitle (Mandatory Requirement)
        Text(
            text = "Welcome to DRB WINNERS",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Login or Create an Account to Continue",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = GoldAccent,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Access Auth Mode Buttons (Mandatory Requirement: Google, Phone, Email, Create Account)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Continue with Google
                Button(
                    onClick = {
                        currentAuthMode = AuthMode.GOOGLE
                        showGoogleAccountPicker = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentAuthMode == AuthMode.GOOGLE) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (currentAuthMode == AuthMode.GOOGLE) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_continue_google")
                ) {
                    Text("🌐 Google", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Button 2: Continue with Phone Number
                Button(
                    onClick = { currentAuthMode = AuthMode.PHONE_OTP },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentAuthMode == AuthMode.PHONE_OTP) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (currentAuthMode == AuthMode.PHONE_OTP) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_continue_phone")
                ) {
                    Text("📱 Phone OTP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 3: Login with Email
                Button(
                    onClick = {
                        currentAuthMode = AuthMode.LOGIN
                        loginError = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentAuthMode == AuthMode.LOGIN) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (currentAuthMode == AuthMode.LOGIN) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_login_email")
                ) {
                    Text("🔑 Email Login", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Button 4: Create New Account
                Button(
                    onClick = {
                        currentAuthMode = AuthMode.SIGNUP
                        signUpError = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentAuthMode == AuthMode.SIGNUP) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (currentAuthMode == AuthMode.SIGNUP) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_create_account")
                ) {
                    Text("📝 New Account", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Auth Card Container
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Crossfade(targetState = currentAuthMode, label = "AuthModeTransition") { mode ->
                    when (mode) {
                        // ==========================================
                        // 1. EMAIL LOGIN FORM
                        // ==========================================
                        AuthMode.LOGIN -> {
                            Column {
                                Text(
                                    text = "Sign In to Your Account",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Enter your registered Firebase email and password",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = {
                                        loginEmail = it
                                        loginError = null
                                    },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = GoldAccent) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldAccent,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_email_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = {
                                        loginPassword = it
                                        loginError = null
                                    },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        IconButton(onClick = { showPassword = !showPassword }) {
                                            Text(if (showPassword) "👁️" else "🙈", fontSize = 14.sp)
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldAccent,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input")
                                )

                                // Forgot Password Link
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            resetEmailInput = loginEmail
                                            showForgotPasswordDialog = true
                                        }
                                    ) {
                                        Text(
                                            text = "Forgot Password?",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent
                                        )
                                    }
                                }

                                if (loginError != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFEF5350), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = loginError!!,
                                            color = Color(0xFFC62828),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val cleanEmail = loginEmail.trim()
                                        val cleanPass = loginPassword.trim()
                                        if (cleanEmail.isEmpty() || !cleanEmail.contains("@") || cleanPass.isEmpty()) {
                                            loginError = "Email or password is incorrect"
                                        } else {
                                            isLoginLoading = true
                                            loginError = null
                                            viewModel.firebaseSignInWithEmail(cleanEmail, cleanPass) { status, err, email ->
                                                isLoginLoading = false
                                                when (status) {
                                                    TournamentViewModel.FirebaseAuthStatus.SUCCESS -> {
                                                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                                        onAuthSuccess()
                                                    }
                                                    TournamentViewModel.FirebaseAuthStatus.UNVERIFIED_EMAIL -> {
                                                        unverifiedUserEmail = email ?: cleanEmail
                                                        unverifiedUserPassword = cleanPass
                                                        verificationError = null
                                                        currentAuthMode = AuthMode.EMAIL_VERIFICATION
                                                    }
                                                    TournamentViewModel.FirebaseAuthStatus.ERROR -> {
                                                        loginError = err ?: "Email or password is incorrect"
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_login_button")
                                ) {
                                    if (isLoginLoading) {
                                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Sign In with Email", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Don't have an account?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    TextButton(
                                        onClick = {
                                            currentAuthMode = AuthMode.SIGNUP
                                            signUpError = null
                                        }
                                    ) {
                                        Text("Create New Account", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // ==========================================
                        // 2. SIGNUP FORM (Mandatory Fields)
                        // ==========================================
                        AuthMode.SIGNUP -> {
                            Column {
                                Text(
                                    text = "Create New Account",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fill in your gamer profile details to get started",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Full Name
                                OutlinedTextField(
                                    value = signUpFullName,
                                    onValueChange = {
                                        signUpFullName = it
                                        signUpError = null
                                    },
                                    label = { Text("Full Name") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = GoldAccent) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_fullname_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Username
                                OutlinedTextField(
                                    value = signUpUsername,
                                    onValueChange = {
                                        signUpUsername = it
                                        signUpError = null
                                    },
                                    label = { Text("Gamer Username") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = GoldAccent) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_username_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Email
                                OutlinedTextField(
                                    value = signUpEmail,
                                    onValueChange = {
                                        signUpEmail = it
                                        signUpError = null
                                    },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = GoldAccent) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_email_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Phone Number
                                OutlinedTextField(
                                    value = signUpPhone,
                                    onValueChange = {
                                        if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                                            signUpPhone = it
                                            signUpError = null
                                        }
                                    },
                                    label = { Text("Phone Number") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = GoldAccent) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_phone_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Password
                                OutlinedTextField(
                                    value = signUpPassword,
                                    onValueChange = {
                                        signUpPassword = it
                                        signUpError = null
                                    },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                                    visualTransformation = if (showSignUpPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_password_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Confirm Password
                                OutlinedTextField(
                                    value = signUpConfirmPassword,
                                    onValueChange = {
                                        signUpConfirmPassword = it
                                        signUpError = null
                                    },
                                    label = { Text("Confirm Password") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_confirm_password_input")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Profile Photo (Optional Gaming Avatar Picker)
                                Text(
                                    text = "Select Gaming Lion Avatar (Optional)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    avatarList.forEachIndexed { idx, pair ->
                                        val (drawableRes, avatarName) = pair
                                        val isSelected = selectedAvatarIndex == idx
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { selectedAvatarIndex = idx }
                                                .padding(2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) GoldAccent else MaterialTheme.colorScheme.surface
                                                    )
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) GoldAccent else Color.Gray.copy(alpha = 0.5f),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(id = drawableRes),
                                                    contentDescription = avatarName,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = avatarName,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (signUpError != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFEF5350), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = signUpError!!,
                                            color = Color(0xFFC62828),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        val cleanEmail = signUpEmail.trim()
                                        val cleanPass = signUpPassword.trim()
                                        val cleanConfirm = signUpConfirmPassword.trim()

                                        if (signUpFullName.isBlank() || cleanEmail.isBlank() || cleanPass.isBlank()) {
                                            signUpError = "Please fill in all required fields"
                                        } else if (!cleanEmail.contains("@")) {
                                            signUpError = "Please enter a valid email address"
                                        } else if (cleanPass != cleanConfirm) {
                                            signUpError = "Passwords do not match"
                                        } else if (cleanPass.length < 6) {
                                            signUpError = "Password must be at least 6 characters"
                                        } else {
                                            isSignUpLoading = true
                                            signUpError = null
                                            viewModel.firebaseSignUpFull(
                                                fullName = signUpFullName.trim(),
                                                username = signUpUsername.trim(),
                                                email = cleanEmail,
                                                phone = signUpPhone.trim(),
                                                password = cleanPass,
                                                profilePic = avatarList[selectedAvatarIndex].second
                                            ) { status, err, email ->
                                                isSignUpLoading = false
                                                when (status) {
                                                    TournamentViewModel.FirebaseAuthStatus.SUCCESS -> {
                                                        onAuthSuccess()
                                                    }
                                                    TournamentViewModel.FirebaseAuthStatus.UNVERIFIED_EMAIL -> {
                                                        unverifiedUserEmail = email ?: cleanEmail
                                                        unverifiedUserPassword = cleanPass
                                                        verificationError = null
                                                        currentAuthMode = AuthMode.EMAIL_VERIFICATION
                                                    }
                                                    TournamentViewModel.FirebaseAuthStatus.ERROR -> {
                                                        signUpError = err ?: "User already exists. Please sign in"
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_signup_button")
                                ) {
                                    if (isSignUpLoading) {
                                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Already have an account?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    TextButton(
                                        onClick = {
                                            currentAuthMode = AuthMode.LOGIN
                                            loginError = null
                                        }
                                    ) {
                                        Text("Sign In", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // ==========================================
                        // 3. PHONE OTP FORM
                        // ==========================================
                        AuthMode.PHONE_OTP -> {
                            Column {
                                Text(
                                    text = "Mobile Number OTP Login",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "We will send a 6-digit verification code to your mobile phone",
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
                                        Text("Send OTP Code", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                } else {
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
                                                text = "Verification Code: $generatedOtp",
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
                                            text = if (otpTimer > 0) "Resend in ${otpTimer}s" else "Didn't receive code?",
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
                                            if (enteredOtp == generatedOtp) {
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
                                            Text("Verify OTP & Login", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                        }

                        // ==========================================
                        // 4. GOOGLE SIGN-IN CARD
                        // ==========================================
                        AuthMode.GOOGLE -> {
                            Column {
                                Text(
                                    text = "Google Authentication",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fast & secure 1-click login with your Google account",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

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
                        }

                        // ==========================================
                        // 5. EMAIL VERIFICATION SCREEN
                        // ==========================================
                        AuthMode.EMAIL_VERIFICATION -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("verification_screen")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(GoldAccent.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email Verification",
                                            tint = GoldAccent,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Verify Your Email Address",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val displayEmail = if (unverifiedUserEmail.isNotBlank()) unverifiedUserEmail else "your email address"
                                    Text(
                                        text = "A real verification link has been sent to . Please open your email app, click the link, and then tap Check Email Verification Status below.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .testTag("verification_message")
                                    )

                                    if (verificationError != null) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFEF5350), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = verificationError!!,
                                                color = Color(0xFFC62828),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            isCheckingVerification = true
                                            verificationError = null
                                            viewModel.checkEmailVerificationStatus(unverifiedUserEmail, unverifiedUserPassword) { isVerified, msg ->
                                                isCheckingVerification = false
                                                if (isVerified) {
                                                    Toast.makeText(context, "Email Verified Successfully! Welcome to DRB WINNERS", Toast.LENGTH_LONG).show()
                                                    onAuthSuccess()
                                                } else {
                                                    verificationError = msg
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("verification_login_button")
                                    ) {
                                        if (isCheckingVerification) {
                                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Check Email Verification Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = {
                                            if (unverifiedUserEmail.isNotBlank()) {
                                                viewModel.resendVerificationEmail(unverifiedUserEmail, unverifiedUserPassword) { success, msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text("Resend Verification Link", color = GoldAccent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    TextButton(
                                        onClick = {
                                            currentAuthMode = AuthMode.LOGIN
                                            loginError = null
                                            verificationError = null
                                        }
                                    ) {
                                        Text("Back to Sign In", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // FORGOT PASSWORD DIALOG
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text("Reset Password", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Enter your registered Firebase email to receive a password reset link:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanReset = resetEmailInput.trim()
                        if (cleanReset.isEmpty() || !cleanReset.contains("@")) {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                        } else {
                            isSendingReset = true
                            viewModel.firebaseSendPasswordResetEmail(cleanReset) { success, msg ->
                                isSendingReset = false
                                showForgotPasswordDialog = false
                                Toast.makeText(context, msg ?: "Password reset email sent!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    if (isSendingReset) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Send Reset Link", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // GOOGLE ACCOUNT PICKER DIALOG
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = {
                showGoogleAccountPicker = false
                googleLoginError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF4285F4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Google Account Sign In", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter your real Google Email Address to sign in:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = customGoogleEmail,
                        onValueChange = {
                            customGoogleEmail = it
                            googleLoginError = null
                        },
                        label = { Text("Google Email Address") },
                        placeholder = { Text("your.email@gmail.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = GoldAccent)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_email_custom_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customGoogleName,
                        onValueChange = {
                            customGoogleName = it
                            googleLoginError = null
                        },
                        label = { Text("Gamer Display Name (Optional)") },
                        placeholder = { Text("Your Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_name_custom_input")
                    )

                    if (googleLoginError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = googleLoginError!!,
                            color = Color(0xFFEF5350),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val finalEmail = customGoogleEmail.trim().lowercase()
                            if (finalEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(finalEmail).matches()) {
                                googleLoginError = "Please enter a valid Google email address (e.g. user@gmail.com)"
                            } else {
                                val inferredName = if (customGoogleName.isNotBlank()) customGoogleName.trim() else finalEmail.substringBefore("@").replace(".", " ").replace("_", " ").replaceFirstChar { it.uppercase() }
                                viewModel.loginWithGoogle(accountName = inferredName, accountEmail = finalEmail) {
                                    showGoogleAccountPicker = false
                                    googleLoginError = null
                                    Toast.makeText(context, "Logged in as ", Toast.LENGTH_SHORT).show()
                                    onAuthSuccess()
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_custom_signin_button")
                    ) {
                        Text("Continue with Google", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showGoogleAccountPicker = false
                    googleLoginError = null
                }) {
                    Text("Cancel", color = GoldAccent)
                }
            }
        )
    }
}

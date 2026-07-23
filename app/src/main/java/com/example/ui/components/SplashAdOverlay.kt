package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.entity.AdEntity
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.delay

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import coil.compose.AsyncImage

@Composable
fun SplashAdOverlay(
    ad: AdEntity?,
    onDismiss: () -> Unit
) {
    if (ad == null || !ad.isEnabled) return

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var secondsRemaining by remember { mutableIntStateOf(ad.displayDurationSeconds.coerceAtLeast(3)) }
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        } else {
            isVisible = false
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("splash_ad_overlay"),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Poster Image / Ad Image
                if (ad.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ad.imageUrl,
                        contentDescription = ad.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (ad.imageResId != 0) {
                    Image(
                        painter = painterResource(id = ad.imageResId),
                        contentDescription = ad.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF1E1B4B),
                                        Color(0xFF020617)
                                    )
                                )
                            )
                    )
                }

                // Overlay gradient for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top Skip Bar with Countdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 20.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ad · $secondsRemaining s",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            isVisible = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("skip_ad_button")
                    ) {
                        Text("Skip Ad", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Bottom Content Details
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "SPONSORED",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ad.title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

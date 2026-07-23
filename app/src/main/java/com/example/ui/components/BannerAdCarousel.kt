package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.db.entity.AdEntity
import com.example.ui.theme.GoldAccent

@Composable
fun BannerAdCarousel(
    bannerAds: List<AdEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val activeAd = bannerAds.firstOrNull { it.isEnabled } ?: return

    Card(
        onClick = {
            if (activeAd.targetUrl.isNotBlank()) {
                try {
                    val url = if (!activeAd.targetUrl.startsWith("http://") && !activeAd.targetUrl.startsWith("https://")) {
                        "https://${activeAd.targetUrl}"
                    } else {
                        activeAd.targetUrl
                    }
                    uriHandler.openUri(url)
                } catch (e: Exception) {
                    Toast.makeText(context, "Link: ${activeAd.targetUrl}", Toast.LENGTH_SHORT).show()
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (activeAd.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = activeAd.imageUrl,
                    contentDescription = activeAd.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (activeAd.imageResId != 0) {
                Image(
                    painter = painterResource(id = activeAd.imageResId),
                    contentDescription = activeAd.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E1B4B),
                                    Color(0xFF311B92),
                                    Color(0xFF4A148C)
                                )
                            )
                        )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(GoldAccent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ADVERTISEMENT",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        if (activeAd.targetUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "External Link",
                                tint = GoldAccent,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = activeAd.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    if (activeAd.targetUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to open link",
                            color = GoldAccent.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ad Details",
                    tint = GoldAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}


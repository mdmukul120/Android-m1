package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentSection
import com.example.data.model.LiveChannel
import com.example.data.model.MediaItem
import com.example.ui.components.ChannelCard
import com.example.ui.components.HeroBanner
import com.example.ui.components.MediaCard
import com.example.ui.theme.MukulDarkBg
import com.example.ui.theme.MukulRedPrimary
import com.example.ui.theme.MukulTextPrimary
import com.example.ui.theme.MukulTextSecondary

@Composable
fun HomeScreen(
    heroMedia: MediaItem?,
    sections: List<ContentSection>,
    liveChannels: List<LiveChannel>,
    watchlistIds: Set<String>,
    isLoading: Boolean,
    onMediaClick: (MediaItem) -> Unit,
    onChannelClick: (LiveChannel) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onNavigateToPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .background(MukulDarkBg)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MukulRedPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Loading Mukul plus...",
                    color = MukulTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MukulDarkBg)
            .testTag("home_screen")
    ) {
        // Hero Banner
        if (heroMedia != null) {
            item(key = "hero_banner") {
                HeroBanner(
                    media = heroMedia,
                    isInWatchlist = watchlistIds.contains(heroMedia.id),
                    onWatchNow = { onMediaClick(heroMedia) },
                    onToggleWatchlist = { onToggleWatchlist(heroMedia.id) }
                )
            }
        }

        // Live Channels Carousel
        if (liveChannels.isNotEmpty()) {
            item(key = "live_channels_section") {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = MukulRedPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "Live TV Channels",
                                color = MukulTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${liveChannels.size} Channels",
                            color = MukulTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(liveChannels.take(15), key = { it.id }) { channel ->
                            ChannelCard(
                                channel = channel,
                                onClick = { onChannelClick(channel) }
                            )
                        }
                    }
                }
            }
        }

        // Media Sections (Recommendations, Bollywood, Hindi Dubbed, Bongo Specials)
        items(sections, key = { it.title }) { section ->
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = section.title,
                        color = MukulTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { }
                    ) {
                        Text(
                            text = "See All",
                            color = MukulRedPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MukulRedPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(section.items, key = { it.id }) { media ->
                        MediaCard(
                            media = media,
                            isInWatchlist = watchlistIds.contains(media.id),
                            onClick = { onMediaClick(media) },
                            onToggleWatchlist = { onToggleWatchlist(media.id) }
                        )
                    }
                }
            }
        }

        // Bottom Plans Teaser
        item(key = "plans_teaser") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onNavigateToPlans() }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF191A24),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2B3D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Experience Mukul plus VIP",
                                color = MukulTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "4K HDR, No Ads, 230+ Live Channels & Downloads",
                                color = MukulTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MukulRedPrimary
                        ) {
                            Text(
                                text = "View Plans",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

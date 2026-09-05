package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MukulCardBg
import com.example.ui.theme.MukulCardBorder
import com.example.ui.theme.MukulDarkBg
import com.example.ui.theme.MukulGold
import com.example.ui.theme.MukulRedGlowing
import com.example.ui.theme.MukulRedPrimary
import com.example.ui.theme.MukulTextPrimary
import com.example.ui.theme.MukulTextSecondary

@Composable
fun PlansScreen(
    onSelectPlan: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MukulDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("plans_screen")
    ) {
        // Header
        Text(
            text = "Choose your plan",
            color = MukulTextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Watch all you want. Recommendations just for you.\nChange or cancel your plan anytime.",
            color = MukulTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Platinum Plan (Highlighted First / Top or Center)
        PlanCard(
            title = "Platinum Plan",
            price = "$29.99",
            period = "/year",
            isFeatured = true,
            badge = "MOST POPULAR",
            features = listOf(
                "Unlimited movies & Live TV",
                "No Ads",
                "Team watching up to 55 members",
                "4K + HDR Resolution",
                "300 Downloading slots",
                "Exclusive Bongo & Bioscope Originals"
            ),
            onSelect = { onSelectPlan("Platinum") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Gold Plan
        PlanCard(
            title = "Gold Plan",
            price = "$9.99",
            period = "/month",
            isFeatured = false,
            badge = null,
            features = listOf(
                "No Ads",
                "Team watching up to 10 members",
                "720p Resolution",
                "50 Downloading slots",
                "Access to Bollywood & Hindi Dubbed"
            ),
            onSelect = { onSelectPlan("Gold") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Diamond Plan
        PlanCard(
            title = "Diamond Plan",
            price = "$19.99",
            period = "/year",
            isFeatured = false,
            badge = "BEST VALUE",
            features = listOf(
                "Unlimited movies",
                "No Ads",
                "Team watching up to 20 members",
                "1080p Full HD Resolution",
                "100 Downloading slots",
                "All 230+ IPTV Channels"
            ),
            onSelect = { onSelectPlan("Diamond") }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    period: String,
    isFeatured: Boolean,
    badge: String?,
    features: List<String>,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured) Color(0xFF1B1822) else MukulCardBg
        ),
        border = BorderStroke(
            width = if (isFeatured) 2.dp else 1.dp,
            color = if (isFeatured) MukulRedPrimary else MukulCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = MukulTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFeatured) MukulRedPrimary else Color(0xFF2A2B3D)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    color = if (isFeatured) MukulRedGlowing else MukulGold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = period,
                    color = MukulTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Features list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (feature in features) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFeatured) MukulRedPrimary.copy(alpha = 0.2f) else Color(0xFF222430))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isFeatured) MukulRedGlowing else MukulGold,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            color = MukulTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSelect,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFeatured) MukulRedPrimary else Color(0xFF262738),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(
                    text = "Get Trial Period",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

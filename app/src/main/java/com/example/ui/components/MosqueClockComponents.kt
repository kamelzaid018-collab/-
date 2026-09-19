package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.PrayerType
import com.example.data.model.TimeFormatPreference
import com.example.ui.language.AppStrings

// Mosque Clock Colors
val MosqueEmeraldDark = Color(0xFF031D18)
val MosqueEmeraldPanel = Color(0xFF072F27)
val MosqueEmeraldBorder = Color(0xFF0E5244)
val MosqueGold = Color(0xFFFFD54F)
val MosqueGoldLight = Color(0xFFFFF176)
val MosqueGoldGlow = Color(0x33FFD54F)
val MosqueLedCyan = Color(0xFF64FFDA)
val MosqueLedRed = Color(0xFFFF5252)

@Composable
fun MosqueHeaderCard(
    cityName: String,
    dayOfWeek: String,
    hijriDateText: String,
    gregorianDateText: String,
    lang: Language
) {
    Surface(
        color = MosqueEmeraldPanel,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, MosqueGold.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MosqueEmeraldDark)
                    .border(1.dp, MosqueGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(text = "📍", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = cityName,
                    color = MosqueGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day of week & Hijri date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayOfWeek,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = hijriDateText,
                    color = MosqueGoldLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Gregorian Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (lang.isRtl) "التاريخ الميلادي" else "Gregorian Date",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
                Text(
                    text = gregorianDateText,
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LiveDigitalClockCard(
    hours: String,
    minutes: String,
    seconds: String,
    amPm: String,
    is24H: Boolean,
    lang: Language
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkColon")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colonAlpha"
    )

    Surface(
        color = MosqueEmeraldDark,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(2.dp, MosqueGold),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = AppStrings.currentTime(lang),
                color = Color(0xFFA7F3D0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Large Electronic Clock Display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hour box
                DigitalSegmentBox(text = hours)

                // Colon
                Text(
                    text = ":",
                    color = MosqueGold.copy(alpha = colonAlpha),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Minute box
                DigitalSegmentBox(text = minutes)

                // Colon
                Text(
                    text = ":",
                    color = MosqueGold.copy(alpha = colonAlpha),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Second box
                DigitalSegmentBox(text = seconds, isSecond = true)

                // AM / PM Badge
                if (!is24H) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F3830))
                            .border(1.dp, MosqueGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = amPm,
                            color = MosqueGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalSegmentBox(text: String, isSecond: Boolean = false) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF02120F))
            .border(1.dp, Color(0xFF134E42), RoundedCornerShape(8.dp))
            .padding(horizontal = if (isSecond) 8.dp else 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSecond) MosqueLedCyan else MosqueGoldLight,
            fontSize = if (isSecond) 32.sp else 38.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun NextPrayerBanner(
    prayerType: PrayerType,
    countdownText: String,
    lang: Language
) {
    val prayerName = AppStrings.getPrayerName(prayerType, lang)

    Surface(
        color = Color(0xFF1E1402),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MosqueGold),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = AppStrings.nextPrayer(lang),
                    color = Color(0xFFFDE68A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = prayerName,
                    color = MosqueGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = AppStrings.timeRemaining(lang),
                    color = Color(0xFFFDE68A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = countdownText,
                    color = MosqueLedCyan,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun PrayerRowItem(
    prayerType: PrayerType,
    timeText: String,
    isNext: Boolean,
    lang: Language
) {
    val prayerName = AppStrings.getPrayerName(prayerType, lang)
    val bgColor by animateColorAsState(
        targetValue = if (isNext) Color(0xFF0D3B32) else MosqueEmeraldPanel,
        label = "PrayerRowBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isNext) MosqueGold else MosqueEmeraldBorder,
        label = "PrayerRowBorder"
    )

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (isNext) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(if (isNext) 4.dp else 1.dp, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isNext) {
                    Text(
                        text = "▶",
                        color = MosqueGold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = prayerName,
                    color = if (isNext) MosqueGoldLight else Color.White,
                    fontSize = 18.sp,
                    fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold
                )
            }

            Text(
                text = timeText,
                color = if (isNext) MosqueGold else Color(0xFF6EE7B7),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

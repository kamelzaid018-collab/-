package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Language
import com.example.data.model.PrayerType
import com.example.data.model.TimeFormatPreference
import com.example.ui.language.AppStrings
import java.util.Locale

// Palette matched from reference screenshot
val AqsaDarkBg = Color(0xFF031411)
val AqsaCardBg = Color(0xFF06231E)
val AqsaCardBorder = Color(0xFFC8A858)
val AqsaGold = Color(0xFFE5C158)
val AqsaGoldLight = Color(0xFFFBE49B)
val AqsaMint = Color(0xFF8CE3C3)
val AqsaTextMuted = Color(0xFF94A3B8)

@Composable
fun AlAqsaHeaderSection(
    locationName: String,
    countryName: String,
    hours: String,
    minutes: String,
    amPm: String,
    hijriDateText: String,
    gregorianDateText: String,
    lang: Language,
    onLocationClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
    ) {
        // Background Al-Aqsa artwork
        Image(
            painter = painterResource(id = R.drawable.img_alaqsa_header),
            contentDescription = "Al-Aqsa Mosque Dome of the Rock",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    // Dark vignette gradients for perfect readability
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC020E0C),
                                Color(0x66020E0C),
                                Color(0xEE031411),
                                AqsaDarkBg
                            )
                        )
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar: Location on left, Settings on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location selector badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x77000000))
                        .border(1.dp, AqsaGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { onLocationClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AqsaGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = locationName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AqsaGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (countryName.isNotBlank()) {
                            Text(
                                text = countryName,
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Settings button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x77000000))
                        .border(1.dp, AqsaGold.copy(alpha = 0.5f), CircleShape)
                        .clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current Time (Left) & Calligraphy (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Clock and Dates
                Column {
                    Text(
                        text = if (lang.isRtl) "الوقت الحالي" else "Current Time",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$hours:$minutes",
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp
                        )
                        if (amPm.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = amPm,
                                color = AqsaGold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Hijri Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = AqsaGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = hijriDateText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Gregorian Date
                    Text(
                        text = gregorianDateText,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                }

                // Right Column: Salawat Calligraphy Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, AqsaGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "اللَّهُمَّ صَلِّ عَلَى",
                            color = AqsaGoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مُحَمَّدٍ وَآلِ مُحَمَّدٍ",
                            color = AqsaGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NextPrayerFeaturedCard(
    prayerType: PrayerType,
    prayerTimeText: String,
    countdownHours: Long,
    countdownMinutes: Long,
    countdownSeconds: Long,
    lang: Language
) {
    val prayerName = AppStrings.getPrayerName(prayerType, lang)

    Surface(
        color = AqsaCardBg,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.8.dp, AqsaCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B332C), Color(0xFF041914))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Subtitle
                Text(
                    text = "✧ " + (if (lang.isRtl) "يتبقى على موعد الصلاة" else "Time Remaining Until") + " ✧",
                    color = AqsaGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Prayer Name
                Text(
                    text = prayerName,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Row with Mosque Arch Graphic, Countdown, and Prayer Time Seal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Mosque Arch Silhouette
                    Box(
                        modifier = Modifier
                            .size(68.dp, 84.dp)
                            .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(Color(0xFF051C17))
                            .border(1.dp, AqsaGold.copy(alpha = 0.4f), RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp, bottomStart = 8.dp, bottomEnd = 8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🕌", fontSize = 34.sp)
                    }

                    // Center: Countdown Boxes (Hours : Minutes : Seconds)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CountdownSegmentBox(
                            value = String.format(Locale.US, "%02d", countdownSeconds),
                            unitLabel = if (lang.isRtl) "ثانية" else "sec"
                        )

                        Text(
                            text = ":",
                            color = AqsaGold,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 16.dp)
                        )

                        CountdownSegmentBox(
                            value = String.format(Locale.US, "%02d", countdownMinutes),
                            unitLabel = if (lang.isRtl) "دقيقة" else "min"
                        )

                        Text(
                            text = ":",
                            color = AqsaGold,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 16.dp)
                        )

                        CountdownSegmentBox(
                            value = String.format(Locale.US, "%02d", countdownHours),
                            unitLabel = if (lang.isRtl) "ساعة" else "hour"
                        )
                    }

                    // Right: Octagonal Seal with Prayer Time
                    Box(
                        modifier = Modifier
                            .size(76.dp, 84.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF031612))
                            .border(1.5.dp, AqsaGold, RoundedCornerShape(16.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (lang.isRtl) "موعد الصلاة" else "Prayer Time",
                                color = AqsaGoldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = prayerTimeText,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = AqsaGold,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownSegmentBox(value: String, unitLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF031713))
                .border(1.dp, AqsaGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = unitLabel,
            color = Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PrayerTimesPillBar(
    fajrTime: String,
    sunriseTime: String,
    dhuhrTime: String,
    asrTime: String,
    maghribTime: String,
    ishaTime: String,
    nextPrayerType: PrayerType?,
    lang: Language
) {
    Surface(
        color = Color(0xFF041914),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AqsaGold.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrayerPillItem(
                title = AppStrings.getPrayerName(PrayerType.FAJR, lang),
                time = fajrTime,
                icon = "🌅",
                isNext = nextPrayerType == PrayerType.FAJR
            )
            PrayerPillItem(
                title = AppStrings.getPrayerName(PrayerType.SUNRISE, lang),
                time = sunriseTime,
                icon = "☀️",
                isNext = false
            )
            PrayerPillItem(
                title = AppStrings.getPrayerName(PrayerType.DHUHR, lang),
                time = dhuhrTime,
                icon = "🔆",
                isNext = nextPrayerType == PrayerType.DHUHR || nextPrayerType == PrayerType.JUMUAH
            )
            PrayerPillItem(
                title = AppStrings.getPrayerName(PrayerType.ASR, lang),
                time = asrTime,
                icon = "🌤️",
                isNext = nextPrayerType == PrayerType.ASR
            )
            PrayerPillItem(
                title = AppStrings.getPrayerName(PrayerType.MAGHRIB, lang),
                time = maghribTime,
                icon = "🌇",
                isNext = nextPrayerType == PrayerType.MAGHRIB
            )
            PrayerPillItem(
                title = AppStrings.getPrayerName(PrayerType.ISHA, lang),
                time = ishaTime,
                icon = "🌙",
                isNext = nextPrayerType == PrayerType.ISHA
            )
        }
    }
}

@Composable
private fun PrayerPillItem(
    title: String,
    time: String,
    icon: String,
    isNext: Boolean
) {
    val bgModifier = if (isNext) {
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF093129))
            .border(1.5.dp, AqsaGold, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    } else {
        Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
    }

    Column(
        modifier = bgModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = if (isNext) AqsaGoldLight else Color(0xFFCBD5E1),
            fontSize = 12.sp,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = time,
            color = if (isNext) AqsaGold else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun SalawatCountdownCard(
    remainingHours: Long,
    remainingMinutes: Long,
    remainingSeconds: Long,
    isEnabled: Boolean,
    lang: Language,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF041C17),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, AqsaGold.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left thumbnail image of Al-Aqsa / Jerusalem
            Image(
                painter = painterResource(id = R.drawable.img_alaqsa_thumbnail),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp, 68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, AqsaGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Center details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🕌", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✧ " + (if (lang.isRtl) "التذكير القادم بالصلاة على النبي" else "Next Salawat Reminder") + " ✧",
                        color = AqsaGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Countdown digits
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%02d", remainingSeconds),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " : ",
                        color = AqsaGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%02d", remainingMinutes),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " : ",
                        color = AqsaGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%02d", remainingHours),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Labels
                Row(
                    modifier = Modifier.width(130.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (lang.isRtl) "ثانية" else "s", color = Color.Gray, fontSize = 10.sp)
                    Text(text = if (lang.isRtl) "دقيقة" else "m", color = Color.Gray, fontSize = 10.sp)
                    Text(text = if (lang.isRtl) "ساعة" else "h", color = Color.Gray, fontSize = 10.sp)
                }
            }

            // Right Bell button & Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF082B23))
                        .border(1.dp, AqsaGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Alert",
                        tint = AqsaGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AqsaGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun QuickFeaturesGrid(
    alertsCount: Int,
    lang: Language,
    onAlertsClick: () -> Unit,
    onAzanClick: () -> Unit,
    onRamadanClick: () -> Unit,
    onSalawatClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Alerts Card (Teal/Emerald gradient)
        FeatureCardItem(
            modifier = Modifier.weight(1f),
            title = if (lang.isRtl) "التنبيهات" else "Alerts",
            subtitle = if (lang.isRtl) "إدارة التنبيهات" else "Manage Alerts",
            icon = "🔔",
            badgeCount = alertsCount,
            gradient = listOf(Color(0xFF0C3E33), Color(0xFF041E18)),
            borderColor = Color(0xFF147A65),
            onClick = onAlertsClick
        )

        // 2. Azan Card (Navy/Teal gradient)
        FeatureCardItem(
            modifier = Modifier.weight(1f),
            title = if (lang.isRtl) "الأذان" else "Azan",
            subtitle = if (lang.isRtl) "صوت الأذان" else "Azan Audio",
            icon = "🕌",
            badgeCount = 0,
            gradient = listOf(Color(0xFF0F3642), Color(0xFF051D24)),
            borderColor = Color(0xFF1B647B),
            onClick = onAzanClick
        )

        // 3. Ramadan Card (Purple gradient)
        FeatureCardItem(
            modifier = Modifier.weight(1f),
            title = if (lang.isRtl) "رمضان" else "Ramadan",
            subtitle = if (lang.isRtl) "مواقيت رمضان" else "Iftar & Suhoor",
            icon = "🌙",
            badgeCount = 0,
            gradient = listOf(Color(0xFF381E43), Color(0xFF1B0B22)),
            borderColor = Color(0xFF6B3682),
            onClick = onRamadanClick
        )

        // 4. Salawat Card (Warm Bronze gradient)
        FeatureCardItem(
            modifier = Modifier.weight(1f),
            title = if (lang.isRtl) "الصلاة على النبي" else "Salawat",
            subtitle = if (lang.isRtl) "أذكار وأدعية" else "Daily Reminders",
            icon = "🤲",
            badgeCount = 0,
            gradient = listOf(Color(0xFF4A341B), Color(0xFF261909)),
            borderColor = Color(0xFF916533),
            onClick = onSalawatClick
        )
    }
}

@Composable
private fun FeatureCardItem(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: String,
    badgeCount: Int,
    gradient: List<Color>,
    borderColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(116.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradient))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon with badge if count > 0
                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 20.sp)
                    }
                    if (badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$badgeCount",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = Color(0xFFA5B4FC).copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AlAqsaBottomNavigationBar(
    selectedTab: Int, // 0 = Home, 1 = Mosque Clock, 2 = More
    lang: Language,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = Color(0xEE03120F),
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, AqsaGold.copy(alpha = 0.25f)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = if (lang.isRtl) "الرئيسية" else "Home",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )

            BottomNavItem(
                icon = Icons.Default.Schedule,
                label = if (lang.isRtl) "مواقيت الصلاة" else "Prayer Times",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )

            BottomNavItem(
                icon = Icons.Default.GridView,
                label = if (lang.isRtl) "المزيد" else "More",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) AqsaGold else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) AqsaGold else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(AqsaGold)
            )
        }
    }
}

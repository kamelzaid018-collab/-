package com.example.ui.screens

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.R
import com.example.data.model.Language
import com.example.data.model.PrayerTimesResult
import com.example.data.model.PrayerType
import com.example.notification.PrayerNotificationHelper
import com.example.ui.components.*
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel
import com.example.widget.PrayerAppWidgetProvider
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Custom Islamic Arch Shape for Prayer Cards
val IslamicMihrabArchShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val r = 16f

    reset()
    // Top Arch Center Peak
    moveTo(w / 2f, 0f)
    // Top right curve of arch
    cubicTo(w * 0.68f, 0f, w * 0.85f, h * 0.06f, w - r, h * 0.14f)
    cubicTo(w, h * 0.17f, w, h * 0.20f, w, h * 0.22f + r)
    // Right side down
    lineTo(w, h - r)
    cubicTo(w, h, w - r, h, w - r, h)
    // Bottom
    lineTo(r, h)
    cubicTo(0f, h, 0f, h - r, 0f, h - r)
    // Left side up
    lineTo(0f, h * 0.22f + r)
    cubicTo(0f, h * 0.20f, 0f, h * 0.17f, r, h * 0.14f)
    // Top left curve back to peak
    cubicTo(w * 0.15f, h * 0.06f, w * 0.32f, 0f, w / 2f, 0f)
    close()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationAndWidgetsScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.settingsRepo.language.collectAsState()
    val isNotifEnabled by viewModel.settingsRepo.isNotificationBarEnabled.collectAsState()
    val notifStyle by viewModel.settingsRepo.notificationBarStyle.collectAsState()
    val isNotifCountdown by viewModel.settingsRepo.isNotificationCountdownEnabled.collectAsState()
    val isNotifHijri by viewModel.settingsRepo.isNotificationHijriEnabled.collectAsState()

    val isWidgetHijri by viewModel.settingsRepo.isWidgetHijriEnabled.collectAsState()
    val isWidgetLoc by viewModel.settingsRepo.isWidgetLocationEnabled.collectAsState()
    val widgetTheme by viewModel.settingsRepo.widgetThemeStyle.collectAsState()

    val location by viewModel.settingsRepo.selectedLocation.collectAsState()
    val prayerResult by viewModel.prayerTimes.collectAsState()
    val nextPrayerInfo by viewModel.nextPrayerInfo.collectAsState()
    val salawatCountdown by viewModel.salawatRemainingSeconds.collectAsState()
    val isFriday by viewModel.isFriday.collectAsState()

    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Live Board / Widget, 1: Settings

    // Live clock ticker
    var currentTimeString by remember { mutableStateOf("") }
    var currentDayName by remember { mutableStateOf("") }
    var currentGregorianDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val tz = TimeZone.getTimeZone(viewModel.settingsRepo.getEffectiveTimeZoneId())
            val cal = Calendar.getInstance(tz)
            val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.US).apply { timeZone = tz }
            val daySdf = SimpleDateFormat("EEEE", if (lang.isRtl) Locale("ar") else Locale.ENGLISH).apply { timeZone = tz }
            val dateSdf = SimpleDateFormat("yyyy - MM - dd", Locale.US).apply { timeZone = tz }

            currentTimeString = timeSdf.format(cal.time)
            currentDayName = daySdf.format(cal.time)
            currentGregorianDate = dateSdf.format(cal.time)

            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang.isRtl) "شريط الإشعارات والتطبيقات المصغرة" else "Notification Bar & Widgets",
                        color = AqsaGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AqsaGold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            PrayerNotificationHelper.updatePersistentNotification(context)
                            PrayerAppWidgetProvider.updateAllWidgets(context)
                            Toast.makeText(
                                context,
                                if (lang.isRtl) "تم تحديث شريط الإشعارات والودجت بنجاح ✓" else "Widgets & Notification Bar Updated ✓",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AqsaGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF031411)
                )
            )
        },
        containerColor = Color(0xFF020E0C)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Tab Switcher (Design Master View vs Customization Settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF06231E))
                    .padding(4.dp)
            ) {
                TabPill(
                    text = if (lang.isRtl) "لوحة الأقصى والتطبيق المصغر" else "Al-Aqsa Widget Board",
                    icon = Icons.Default.Widgets,
                    isSelected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabPill(
                    text = if (lang.isRtl) "إعدادات شريط الإشعارات" else "Notification Settings",
                    icon = Icons.Default.Tune,
                    isSelected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            if (activeTab == 0) {
                // =========================================================================
                // MASTER AL-AQSA MOSQUE CLOCK & WIDGET BOARD (EXACT DESIGN FROM SCREENSHOT)
                // =========================================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF041914),
                                    Color(0xFF020E0C),
                                    Color(0xFF010807)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.verticalGradient(listOf(AqsaGold, Color(0xFF8A6D2B), Color(0xFF2E240D)))
                            ),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        // 1. TOP HEADER SECTION WITH AL-AQSA MOSQUE ARTWORK
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                        ) {
                            // Al-Aqsa Background image
                            Image(
                                painter = painterResource(id = R.drawable.img_alaqsa_header),
                                contentDescription = "Al-Aqsa Mosque Sunset",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0x99020F0C),
                                                    Color(0x44020F0C),
                                                    Color(0xEE031411),
                                                    Color(0xFF031411)
                                                )
                                            )
                                        )
                                    }
                            )

                            // Top elements overlay (Calligraphy left, Title center, Date right)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                // Left: Calligraphy "اللهم صل على محمد وعلى آل محمد" + Lantern
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🏮",
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",
                                            color = AqsaGoldLight,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif
                                        )
                                        Text(
                                            text = "✧ وَعَلَى آلِ مُحَمَّدٍ ✧",
                                            color = AqsaGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Center: "مواقيت الصلاة" + Location Name
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "مَوَاقِيتُ الصَّلَاةِ",
                                        color = AqsaGoldLight,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "✧── ", color = AqsaGold.copy(alpha = 0.6f), fontSize = 10.sp)
                                        Text(
                                            text = location.nameAr,
                                            color = Color(0xFFFDE68A),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = " ──✧", color = AqsaGold.copy(alpha = 0.6f), fontSize = 10.sp)
                                    }
                                }

                                // Right: Day, Gregorian & Hijri Date + Lantern
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currentDayName.ifEmpty { "اليوم" },
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AqsaGold, modifier = Modifier.size(13.dp))
                                        }
                                        Text(
                                            text = currentGregorianDate.ifEmpty { "2026 - 09 - 19" },
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 11.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${prayerResult?.hijriDay ?: 15} ${prayerResult?.hijriMonthNameAr ?: "رمضان"} ${prayerResult?.hijriYear ?: 1447} هـ",
                                                color = Color(0xFFFDE68A),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("🌙", fontSize = 10.sp)
                                        }
                                    }
                                    Text(
                                        text = "🏮",
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }

                            // Center Floating Current Time Arch Badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 12.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF062E27),
                                                Color(0xFF031915)
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(1.5.dp, AqsaGold),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .padding(horizontal = 24.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = AqsaGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "الوقت الحالي",
                                            color = AqsaGoldLight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = currentTimeString.ifEmpty { "14:37" },
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    )
                                    Text(
                                        text = "$currentDayName  ${currentGregorianDate}",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. MIDDLE ROW: WEATHER + NEXT PRAYER COUNTDOWN + SALAWAT COUNTDOWN
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // A) Weather Mini Card
                            Box(
                                modifier = Modifier
                                    .weight(0.75f)
                                    .height(86.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF05211B))
                                    .border(BorderStroke(1.dp, AqsaGold.copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("☀️", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("32° | الطقس", color = AqsaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("مشمس صافي", color = Color(0xFFE2E8F0), fontSize = 10.sp)
                                    }
                                }
                            }

                            // B) Next Prayer Countdown Arch (Maroon / Burgundy & Gold)
                            val nextName = AppStrings.getPrayerName(nextPrayerInfo?.prayerType ?: PrayerType.ASR, lang)
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(86.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF4A0E13),
                                                Color(0xFF28070A)
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(AqsaGold, Color(0xFFB45309)))),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Mosque, contentDescription = null, tint = AqsaGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "المتبقي للصلاة القادمة",
                                                color = Color(0xFFFDE68A),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = nextPrayerInfo?.countdownText ?: "01 : 22 : 13",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(0.9f),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("ثانية", color = Color.Gray, fontSize = 9.sp)
                                            Text("دقيقة", color = Color.Gray, fontSize = 9.sp)
                                            Text("ساعة", color = Color.Gray, fontSize = 9.sp)
                                        }
                                    }

                                    // Next Prayer Name Pill
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF6B141C))
                                            .border(1.dp, AqsaGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("☀️", fontSize = 14.sp)
                                            Text(
                                                text = nextName,
                                                color = AqsaGoldLight,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // C) Next Salawat on the Prophet ﷺ Countdown Arch (Emerald Green & Gold)
                            val salHrs = salawatCountdown / 3600
                            val salMins = (salawatCountdown % 3600) / 60
                            val salSecs = salawatCountdown % 60
                            val salawatTimeText = String.format(Locale.US, "%02d : %02d : %02d", salHrs, salMins, salSecs)

                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(86.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF063327),
                                                Color(0xFF021B14)
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF10B981), AqsaGold))),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🕌", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "الصلاة على النبي ﷺ",
                                                color = Color(0xFF6EE7B7),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = salawatTimeText,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(0.9f),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("ثانية", color = Color.Gray, fontSize = 9.sp)
                                            Text("دقيقة", color = Color.Gray, fontSize = 9.sp)
                                            Text("ساعة", color = Color.Gray, fontSize = 9.sp)
                                        }
                                    }

                                    // Calligraphy Mini Emblem
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0B4637))
                                            .border(1.dp, Color(0xFF6EE7B7), CircleShape)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "ﷺ\nصلّ عليه",
                                            color = AqsaGoldLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. BOTTOM SECTION: 6 ISLAMIC MIHRAB ARCH CARDS (الفجر، الشروق، الظهر، العصر، المغرب، العشاء)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val activePrayer = nextPrayerInfo?.prayerType ?: PrayerType.ASR

                            // 1. FAJR
                            MihrabArchCard(
                                prayerName = AppStrings.getPrayerName(PrayerType.FAJR, lang, isFriday),
                                icon = "🌙",
                                timeText = prayerResult?.let { formatPrayerTime(it.fajrMillis, viewModel) } ?: "04:39",
                                remainingText = if (activePrayer == PrayerType.FAJR) nextPrayerInfo?.countdownText ?: "05:10:00" else "--:--:--",
                                isActive = activePrayer == PrayerType.FAJR,
                                archColor = Color(0xFF0F172A),
                                borderColor = Color(0xFF38BDF8),
                                iconColor = Color(0xFF7DD3FC)
                            )

                            // 2. SUNRISE (الشروق)
                            MihrabArchCard(
                                prayerName = AppStrings.getPrayerName(PrayerType.SUNRISE, lang, isFriday),
                                icon = "🌅",
                                timeText = prayerResult?.let { formatPrayerTime(it.sunriseMillis, viewModel) } ?: "06:06",
                                remainingText = "--:--:--",
                                isActive = false,
                                archColor = Color(0xFF042F2E),
                                borderColor = Color(0xFF2DD4BF),
                                iconColor = Color(0xFF5EEAD4)
                            )

                            // 3. DHUHR / JUMUAH
                            val isMiddayActive = activePrayer == PrayerType.DHUHR || activePrayer == PrayerType.JUMUAH
                            MihrabArchCard(
                                prayerName = if (isFriday) (if (lang.isRtl) "الجمعة" else "Jumu'ah") else (if (lang.isRtl) "الظهر" else "Dhuhr"),
                                icon = "☀️",
                                timeText = prayerResult?.let { formatPrayerTime(it.dhuhrMillis, viewModel) } ?: "12:46",
                                remainingText = if (isMiddayActive) nextPrayerInfo?.countdownText ?: "01:15:00" else "--:--:--",
                                isActive = isMiddayActive,
                                archColor = Color(0xFF064E3B),
                                borderColor = Color(0xFF34D399),
                                iconColor = Color(0xFF6EE7B7)
                            )

                            // 4. ASR
                            MihrabArchCard(
                                prayerName = AppStrings.getPrayerName(PrayerType.ASR, lang, isFriday),
                                icon = "🌤️",
                                timeText = prayerResult?.let { formatPrayerTime(it.asrMillis, viewModel) } ?: "16:21",
                                remainingText = if (activePrayer == PrayerType.ASR) nextPrayerInfo?.countdownText ?: "01:22:00" else "--:--:--",
                                isActive = activePrayer == PrayerType.ASR,
                                archColor = Color(0xFF451A03),
                                borderColor = AqsaGold,
                                iconColor = Color(0xFFFDE68A)
                            )

                            // 5. MAGHRIB
                            MihrabArchCard(
                                prayerName = AppStrings.getPrayerName(PrayerType.MAGHRIB, lang, isFriday),
                                icon = "🌇",
                                timeText = prayerResult?.let { formatPrayerTime(it.maghribMillis, viewModel) } ?: "18:44",
                                remainingText = if (activePrayer == PrayerType.MAGHRIB) nextPrayerInfo?.countdownText ?: "03:07:00" else "--:--:--",
                                isActive = activePrayer == PrayerType.MAGHRIB,
                                archColor = Color(0xFF450A0A),
                                borderColor = Color(0xFFF87171),
                                iconColor = Color(0xFFFCA5A5)
                            )

                            // 6. ISHA
                            MihrabArchCard(
                                prayerName = AppStrings.getPrayerName(PrayerType.ISHA, lang, isFriday),
                                icon = "🌙",
                                timeText = prayerResult?.let { formatPrayerTime(it.ishaMillis, viewModel) } ?: "20:03",
                                remainingText = if (activePrayer == PrayerType.ISHA) nextPrayerInfo?.countdownText ?: "05:26:00" else "--:--:--",
                                isActive = activePrayer == PrayerType.ISHA,
                                archColor = Color(0xFF1E1B4B),
                                borderColor = Color(0xFFA78BFA),
                                iconColor = Color(0xFFC4B5FD)
                            )
                        }
                    }
                }

                // Action Buttons Below the Board
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Pin Widget to Android Home Screen
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val appWidgetManager = AppWidgetManager.getInstance(context)
                                val provider = ComponentName(context, PrayerAppWidgetProvider::class.java)
                                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                    val pinnedWidgetCallbackIntent = Intent(context, MainActivity::class.java)
                                    val successCallback = PendingIntent.getActivity(
                                        context,
                                        0,
                                        pinnedWidgetCallbackIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    )
                                    appWidgetManager.requestPinAppWidget(provider, null, successCallback)
                                    Toast.makeText(context, if (lang.isRtl) "جارٍ إضافة الودجت للشاشة الرئيسية..." else "Requesting widget addition...", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, if (lang.isRtl) "اضغط مطولاً على شاشة هاتفك الرئيسية لإضافة الودجت" else "Long press on home screen to add widget", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, if (lang.isRtl) "اضغط مطولاً على الشاشة الرئيسية ثم اختر التطبيقات المصغرة (Widgets)" else "Long press home screen to add widget", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AqsaGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_pin_widget")
                    ) {
                        Icon(imageVector = Icons.Default.Widgets, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang.isRtl) "إضافة هذا التطبيق المصغر لشاشة هاتفك 📌" else "Pin This Widget to Home Screen 📌",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Button 2: Update & Apply to Status Bar
                    OutlinedButton(
                        onClick = {
                            PrayerNotificationHelper.updatePersistentNotification(context)
                            Toast.makeText(context, if (lang.isRtl) "تم تطبيق هذا النمط على شريط الإشعارات العلوي ✓" else "Applied to Status Bar Notification ✓", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AqsaGold),
                        border = BorderStroke(1.5.dp, AqsaGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = AqsaGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang.isRtl) "تطبيق المظهر على شريط الإشعارات العلوي" else "Apply to Status Bar Notification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

            } else {
                // =========================================================================
                // TAB 2: NOTIFICATION BAR & WIDGET SETTINGS AND CONTROLS
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Master Toggle
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF06231E)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AqsaGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (isNotifEnabled) AqsaGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isNotifEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (isNotifEnabled) AqsaGold else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang.isRtl) "شريط مواقيت الصلاة الدائم" else "Persistent Notification Bar",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (lang.isRtl) "عرض الصلاة القادمة والمواقيت في أعلى الشاشة باستمرار"
                                    else "Show upcoming prayer and countdown in status bar",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = isNotifEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.settingsRepo.setNotificationBarEnabled(enabled)
                                    if (enabled) {
                                        PrayerNotificationHelper.updatePersistentNotification(context)
                                    } else {
                                        PrayerNotificationHelper.cancelPersistentNotification(context)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AqsaGold,
                                    checkedTrackColor = AqsaGold.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }

                    // Toggles for Content
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF06231E)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = if (lang.isRtl) "خيارات التخصيص" else "Customization Options",
                                color = AqsaGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang.isRtl) "العد التنازلي المباشر للصلاة القادمة" else "Live Next Prayer Countdown",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Switch(
                                    checked = isNotifCountdown,
                                    onCheckedChange = {
                                        viewModel.settingsRepo.setNotificationCountdownEnabled(it)
                                        PrayerNotificationHelper.updatePersistentNotification(context)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = AqsaGold)
                                )
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang.isRtl) "إظهار التاريخ الهجري والموقع" else "Show Hijri Date & Location",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Switch(
                                    checked = isNotifHijri,
                                    onCheckedChange = {
                                        viewModel.settingsRepo.setNotificationHijriEnabled(it)
                                        PrayerNotificationHelper.updatePersistentNotification(context)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = AqsaGold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// Mihrab Arch Card Composable (Faithfully crafted from uploaded screenshot)
@Composable
private fun MihrabArchCard(
    prayerName: String,
    icon: String,
    timeText: String,
    remainingText: String,
    isActive: Boolean,
    archColor: Color,
    borderColor: Color,
    iconColor: Color
) {
    Box(
        modifier = Modifier
            .width(105.dp)
            .height(155.dp)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        archColor.copy(alpha = if (isActive) 0.95f else 0.7f),
                        Color(0xFF030D0B)
                    )
                )
            )
            .border(
                BorderStroke(
                    width = if (isActive) 2.5.dp else 1.2.dp,
                    color = if (isActive) AqsaGold else borderColor.copy(alpha = 0.6f)
                ),
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Icon + Prayer Name
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = icon,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = prayerName,
                    color = if (isActive) AqsaGoldLight else Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
                )
            }

            // Center: Big Prayer Time
            Text(
                text = timeText,
                color = if (isActive) AqsaGold else Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )

            // Bottom: Remaining Time Pill
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "المتبقي",
                    color = if (isActive) AqsaGoldLight.copy(alpha = 0.8f) else Color.Gray,
                    fontSize = 9.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) AqsaGold.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.4f))
                        .border(
                            0.8.dp,
                            if (isActive) AqsaGold else borderColor.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = remainingText,
                        color = if (isActive) AqsaGoldLight else Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (isSelected) AqsaGold else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else Color.LightGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (isSelected) Color.Black else Color.LightGray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

private fun formatPrayerTime(ms: Long, viewModel: PrayerViewModel): String {
    val tz = TimeZone.getTimeZone(viewModel.settingsRepo.getEffectiveTimeZoneId())
    val cal = Calendar.getInstance(tz).apply { timeInMillis = ms }
    val sdf = SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = tz }
    return sdf.format(cal.time)
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.Language
import com.example.data.model.PrayerType
import com.example.data.model.TimeFormatPreference
import com.example.ui.components.*
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel
import java.util.Calendar

@Composable
fun MainMosqueClockScreen(
    viewModel: PrayerViewModel,
    onOpenMenu: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenAzan: () -> Unit,
    onOpenRamadan: () -> Unit,
    onOpenSalawat: () -> Unit,
    onSwitchToMosqueClockView: () -> Unit
) {
    val lang by viewModel.settingsRepo.language.collectAsState()
    val location by viewModel.settingsRepo.selectedLocation.collectAsState()
    val timeFormat by viewModel.settingsRepo.timeFormat.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val nextPrayerInfo by viewModel.nextPrayerInfo.collectAsState()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsState()
    val alertsList by viewModel.alertsList.collectAsState()
    val salawatConfig by viewModel.settingsRepo.salawatConfig.collectAsState()
    val salawatRemainingSeconds by viewModel.salawatRemainingSeconds.collectAsState()

    val (hours, minutes, seconds) = remember(currentTimeMillis, timeFormat) {
        viewModel.formatClockTime(currentTimeMillis, timeFormat)
    }
    val amPm = remember(currentTimeMillis, lang) {
        viewModel.getAmPmIndicator(currentTimeMillis, lang)
    }

    val locationName = remember(location, lang) {
        when (lang) {
            Language.ARABIC -> location.nameAr
            Language.ENGLISH -> location.nameEn
            Language.FRENCH -> location.nameFr
        }
    }

    val countryName = remember(location, lang) {
        when (lang) {
            Language.ARABIC -> location.countryAr
            Language.ENGLISH -> location.countryEn
            Language.FRENCH -> location.countryFr
        }
    }

    val dayOfWeekName = remember(prayerTimes, lang) {
        prayerTimes?.getDayOfWeek(lang) ?: ""
    }

    val hijriDateText = remember(prayerTimes, lang, dayOfWeekName) {
        val hDate = prayerTimes?.getHijriDateText(lang) ?: ""
        if (dayOfWeekName.isNotBlank()) "$dayOfWeekName، $hDate" else hDate
    }

    val gregorianDateText = remember(prayerTimes) {
        prayerTimes?.gregorianDateText ?: ""
    }

    val isFriday = remember(currentTimeMillis) {
        val c = Calendar.getInstance()
        c.timeInMillis = currentTimeMillis
        c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }

    // Salawat countdown breakdown
    val salawatHours = salawatRemainingSeconds / 3600
    val salawatMinutes = (salawatRemainingSeconds % 3600) / 60
    val salawatSecs = salawatRemainingSeconds % 60

    // Next Prayer breakdown
    val remainingMillis = nextPrayerInfo?.remainingMillis ?: 0L
    val npHours = remainingMillis / 3600000L
    val npMinutes = (remainingMillis % 3600000L) / 60000L
    val npSeconds = (remainingMillis % 60000L) / 1000L

    val nextType = nextPrayerInfo?.prayerType ?: PrayerType.DHUHR
    val nextTimeText = remember(prayerTimes, nextType, timeFormat, lang) {
        prayerTimes?.let { pt ->
            val millis = pt.getTimeMillis(nextType)
            viewModel.formatTime(millis, timeFormat, lang)
        } ?: "--:--"
    }

    Scaffold(
        bottomBar = {
            AlAqsaBottomNavigationBar(
                selectedTab = 0,
                lang = lang,
                onTabSelected = { index ->
                    when (index) {
                        0 -> { /* Already on Home */ }
                        1 -> onSwitchToMosqueClockView()
                        2 -> onOpenMenu()
                    }
                }
            )
        },
        containerColor = AqsaDarkBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Section with Al-Aqsa Background Artwork, Location, Clock & Calligraphy
            item {
                AlAqsaHeaderSection(
                    locationName = locationName,
                    countryName = countryName,
                    hours = hours,
                    minutes = minutes,
                    amPm = amPm,
                    hijriDateText = hijriDateText,
                    gregorianDateText = gregorianDateText,
                    lang = lang,
                    onLocationClick = onOpenLocation,
                    onSettingsClick = onOpenMenu
                )
            }

            // 2. Featured Next Prayer Card (with countdown, mosque arch & prayer time seal)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NextPrayerFeaturedCard(
                        prayerType = nextType,
                        prayerTimeText = nextTimeText,
                        countdownHours = npHours,
                        countdownMinutes = npMinutes,
                        countdownSeconds = npSeconds,
                        lang = lang
                    )
                }
            }

            // 3. Horizontal Prayer Times Bar (All 6: الفجر، الشروق، الظهر، العصر، المغرب، العشاء)
            item {
                prayerTimes?.let { pt ->
                    val middayType = if (isFriday) PrayerType.JUMUAH else PrayerType.DHUHR
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PrayerTimesPillBar(
                            fajrTime = viewModel.formatTime(pt.fajrMillis, timeFormat, lang),
                            sunriseTime = viewModel.formatTime(pt.sunriseMillis, timeFormat, lang),
                            dhuhrTime = viewModel.formatTime(pt.dhuhrMillis, timeFormat, lang),
                            asrTime = viewModel.formatTime(pt.asrMillis, timeFormat, lang),
                            maghribTime = viewModel.formatTime(pt.maghribMillis, timeFormat, lang),
                            ishaTime = viewModel.formatTime(pt.ishaMillis, timeFormat, lang),
                            nextPrayerType = nextType,
                            lang = lang
                        )
                    }
                }
            }

            // 4. Salawat Countdown Card (with Jerusalem thumbnail and live countdown)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SalawatCountdownCard(
                        remainingHours = salawatHours,
                        remainingMinutes = salawatMinutes,
                        remainingSeconds = salawatSecs,
                        isEnabled = salawatConfig.isEnabled,
                        lang = lang,
                        onClick = onOpenSalawat
                    )
                }
            }

            // 5. 4 Quick Feature Action Cards (التنبيهات, الأذان, رمضان, الصلاة على النبي)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    QuickFeaturesGrid(
                        alertsCount = alertsList.count { it.isEnabled },
                        lang = lang,
                        onAlertsClick = onOpenAlerts,
                        onAzanClick = onOpenAzan,
                        onRamadanClick = onOpenRamadan,
                        onSalawatClick = onOpenSalawat
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

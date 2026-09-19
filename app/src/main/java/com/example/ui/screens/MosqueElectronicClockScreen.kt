package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.PrayerType
import com.example.data.model.TimeFormatPreference
import com.example.ui.components.*
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosqueElectronicClockScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.settingsRepo.language.collectAsState()
    val location by viewModel.settingsRepo.selectedLocation.collectAsState()
    val timeFormat by viewModel.settingsRepo.timeFormat.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val nextPrayerInfo by viewModel.nextPrayerInfo.collectAsState()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsState()

    val (hours, minutes, seconds) = remember(currentTimeMillis, timeFormat) {
        viewModel.formatClockTime(currentTimeMillis, timeFormat)
    }
    val amPm = remember(currentTimeMillis, lang) {
        viewModel.getAmPmIndicator(currentTimeMillis, lang)
    }

    val cityName = remember(location, lang) {
        when (lang) {
            Language.ARABIC -> location.nameAr
            Language.ENGLISH -> location.nameEn
            Language.FRENCH -> location.nameFr
        }
    }

    val dayOfWeekName = remember(prayerTimes, lang) {
        prayerTimes?.getDayOfWeek(lang) ?: ""
    }

    val hijriDateText = remember(prayerTimes, lang) {
        prayerTimes?.getHijriDateText(lang) ?: ""
    }

    val gregorianDateText = remember(prayerTimes) {
        prayerTimes?.gregorianDateText ?: ""
    }

    val isFriday = remember(currentTimeMillis) {
        val c = Calendar.getInstance()
        c.timeInMillis = currentTimeMillis
        c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang.isRtl) "ساعة المسجد الإلكترونية" else "Mosque Electronic Clock",
                        color = MosqueGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MosqueGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MosqueEmeraldDark)
            )
        },
        containerColor = Color(0xFF021612)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                MosqueHeaderCard(
                    cityName = cityName,
                    dayOfWeek = dayOfWeekName,
                    hijriDateText = hijriDateText,
                    gregorianDateText = gregorianDateText,
                    lang = lang
                )
            }

            item {
                LiveDigitalClockCard(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                    amPm = amPm,
                    is24H = timeFormat == TimeFormatPreference.FORMAT_24H,
                    lang = lang
                )
            }

            item {
                nextPrayerInfo?.let { nextInfo ->
                    NextPrayerBanner(
                        prayerType = nextInfo.prayerType,
                        countdownText = nextInfo.countdownText,
                        lang = lang
                    )
                }
            }

            prayerTimes?.let { times ->
                val nextType = nextPrayerInfo?.prayerType

                item {
                    PrayerRowItem(
                        prayerType = PrayerType.FAJR,
                        timeText = viewModel.formatTime(times.fajrMillis, timeFormat, lang),
                        isNext = nextType == PrayerType.FAJR,
                        lang = lang
                    )
                }

                item {
                    PrayerRowItem(
                        prayerType = PrayerType.SUNRISE,
                        timeText = viewModel.formatTime(times.sunriseMillis, timeFormat, lang),
                        isNext = false,
                        lang = lang
                    )
                }

                item {
                    val middayType = if (isFriday) PrayerType.JUMUAH else PrayerType.DHUHR
                    PrayerRowItem(
                        prayerType = middayType,
                        timeText = viewModel.formatTime(times.dhuhrMillis, timeFormat, lang),
                        isNext = nextType == middayType || nextType == PrayerType.DHUHR,
                        lang = lang
                    )
                }

                item {
                    PrayerRowItem(
                        prayerType = PrayerType.ASR,
                        timeText = viewModel.formatTime(times.asrMillis, timeFormat, lang),
                        isNext = nextType == PrayerType.ASR,
                        lang = lang
                    )
                }

                item {
                    PrayerRowItem(
                        prayerType = PrayerType.MAGHRIB,
                        timeText = viewModel.formatTime(times.maghribMillis, timeFormat, lang),
                        isNext = nextType == PrayerType.MAGHRIB,
                        lang = lang
                    )
                }

                item {
                    PrayerRowItem(
                        prayerType = PrayerType.ISHA,
                        timeText = viewModel.formatTime(times.ishaMillis, timeFormat, lang),
                        isNext = nextType == PrayerType.ISHA,
                        lang = lang
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

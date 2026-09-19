package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalawatPlayMode
import com.example.media.MediaStorageHelper
import com.example.media.SoundHelper
import com.example.media.ZipExtractor
import com.example.ui.components.AudioSelectionModalBottomSheet
import com.example.ui.components.MosqueEmeraldDark
import com.example.ui.components.MosqueEmeraldPanel
import com.example.ui.components.MosqueGold
import com.example.ui.components.MosqueLedCyan
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalawatScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lang by viewModel.settingsRepo.language.collectAsState()
    val salawatConfig by viewModel.settingsRepo.salawatConfig.collectAsState()
    val remainingSeconds by viewModel.salawatRemainingSeconds.collectAsState()

    val countdownText = remember(remainingSeconds) {
        val m = remainingSeconds / 60
        val s = remainingSeconds % 60
        String.format(Locale.US, "%02d:%02d", m, s)
    }

    var showSoundSelector by remember { mutableStateOf(false) }
    var isTestPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            SoundHelper.stopCurrentSound()
        }
    }

    val singleSoundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val saved = MediaStorageHelper.saveUserMediaFile(
                    context = context,
                    sourceUri = uri,
                    folderName = "custom_salawat",
                    filePrefix = "salawat"
                )
                if (saved != null) {
                    viewModel.settingsRepo.saveSalawatConfig(
                        salawatConfig.copy(
                            specificSoundUri = saved.first,
                            specificSoundName = saved.second,
                            playMode = SalawatPlayMode.SPECIFIC
                        )
                    )
                }
            }
        }
    }

    val zipPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val extracted = ZipExtractor.extractAudioFromZip(context, uri, "salawat_audio")
                viewModel.settingsRepo.saveSalawatConfig(
                    salawatConfig.copy(
                        zipArchiveUri = uri.toString(),
                        extractedSounds = extracted
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.tabSalawat(lang),
                        color = MosqueGold,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MosqueGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MosqueEmeraldDark)
            )
        },
        containerColor = Color(0xFF021612)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Countdown Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3028)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ﷺ اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ ﷺ",
                            color = MosqueGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (lang.isRtl) "الوقت المتبقي حتى التذكير القادم:" else "Time until next Salawat reminder:",
                            color = Color(0xFFA7F3D0),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (salawatConfig.isEnabled) countdownText else "--:--",
                            color = if (salawatConfig.isEnabled) MosqueLedCyan else Color.Gray,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 2. Master Switch
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (lang.isRtl) "تفعيل التذكير بالصلاة على النبي ﷺ" else "Enable Salawat Reminders",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (lang.isRtl) "تذكير صوتي دوري طوال الـ 24 ساعة" else "Periodic audio reminders 24/7",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = salawatConfig.isEnabled,
                            onCheckedChange = { isChecked ->
                                val intervalMs = salawatConfig.intervalMinutes * 60_000L
                                viewModel.settingsRepo.saveSalawatConfig(
                                    salawatConfig.copy(
                                        isEnabled = isChecked,
                                        nextReminderTimeMillis = System.currentTimeMillis() + intervalMs
                                    )
                                )
                                viewModel.triggerReschedule()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MosqueGold, checkedTrackColor = Color(0xFF0F766E))
                        )
                    }
                }
            }

            // 3. Interval Duration Picker
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (lang.isRtl) "الفترة الزمنية بين كل تذكير:" else "Interval duration between reminders:",
                            color = MosqueGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 5, 10, 15, 30, 60).forEach { mins ->
                                FilterChip(
                                    selected = salawatConfig.intervalMinutes == mins,
                                    onClick = {
                                        val intervalMs = mins * 60_000L
                                        viewModel.settingsRepo.saveSalawatConfig(
                                            salawatConfig.copy(
                                                intervalMinutes = mins,
                                                nextReminderTimeMillis = System.currentTimeMillis() + intervalMs
                                            )
                                        )
                                        viewModel.triggerReschedule()
                                    },
                                    label = { Text("$mins ${if (lang.isRtl) "د" else "m"}") }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Playback Mode (Sequential, Random, Specific)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (lang.isRtl) "طريقة تشغيل الصوت:" else "Sound Playback Mode:",
                            color = MosqueGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        SalawatPlayMode.values().forEach { mode ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = salawatConfig.playMode == mode,
                                    onClick = {
                                        viewModel.settingsRepo.saveSalawatConfig(salawatConfig.copy(playMode = mode))
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.getSalawatModeName(mode, lang),
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // 5. Audio Customization (Single File or ZIP Archive)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (lang.isRtl) "تخصيص الملفات الصوتية:" else "Audio Customization:",
                            color = MosqueGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Pick single audio (Built-in Audio + Choose from Phone)
                        OutlinedButton(
                            onClick = { showSoundSelector = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MosqueGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                salawatConfig.specificSoundName ?: if (lang.isRtl) "اختر صوت الصلاة على النبي أو التنبيه..." else "Select Audio...",
                                color = Color.White
                            )
                        }

                        if (showSoundSelector) {
                            AudioSelectionModalBottomSheet(
                                title = if (lang.isRtl) "اختر صوت التذكير بالصلاة على النبي" else "Select Reminder Audio",
                                currentSelectedIdOrUri = salawatConfig.specificSoundUri,
                                currentDisplayName = salawatConfig.specificSoundName ?: "",
                                isSalawatCatalog = true,
                                lang = lang,
                                onDismiss = { showSoundSelector = false },
                                onSelectBuiltIn = { item ->
                                    viewModel.settingsRepo.saveSalawatConfig(
                                        salawatConfig.copy(
                                            specificSoundUri = item.id,
                                            specificSoundName = item.getTitle(lang),
                                            playMode = SalawatPlayMode.SPECIFIC
                                        )
                                    )
                                    showSoundSelector = false
                                },
                                onChooseFromDevice = {
                                    showSoundSelector = false
                                    singleSoundPicker.launch("audio/*")
                                }
                            )
                        }

                        // Pick ZIP with multiple clips
                        Button(
                            onClick = { zipPicker.launch("application/zip") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (salawatConfig.extractedSounds.isEmpty())
                                    (if (lang.isRtl) "اختر ملف ZIP يحتوي على عدة أصوات" else "Pick ZIP Archive with Audios")
                                else
                                    (if (lang.isRtl) "تم استخراج ${salawatConfig.extractedSounds.size} ملف صوتي من الـ ZIP" else "${salawatConfig.extractedSounds.size} audio clips extracted from ZIP")
                            )
                        }
                    }
                }
            }

            // 6. Test Play Sound Button
            item {
                Button(
                    onClick = {
                        if (isTestPlaying) {
                            SoundHelper.stopCurrentSound()
                            isTestPlaying = false
                        } else {
                            val soundToPlay = when (salawatConfig.playMode) {
                                SalawatPlayMode.SPECIFIC -> salawatConfig.specificSoundUri
                                else -> salawatConfig.extractedSounds.firstOrNull()
                            }
                            val fallback = SoundHelper.getDefaultAlertPath(context)
                            isTestPlaying = true
                            SoundHelper.playSound(
                                context = context,
                                uriString = soundToPlay,
                                fallbackPath = fallback,
                                isAlarm = false,
                                onCompletion = { isTestPlaying = false }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTestPlaying) Color(0xFFEF4444) else MosqueGold,
                        contentColor = if (isTestPlaying) Color.White else MosqueEmeraldDark
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(
                        imageVector = if (isTestPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTestPlaying) {
                            if (lang.isRtl) "إيقاف الصوت الآن" else "Stop Audio Now"
                        } else {
                            if (lang.isRtl) "تشغيل وتجربة صوت الصلاة على النبي الآن" else "Play Test Salawat Audio Now"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

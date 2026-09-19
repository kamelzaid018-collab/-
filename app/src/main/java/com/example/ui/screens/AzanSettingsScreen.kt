package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.media.MediaStorageHelper
import com.example.media.SoundHelper
import com.example.media.ZipExtractor
import com.example.service.AzanMediaService
import com.example.ui.azan.AzanScreenActivity
import com.example.ui.components.AudioSelectionModalBottomSheet
import com.example.ui.components.MosqueEmeraldDark
import com.example.ui.components.MosqueEmeraldPanel
import com.example.ui.components.MosqueGold
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzanSettingsScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lang by viewModel.settingsRepo.language.collectAsState()

    var selectedPrayerTab by remember { mutableStateOf(PrayerType.FAJR) }
    var currentConfig by remember(selectedPrayerTab) {
        mutableStateOf(viewModel.settingsRepo.getAzanConfig(selectedPrayerTab))
    }

    var showTimeAlertAudioSelector by remember { mutableStateOf(false) }
    var showAzanAudioSelector by remember { mutableStateOf(false) }
    var playingPreviewUri by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            SoundHelper.stopCurrentSound()
        }
    }

    // Pickers - Saves files permanently to internal storage so they never disappear
    val azanSoundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val saved = MediaStorageHelper.saveUserMediaFile(
                    context = context,
                    sourceUri = uri,
                    folderName = "custom_azan",
                    filePrefix = "azan_${selectedPrayerTab.name}"
                )
                if (saved != null) {
                    val updated = currentConfig.copy(
                        azanSoundUri = saved.first,
                        azanSoundName = saved.second
                    )
                    currentConfig = updated
                    viewModel.settingsRepo.saveAzanConfig(updated)
                }
            }
        }
    }

    val timeAlertSoundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val saved = MediaStorageHelper.saveUserMediaFile(
                    context = context,
                    sourceUri = uri,
                    folderName = "custom_alerts",
                    filePrefix = "alert_${selectedPrayerTab.name}"
                )
                if (saved != null) {
                    val updated = currentConfig.copy(
                        timeAlertSoundUri = saved.first,
                        timeAlertSoundName = saved.second
                    )
                    currentConfig = updated
                    viewModel.settingsRepo.saveAzanConfig(updated)
                }
            }
        }
    }

    val duaaVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val saved = MediaStorageHelper.saveUserMediaFile(
                    context = context,
                    sourceUri = uri,
                    folderName = "custom_videos",
                    filePrefix = "duaa_${selectedPrayerTab.name}"
                )
                if (saved != null) {
                    val updated = currentConfig.copy(
                        duaaVideoUri = saved.first,
                        duaaVideoName = saved.second
                    )
                    currentConfig = updated
                    viewModel.settingsRepo.saveAzanConfig(updated)
                }
            }
        }
    }

    val staticImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val saved = MediaStorageHelper.saveUserMediaFile(
                    context = context,
                    sourceUri = uri,
                    folderName = "custom_images",
                    filePrefix = "bg_${selectedPrayerTab.name}"
                )
                if (saved != null) {
                    val updated = currentConfig.copy(
                        staticImageUri = saved.first,
                        screenMode = AzanScreenMode.STATIC
                    )
                    currentConfig = updated
                    viewModel.settingsRepo.saveAzanConfig(updated)
                }
            }
        }
    }

    val zipPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val extracted = ZipExtractor.extractImagesFromZip(context, uri, "azan_images_${selectedPrayerTab.name}")
                val updated = currentConfig.copy(
                    zipArchiveUri = uri.toString(),
                    extractedImages = extracted,
                    screenMode = AzanScreenMode.SLIDESHOW
                )
                currentConfig = updated
                viewModel.settingsRepo.saveAzanConfig(updated)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.tabAzan(lang),
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
            // Prayer Selection Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = listOf(
                        PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR,
                        PrayerType.MAGHRIB, PrayerType.ISHA, PrayerType.JUMUAH
                    ).indexOf(selectedPrayerTab),
                    containerColor = MosqueEmeraldPanel,
                    contentColor = MosqueGold,
                    edgePadding = 8.dp
                ) {
                    listOf(
                        PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR,
                        PrayerType.MAGHRIB, PrayerType.ISHA, PrayerType.JUMUAH
                    ).forEach { p ->
                        Tab(
                            selected = selectedPrayerTab == p,
                            onClick = {
                                selectedPrayerTab = p
                                currentConfig = viewModel.settingsRepo.getAzanConfig(p)
                            },
                            text = { Text(AppStrings.getPrayerName(p, lang), fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Apply To All Prayers button
            item {
                OutlinedButton(
                    onClick = {
                        listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA, PrayerType.JUMUAH).forEach { pt ->
                            viewModel.settingsRepo.saveAzanConfig(currentConfig.copy(prayerType = pt))
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (lang.isRtl) "تطبيق هذه الإعدادات على جميع الصلوات" else "Apply Settings to All Prayers")
                }
            }

            // 1. Mandatory Sequence: Time Alert then Azan
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (lang.isRtl) "الترتيب الإجباري: التنبيه بالوقت -> الأذان مباشرة" else "Sequence: Time Alert -> Azan Immediately",
                            color = MosqueGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Time Alert Sound Picker (Built-in Audio + Choose from Phone)
                        Text(
                            text = if (lang.isRtl) "١. صوت التنبيه بالوقت (قبل الأذان مباشرة):" else "1. Time Alert Sound:",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showTimeAlertAudioSelector = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = MosqueGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentConfig.timeAlertSoundName ?: if (lang.isRtl) "اختر صوت التنبيه..." else "Select Time Alert Tone...",
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                            val isTimeAlertPlaying = playingPreviewUri == "time_alert_${selectedPrayerTab.name}"
                            IconButton(
                                onClick = {
                                    val key = "time_alert_${selectedPrayerTab.name}"
                                    if (isTimeAlertPlaying) {
                                        SoundHelper.stopCurrentSound()
                                        playingPreviewUri = null
                                    } else {
                                        playingPreviewUri = key
                                        val fallback = SoundHelper.getDefaultTimeAlertPath(context)
                                        SoundHelper.playSound(
                                            context = context,
                                            uriString = currentConfig.timeAlertSoundUri,
                                            fallbackPath = fallback,
                                            isAlarm = false,
                                            onCompletion = {
                                                if (playingPreviewUri == key) {
                                                    playingPreviewUri = null
                                                }
                                            }
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isTimeAlertPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Preview Time Alert",
                                    tint = if (isTimeAlertPlaying) Color(0xFFEF4444) else MosqueGold
                                )
                            }
                        }

                        if (showTimeAlertAudioSelector) {
                            AudioSelectionModalBottomSheet(
                                title = if (lang.isRtl) "اختر صوت التنبيه بالوقت" else "Select Time Alert Sound",
                                currentSelectedIdOrUri = currentConfig.timeAlertSoundUri,
                                currentDisplayName = currentConfig.timeAlertSoundName ?: "",
                                isAzanCatalog = false,
                                lang = lang,
                                onDismiss = { showTimeAlertAudioSelector = false },
                                onSelectBuiltIn = { item ->
                                    val updated = currentConfig.copy(
                                        timeAlertSoundUri = item.id,
                                        timeAlertSoundName = item.getTitle(lang)
                                    )
                                    currentConfig = updated
                                    viewModel.settingsRepo.saveAzanConfig(updated)
                                    showTimeAlertAudioSelector = false
                                },
                                onChooseFromDevice = {
                                    showTimeAlertAudioSelector = false
                                    timeAlertSoundPicker.launch("audio/*")
                                }
                            )
                        }

                        // Azan Sound Picker (Built-in Audio + Choose from Phone)
                        Text(
                            text = if (lang.isRtl) "٢. صوت الأذان:" else "2. Azan Sound:",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAzanAudioSelector = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = MosqueGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentConfig.azanSoundName ?: if (lang.isRtl) "اختر صوت الأذان..." else "Select Azan Sound...",
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                            val isAzanPlaying = playingPreviewUri == "azan_${selectedPrayerTab.name}"
                            IconButton(
                                onClick = {
                                    val key = "azan_${selectedPrayerTab.name}"
                                    if (isAzanPlaying) {
                                        SoundHelper.stopCurrentSound()
                                        playingPreviewUri = null
                                    } else {
                                        playingPreviewUri = key
                                        val fallback = SoundHelper.getDefaultAzanPath(context)
                                        SoundHelper.playSound(
                                            context = context,
                                            uriString = currentConfig.azanSoundUri,
                                            fallbackPath = fallback,
                                            isAlarm = false,
                                            onCompletion = {
                                                if (playingPreviewUri == key) {
                                                    playingPreviewUri = null
                                                }
                                            }
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isAzanPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Preview Azan",
                                    tint = if (isAzanPlaying) Color(0xFFEF4444) else MosqueGold
                                )
                            }
                        }

                        if (showAzanAudioSelector) {
                            AudioSelectionModalBottomSheet(
                                title = if (lang.isRtl) "اختر صوت الأذان" else "Select Azan Sound",
                                currentSelectedIdOrUri = currentConfig.azanSoundUri,
                                currentDisplayName = currentConfig.azanSoundName ?: "",
                                isAzanCatalog = true,
                                lang = lang,
                                onDismiss = { showAzanAudioSelector = false },
                                onSelectBuiltIn = { item ->
                                    val updated = currentConfig.copy(
                                        azanSoundUri = item.id,
                                        azanSoundName = item.getTitle(lang)
                                    )
                                    currentConfig = updated
                                    viewModel.settingsRepo.saveAzanConfig(updated)
                                    showAzanAudioSelector = false
                                },
                                onChooseFromDevice = {
                                    showAzanAudioSelector = false
                                    azanSoundPicker.launch("audio/*")
                                }
                            )
                        }
                    }
                }
            }

            // 2. Post-Azan Duaa Video
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (lang.isRtl) "دعاء بعد الأذان (فيديو)" else "Post-Azan Duaa (Video)",
                            color = MosqueGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (lang.isRtl) "يتم تشغيل الفيديو تلقائياً بعد انتهاء صوت الأذان مباشرة." else "Video plays automatically right after Azan completes.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        OutlinedButton(
                            onClick = { duaaVideoPicker.launch("video/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(currentConfig.duaaVideoName ?: if (lang.isRtl) "اختر فيديو الدعاء من هاتفك" else "Pick Duaa Video from device")
                        }
                    }
                }
            }

            // 3. Azan Screen Background Images & Slideshow
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (lang.isRtl) "صور شاشة الأذان وشاشة القفل" else "Azan Screen & Lockscreen Background",
                            color = MosqueGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Mode Selector (Slideshow vs Static)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = currentConfig.screenMode == AzanScreenMode.SLIDESHOW,
                                onClick = {
                                    val updated = currentConfig.copy(screenMode = AzanScreenMode.SLIDESHOW)
                                    currentConfig = updated
                                    viewModel.settingsRepo.saveAzanConfig(updated)
                                },
                                label = { Text(if (lang.isRtl) "عرض متتابع (Slideshow)" else "Slideshow") }
                            )
                            FilterChip(
                                selected = currentConfig.screenMode == AzanScreenMode.STATIC,
                                onClick = {
                                    val updated = currentConfig.copy(screenMode = AzanScreenMode.STATIC)
                                    currentConfig = updated
                                    viewModel.settingsRepo.saveAzanConfig(updated)
                                },
                                label = { Text(if (lang.isRtl) "صورة ثابتة" else "Static Image") }
                            )
                        }

                        if (currentConfig.screenMode == AzanScreenMode.SLIDESHOW) {
                            // Pick ZIP file
                            Button(
                                onClick = { zipPicker.launch("application/zip") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Archive, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (currentConfig.extractedImages.isEmpty())
                                        (if (lang.isRtl) "اختر ملف ZIP يحتوي على الصور" else "Pick ZIP Archive with Images")
                                    else
                                        (if (lang.isRtl) "تم استخراج ${currentConfig.extractedImages.size} صورة من الـ ZIP" else "${currentConfig.extractedImages.size} images extracted from ZIP")
                                )
                            }

                            // Duration Picker: 3, 5, 10, 15 seconds
                            Text(
                                text = if (lang.isRtl) "مدة ظهور كل صورة في العرض المتتابع:" else "Duration per image:",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(3, 5, 10, 15).forEach { dur ->
                                    FilterChip(
                                        selected = currentConfig.slideshowDurationSeconds == dur,
                                        onClick = {
                                            val updated = currentConfig.copy(slideshowDurationSeconds = dur)
                                            currentConfig = updated
                                            viewModel.settingsRepo.saveAzanConfig(updated)
                                        },
                                        label = { Text("$dur ${if (lang.isRtl) "ثواني" else "sec"}") }
                                    )
                                }
                            }
                        } else {
                            // Static Image Picker
                            Button(
                                onClick = { staticImagePicker.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (currentConfig.staticImageUri == null) (if (lang.isRtl) "اختر صورة ثابتة" else "Pick Static Image") else (if (lang.isRtl) "تم اختيار صورة ثابتة" else "Static Image Selected"))
                            }
                        }
                    }
                }
            }

            // 4. Test Azan Screen & Audio Button
            item {
                Button(
                    onClick = {
                        // Start service test
                        AzanMediaService.start(context, selectedPrayerTab)
                        // Launch test screen
                        val screenIntent = Intent(context, AzanScreenActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra(AzanScreenActivity.EXTRA_PRAYER_TYPE, selectedPrayerTab.name)
                        }
                        context.startActivity(screenIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGold, contentColor = MosqueEmeraldDark),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("test_azan_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang.isRtl) "معاينة شاشة وتشغيل أذان ${AppStrings.getPrayerName(selectedPrayerTab, lang)}" else "Preview & Test Azan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

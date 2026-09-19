package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.example.data.model.AlertItem
import com.example.data.model.Language
import com.example.data.model.PrayerType
import com.example.media.MediaStorageHelper
import com.example.media.SoundHelper
import com.example.ui.alerts.PrePrayerAlertActivity
import com.example.ui.components.AudioSelectionModalBottomSheet
import com.example.ui.components.MosqueEmeraldDark
import com.example.ui.components.MosqueEmeraldPanel
import com.example.ui.components.MosqueGold
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.settingsRepo.language.collectAsState()
    val alerts by viewModel.alertsList.collectAsState()
    val nextPrayerInfo by viewModel.nextPrayerInfo.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlert by remember { mutableStateOf<AlertItem?>(null) }
    var playingAlertId by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            SoundHelper.stopCurrentSound()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.tabAlerts(lang),
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingAlert = null
                    showAddDialog = true
                },
                containerColor = MosqueGold,
                contentColor = MosqueEmeraldDark,
                modifier = Modifier.testTag("add_alert_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alert")
            }
        },
        containerColor = Color(0xFF031411)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Info Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (lang.isRtl) "تنبيهات ما قبل الأذان" else "Pre-Azan Alerts",
                            color = MosqueGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang.isRtl)
                                "يقوم التطبيق بإصدار تنبيهات مخصصة قبل موعد الصلاة بالصيغة الإلزامية: \"يتبقى [x] دقائق على أذان [اسم الصلاة]\". يمكنك تحديد الصلاة المستهدفة (الكل أو فجر، ظهر، عصر، مغرب، عشاء، جمعة) وأيام التكرار (الكل أو كل يوم على حدة)."
                            else
                                "Configurable alerts before prayer with custom sound and mandatory phrasing. Choose target prayer (All, Fajr, Dhuhr, Asr, Maghrib, Isha, Jumuah) and repeat days.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (alerts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang.isRtl) "لا توجد تنبيهات مضافة حالياً. اضغط + لإضافة تنبيه جديد." else "No alerts configured. Tap + to add one.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(alerts, key = { it.id }) { alert ->
                    val isPlaying = playingAlertId == alert.id
                    AlertItemCard(
                        alert = alert,
                        lang = lang,
                        isPlaying = isPlaying,
                        onTogglePreview = {
                            if (isPlaying) {
                                SoundHelper.stopCurrentSound()
                                playingAlertId = null
                            } else {
                                playingAlertId = alert.id
                                val fallback = SoundHelper.getDefaultAlertPath(context)
                                SoundHelper.playSound(
                                    context = context,
                                    uriString = alert.soundUri,
                                    fallbackPath = fallback,
                                    isAlarm = false,
                                    onCompletion = {
                                        if (playingAlertId == alert.id) {
                                            playingAlertId = null
                                        }
                                    }
                                )
                            }
                        },
                        onToggle = { isEnabled ->
                            viewModel.toggleAlertEnabled(alert, isEnabled)
                        },
                        onEdit = {
                            editingAlert = alert
                            showAddDialog = true
                        },
                        onDelete = {
                            if (playingAlertId == alert.id) {
                                SoundHelper.stopCurrentSound()
                                playingAlertId = null
                            }
                            viewModel.deleteAlert(alert)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showAddDialog) {
        AlertEditDialog(
            existingAlert = editingAlert,
            lang = lang,
            onDismiss = { showAddDialog = false },
            onSave = { savedAlert ->
                if (savedAlert.id == 0L) {
                    viewModel.insertAlert(savedAlert)
                } else {
                    viewModel.updateAlert(savedAlert)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AlertItemCard(
    alert: AlertItem,
    lang: Language,
    isPlaying: Boolean = false,
    onTogglePreview: () -> Unit = {},
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayFormat = AppStrings.formatAlertMessage(alert.minutesBefore, alert.targetPrayer, lang)
    val repeatText = AppStrings.formatRepeatDays(alert.repeatDays, lang)

    Card(
        colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayFormat,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🗓️ $repeatText",
                        color = Color(0xFFA7F3D0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onTogglePreview() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Stop" else "Play",
                            tint = if (isPlaying) Color(0xFFEF4444) else MosqueGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = alert.soundName,
                            color = MosqueGold,
                            fontSize = 12.sp,
                            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePreview) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Stop" else "Play",
                            tint = if (isPlaying) Color(0xFFEF4444) else MosqueGold
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFA7F3D0))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF87171))
                    }
                    Switch(
                        checked = alert.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MosqueGold,
                            checkedTrackColor = Color(0xFF0F766E)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertEditDialog(
    existingAlert: AlertItem?,
    lang: Language,
    onDismiss: () -> Unit,
    onSave: (AlertItem) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(existingAlert?.title ?: "") }
    var targetPrayer by remember { mutableStateOf(existingAlert?.targetPrayer ?: PrayerType.ALL) }
    var minutesBefore by remember { mutableStateOf(existingAlert?.minutesBefore ?: 15) }
    var soundUri by remember {
        mutableStateOf(existingAlert?.soundUri ?: "builtin_alert_default")
    }
    var soundName by remember {
        mutableStateOf(
            existingAlert?.soundName ?: (if (lang.isRtl) "نغمة تنبيه اقتراب الصلاة (افتراضي)" else "Pre-Prayer Alert Tone (Default)")
        )
    }
    var showSoundSelector by remember { mutableStateOf(false) }

    // Repeat Days State: Set of Calendar.DAY_OF_WEEK integers (1=Sunday, ..., 7=Saturday)
    val initialDays = remember(existingAlert) {
        if (existingAlert == null || existingAlert.repeatDays.isBlank()) {
            setOf(1, 2, 3, 4, 5, 6, 7)
        } else {
            existingAlert.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }
    }
    var selectedDays by remember { mutableStateOf(initialDays) }

    val daysOrder = listOf(
        Calendar.SATURDAY,
        Calendar.SUNDAY,
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY
    )

    val isAllDaysSelected = selectedDays.size == 7

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = MediaStorageHelper.saveUserMediaFile(
                context = context,
                sourceUri = uri,
                folderName = "custom_pre_alerts",
                filePrefix = "alert"
            )
            if (saved != null) {
                soundUri = saved.first
                soundName = saved.second
            }
        }
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingAlert == null) (if (lang.isRtl) "إضافة تنبيه جديد" else "Add New Alert")
                       else (if (lang.isRtl) "تعديل التنبيه" else "Edit Alert"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Target Prayer Section
                Column {
                    Text(
                        text = if (lang.isRtl) "الصلاة المستهدفة:" else "Target Prayer:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 1: الكل, الفجر, الظهر, العصر
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(PrayerType.ALL, PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR).forEach { pt ->
                            val label = if (pt == PrayerType.ALL) (if (lang.isRtl) "الكل" else "All") else AppStrings.getPrayerName(pt, lang)
                            FilterChip(
                                selected = targetPrayer == pt,
                                onClick = { targetPrayer = pt },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row 2: المغرب, العشاء, الجمعة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(PrayerType.MAGHRIB, PrayerType.ISHA, PrayerType.JUMUAH).forEach { pt ->
                            val label = AppStrings.getPrayerName(pt, lang)
                            FilterChip(
                                selected = targetPrayer == pt,
                                onClick = { targetPrayer = pt },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))

                // 2. Repeat Days Section
                Column {
                    Text(
                        text = if (lang.isRtl) "أيام التكرار:" else "Repeat Days:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // "الكل" (All Days) Button Chip
                    FilterChip(
                        selected = isAllDaysSelected,
                        onClick = {
                            selectedDays = if (isAllDaysSelected) {
                                emptySet()
                            } else {
                                setOf(1, 2, 3, 4, 5, 6, 7)
                            }
                        },
                        label = {
                            Text(
                                text = if (lang.isRtl) "الكل (يومياً / كل الأيام)" else "All (Daily / Every day)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (lang.isRtl) "أو حدد كل يوم على حدة:" else "Or select each day individually:",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Days Row 1: السبت, الأحد, الإثنين, الثلاثاء
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        daysOrder.take(4).forEach { dayInt ->
                            val isSelected = selectedDays.contains(dayInt)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayInt
                                    } else {
                                        selectedDays + dayInt
                                    }
                                },
                                label = {
                                    Text(
                                        text = AppStrings.getDayName(dayInt, lang),
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Days Row 2: الأربعاء, الخميس, الجمعة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        daysOrder.drop(4).forEach { dayInt ->
                            val isSelected = selectedDays.contains(dayInt)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayInt
                                    } else {
                                        selectedDays + dayInt
                                    }
                                },
                                label = {
                                    Text(
                                        text = AppStrings.getDayName(dayInt, lang),
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))

                // 3. Minutes Before Azan
                Column {
                    Text(
                        text = if (lang.isRtl) "وقت التنبيه قبل الأذان: $minutesBefore دقيقة" else "Alert time: $minutesBefore minutes before",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = minutesBefore.toFloat(),
                        onValueChange = { minutesBefore = it.toInt() },
                        valueRange = 1f..60f,
                        steps = 59
                    )
                }

                // 4. Sound Picker (Built-in Audio + Choose from Phone)
                var isDialogSoundPlaying by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSoundSelector = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = MosqueGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔔 $soundName",
                            maxLines = 1,
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = {
                            if (isDialogSoundPlaying) {
                                SoundHelper.stopCurrentSound()
                                isDialogSoundPlaying = false
                            } else {
                                isDialogSoundPlaying = true
                                val fallback = SoundHelper.getDefaultAlertPath(context)
                                SoundHelper.playSound(
                                    context = context,
                                    uriString = soundUri,
                                    fallbackPath = fallback,
                                    isAlarm = false,
                                    onCompletion = { isDialogSoundPlaying = false }
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isDialogSoundPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Preview sound",
                            tint = if (isDialogSoundPlaying) Color(0xFFEF4444) else MosqueGold
                        )
                    }
                }

                if (showSoundSelector) {
                    AudioSelectionModalBottomSheet(
                        title = if (lang.isRtl) "اختر نغمة التنبيه" else "Select Alert Sound",
                        currentSelectedIdOrUri = soundUri,
                        currentDisplayName = soundName,
                        isAzanCatalog = false,
                        lang = lang,
                        onDismiss = { showSoundSelector = false },
                        onSelectBuiltIn = { item ->
                            soundUri = item.id
                            soundName = item.getTitle(lang)
                            showSoundSelector = false
                        },
                        onChooseFromDevice = {
                            showSoundSelector = false
                            soundPickerLauncher.launch("audio/*")
                        }
                    )
                }

                // 5. Required Format preview in dialog
                val previewPrayer = if (targetPrayer == PrayerType.ALL) PrayerType.ASR else targetPrayer
                val previewMsg = AppStrings.formatAlertMessage(minutesBefore, previewPrayer, lang)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF021B16)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (lang.isRtl) "معاينة نص التنبيه الإلزامي:" else "Mandatory Alert Preview:",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = previewMsg,
                            color = MosqueGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Preview Full Alert Screen Button
                OutlinedButton(
                    onClick = {
                        PrePrayerAlertActivity.start(
                            context = context,
                            prayerType = previewPrayer,
                            minutesBefore = minutesBefore,
                            soundUri = soundUri
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MosqueGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lang.isRtl) "معاينة شاشة اقتراب الصلاة (تظل مضاءة)" else "Preview Alert Screen (Stays Awake)",
                        color = MosqueGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val daysString = if (selectedDays.isEmpty()) "1,2,3,4,5,6,7" else selectedDays.sorted().joinToString(",")
                    val alertToSave = existingAlert?.copy(
                        title = title.ifBlank { "Alert" },
                        targetPrayer = targetPrayer,
                        minutesBefore = minutesBefore,
                        repeatDays = daysString,
                        soundUri = soundUri,
                        soundName = soundName
                    ) ?: AlertItem(
                        title = title.ifBlank { "Alert" },
                        targetPrayer = targetPrayer,
                        minutesBefore = minutesBefore,
                        repeatDays = daysString,
                        soundUri = soundUri,
                        soundName = soundName,
                        isEnabled = true
                    )
                    onSave(alertToSave)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MosqueGold, contentColor = MosqueEmeraldDark)
            ) {
                Text(if (lang.isRtl) "حفظ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (lang.isRtl) "إلغاء" else "Cancel")
            }
        }
    )
}

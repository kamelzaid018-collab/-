package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
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
import com.example.media.MediaStorageHelper
import com.example.ui.azan.AzanScreenActivity
import com.example.ui.components.MosqueEmeraldDark
import com.example.ui.components.MosqueEmeraldPanel
import com.example.ui.components.MosqueGold
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamadanScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lang by viewModel.settingsRepo.language.collectAsState()
    val ramadanConfig by viewModel.settingsRepo.ramadanConfig.collectAsState()

    val iftarVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val saved = MediaStorageHelper.saveUserMediaFile(
                    context = context,
                    sourceUri = uri,
                    folderName = "custom_ramadan",
                    filePrefix = "iftar"
                )
                if (saved != null) {
                    viewModel.settingsRepo.saveRamadanConfig(
                        ramadanConfig.copy(
                            iftarCannonVideoUri = saved.first,
                            iftarCannonVideoName = saved.second
                        )
                    )
                }
            }
        }
    }

    val musaharatiVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val saved = MediaStorageHelper.saveUserMediaFile(
                    context = context,
                    sourceUri = uri,
                    folderName = "custom_ramadan",
                    filePrefix = "musaharati"
                )
                if (saved != null) {
                    viewModel.settingsRepo.saveRamadanConfig(
                        ramadanConfig.copy(
                            musaharatiVideoUri = saved.first,
                            musaharatiVideoName = saved.second
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.tabRamadan(lang),
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
            // 1. Ramadan Mode Master Switch
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
                                text = if (lang.isRtl) "وضع رمضان المبارك" else "Holy Ramadan Mode",
                                color = MosqueGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (lang.isRtl) "تفعيل ميزات مدفع الإفطار والمسحراتي" else "Enable Iftar Cannon & Musaharati features",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = ramadanConfig.isRamadanMode,
                            onCheckedChange = { isEnabled ->
                                viewModel.settingsRepo.saveRamadanConfig(ramadanConfig.copy(isRamadanMode = isEnabled))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MosqueGold, checkedTrackColor = Color(0xFF0F766E))
                        )
                    }
                }
            }

            // 2. Iftar Cannon Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang.isRtl) "💥 مدفع الإفطار" else "💥 Iftar Cannon",
                                color = MosqueGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = ramadanConfig.isIftarCannonEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.settingsRepo.saveRamadanConfig(ramadanConfig.copy(isIftarCannonEnabled = isChecked))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MosqueGold, checkedTrackColor = Color(0xFF0F766E))
                            )
                        }

                        Text(
                            text = if (lang.isRtl) "يعمل الفيديو تلقائياً قبل أذان المغرب مباشرة في رمضان." else "Video plays automatically right before Maghrib Azan in Ramadan.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        OutlinedButton(
                            onClick = { iftarVideoPicker.launch("video/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(ramadanConfig.iftarCannonVideoName ?: if (lang.isRtl) "اختر فيديو مدفع الإفطار من هاتفك" else "Pick Cannon Video")
                        }

                        if (!ramadanConfig.iftarCannonVideoUri.isNullOrBlank()) {
                            Button(
                                onClick = {
                                    val intent = Intent(context, AzanScreenActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        putExtra(AzanScreenActivity.EXTRA_PLAY_VIDEO_NOW, true)
                                        putExtra(AzanScreenActivity.EXTRA_VIDEO_URI, ramadanConfig.iftarCannonVideoUri)
                                        putExtra(AzanScreenActivity.EXTRA_TITLE_OVERRIDE, if (lang.isRtl) "مدفع الإفطار" else "Iftar Cannon")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (lang.isRtl) "معاينة تشغيل فيديو مدفع الإفطار" else "Preview Cannon Video")
                            }
                        }
                    }
                }
            }

            // 3. Musaharati (Suhoor) Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang.isRtl) "🥁 المسحراتي (تنبيه السحور)" else "🥁 Musaharati (Suhoor Alert)",
                                color = MosqueGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = ramadanConfig.isMusaharatiEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.settingsRepo.saveRamadanConfig(ramadanConfig.copy(isMusaharatiEnabled = isChecked))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MosqueGold, checkedTrackColor = Color(0xFF0F766E))
                            )
                        }

                        // Pick Musaharati Video
                        OutlinedButton(
                            onClick = { musaharatiVideoPicker.launch("video/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(ramadanConfig.musaharatiVideoName ?: if (lang.isRtl) "اختر فيديو المسحراتي من هاتفك" else "Pick Musaharati Video")
                        }

                        // Timing Mode: Fixed Time or Minutes Before Fajr
                        Text(
                            text = if (lang.isRtl) "تحديد موعد تنبيه المسحراتي:" else "Suhoor Alert Timing:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !ramadanConfig.musaharatiIsFixedTime,
                                onClick = {
                                    viewModel.settingsRepo.saveRamadanConfig(ramadanConfig.copy(musaharatiIsFixedTime = false))
                                },
                                label = { Text(if (lang.isRtl) "قبل أذان الفجر" else "Before Fajr") }
                            )
                            FilterChip(
                                selected = ramadanConfig.musaharatiIsFixedTime,
                                onClick = {
                                    viewModel.settingsRepo.saveRamadanConfig(ramadanConfig.copy(musaharatiIsFixedTime = true))
                                },
                                label = { Text(if (lang.isRtl) "ساعة ثابتة" else "Fixed Clock Time") }
                            )
                        }

                        if (!ramadanConfig.musaharatiIsFixedTime) {
                            Text(
                                text = if (lang.isRtl) "التنبيه قبل الفجر بـ: ${ramadanConfig.musaharatiMinutesBeforeFajr} دقيقة" else "Alert: ${ramadanConfig.musaharatiMinutesBeforeFajr} min before Fajr",
                                color = MosqueGold,
                                fontSize = 13.sp
                            )
                            Slider(
                                value = ramadanConfig.musaharatiMinutesBeforeFajr.toFloat(),
                                onValueChange = {
                                    viewModel.settingsRepo.saveRamadanConfig(ramadanConfig.copy(musaharatiMinutesBeforeFajr = it.toInt()))
                                },
                                valueRange = 15f..120f,
                                steps = 20
                            )
                        } else {
                            val timeStr = "%02d:%02d".format(ramadanConfig.musaharatiFixedHour, ramadanConfig.musaharatiFixedMinute)
                            OutlinedButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            viewModel.settingsRepo.saveRamadanConfig(
                                                ramadanConfig.copy(
                                                    musaharatiFixedHour = hourOfDay,
                                                    musaharatiFixedMinute = minute
                                                )
                                            )
                                        },
                                        ramadanConfig.musaharatiFixedHour,
                                        ramadanConfig.musaharatiFixedMinute,
                                        false
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (lang.isRtl) "الساعة المحددة: $timeStr" else "Fixed Time: $timeStr")
                            }
                        }

                        if (!ramadanConfig.musaharatiVideoUri.isNullOrBlank()) {
                            Button(
                                onClick = {
                                    val intent = Intent(context, AzanScreenActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        putExtra(AzanScreenActivity.EXTRA_PLAY_VIDEO_NOW, true)
                                        putExtra(AzanScreenActivity.EXTRA_VIDEO_URI, ramadanConfig.musaharatiVideoUri)
                                        putExtra(AzanScreenActivity.EXTRA_TITLE_OVERRIDE, if (lang.isRtl) "المسحراتي" else "Musaharati")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (lang.isRtl) "معاينة تشغيل فيديو المسحراتي" else "Preview Musaharati Video")
                            }
                        }
                    }
                }
            }
        }
    }
}

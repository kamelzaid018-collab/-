package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.components.MosqueEmeraldDark
import com.example.ui.components.MosqueEmeraldPanel
import com.example.ui.components.MosqueGold
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.settingsRepo.language.collectAsState()

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.tabPermissions(lang),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = if (lang.isRtl) "لضمان تشغيل الأذان والتنبيهات بدقة في موعدها حتى لو كان التطبيق مغلقاً أو الهاتف في وضع السكون:"
                           else "To ensure Azan and alarms trigger accurately even when the app is closed or sleeping:",
                    color = Color(0xFFA7F3D0),
                    fontSize = 14.sp
                )
            }

            // 1. Exact Alarm
            item {
                PermissionCard(
                    icon = Icons.Default.Schedule,
                    title = if (lang.isRtl) "تنبيهات دقيقة (Exact Alarms)" else "Exact Alarms Permission",
                    desc = if (lang.isRtl) "مطلوب لإطلاق الأذان في الثانية المحددة بالضبط" else "Required to trigger Azan at the exact second",
                    buttonText = if (lang.isRtl) "إعداد التنبيه الدقيق" else "Configure Alarms",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                )
            }

            // 2. Notifications
            item {
                PermissionCard(
                    icon = Icons.Default.Notifications,
                    title = if (lang.isRtl) "إذن الإشعارات" else "Notifications Permission",
                    desc = if (lang.isRtl) "لعرض إشعارات مواقيت الصلاة وشاشة القفل" else "To show prayer times and lockscreen notices",
                    buttonText = if (lang.isRtl) "طلب الإذن" else "Grant Permission",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }

            // 3. Battery Optimization Exemption
            item {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                val isIgnoring = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
                } else true

                PermissionCard(
                    icon = Icons.Default.BatteryAlert,
                    title = if (lang.isRtl) "استثناء تحسين البطارية" else "Ignore Battery Optimization",
                    desc = if (lang.isRtl) "يمنع نظام أندرويد من إيقاف الأذان في الخلفية" else "Prevents Android from killing background Azans",
                    buttonText = if (isIgnoring) (if (lang.isRtl) "مُفعل بالفعل ✓" else "Already Active ✓") else (if (lang.isRtl) "تعطيل تحسين البطارية" else "Disable Optimization"),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                            }
                        }
                    }
                )
            }

            // 4. Draw Over Other Apps (Lockscreen overlay)
            item {
                PermissionCard(
                    icon = Icons.Default.Lock,
                    title = if (lang.isRtl) "الظهور فوق التطبيقات الأخرى" else "Display Over Other Apps",
                    desc = if (lang.isRtl) "لعرض شاشة الأذان التفاعلية على شاشة القفل" else "To show Azan screen on lockscreen",
                    buttonText = if (lang.isRtl) "فتح الإعدادات" else "Open Settings",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    desc: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MosqueGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = desc, color = Color.LightGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = buttonText, fontSize = 13.sp)
            }
        }
    }
}

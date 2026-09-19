package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
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
import androidx.core.content.ContextCompat
import com.example.data.model.*
import com.example.data.repository.LocationRepository
import com.example.ui.components.MosqueEmeraldDark
import com.example.ui.components.MosqueEmeraldPanel
import com.example.ui.components.MosqueGold
import com.example.ui.language.AppStrings
import com.example.ui.viewmodel.PrayerViewModel
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationAndCalcScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.settingsRepo.language.collectAsState()
    val selectedLocation by viewModel.settingsRepo.selectedLocation.collectAsState()
    val isAutoLocation by viewModel.settingsRepo.isAutoLocation.collectAsState()
    val isAutoTz by viewModel.settingsRepo.isAutoTimeZone.collectAsState()
    val manualTzId by viewModel.settingsRepo.manualTimeZoneId.collectAsState()
    val dstMode by viewModel.settingsRepo.dstMode.collectAsState()
    val calcMethod by viewModel.settingsRepo.calculationMethod.collectAsState()
    val juristicMethod by viewModel.settingsRepo.juristicMethod.collectAsState()
    val timeFormat by viewModel.settingsRepo.timeFormat.collectAsState()
    val hijriOffset by viewModel.settingsRepo.hijriOffsetDays.collectAsState()

    val fajrOffset by viewModel.settingsRepo.fajrOffset.collectAsState()
    val sunriseOffset by viewModel.settingsRepo.sunriseOffset.collectAsState()
    val dhuhrOffset by viewModel.settingsRepo.dhuhrOffset.collectAsState()
    val asrOffset by viewModel.settingsRepo.asrOffset.collectAsState()
    val maghribOffset by viewModel.settingsRepo.maghribOffset.collectAsState()
    val ishaOffset by viewModel.settingsRepo.ishaOffset.collectAsState()
    val isFriday by viewModel.isFriday.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showTzPicker by remember { mutableStateOf(false) }

    // GPS Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchGpsLocation(context) { lat, lng ->
                val tz = TimeZone.getDefault().id
                val name = if (lang.isRtl) "موقعي الحالي (GPS)" else "Current Location (GPS)"
                val customLoc = LocationItem("gps", name, name, name, "", "", "", lat, lng, tz)
                viewModel.settingsRepo.setLocation(customLoc, isAuto = true)
                viewModel.calculateTodayPrayers()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.tabLocationAndCalc(lang),
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
            // 1. Current Location Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (lang.isRtl) "الموقع الحالي" else "Current Location",
                            color = MosqueGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${selectedLocation.getName(lang)} (${selectedLocation.getCountry(lang)})",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Lat: %.4f, Lng: %.4f".format(selectedLocation.latitude, selectedLocation.longitude),
                            color = Color(0xFFA7F3D0),
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // GPS Button
                            Button(
                                onClick = {
                                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (hasFine) {
                                        fetchGpsLocation(context) { lat, lng ->
                                            val tz = TimeZone.getDefault().id
                                            val name = if (lang.isRtl) "موقعي الحالي (GPS)" else "Current Location (GPS)"
                                            val customLoc = LocationItem("gps", name, name, name, "", "", "", lat, lng, tz)
                                            viewModel.settingsRepo.setLocation(customLoc, isAuto = true)
                                            viewModel.calculateTodayPrayers()
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                modifier = Modifier.weight(1f).testTag("gps_auto_btn")
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang.isRtl) "تحديد بـ GPS" else "GPS Auto", fontSize = 13.sp)
                            }

                            // Manual Pick Button
                            OutlinedButton(
                                onClick = { showLocationPicker = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueGold),
                                modifier = Modifier.weight(1f).testTag("choose_city_btn")
                            ) {
                                Text(if (lang.isRtl) "اختر من القائمة" else "Select City", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // 2. Calculation Method
            item {
                SectionCard(title = if (lang.isRtl) "طريقة حساب المواقيت" else "Calculation Method") {
                    CalculationMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.settingsRepo.setCalculationMethod(method)
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (calcMethod == method),
                                onClick = { viewModel.settingsRepo.setCalculationMethod(method) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.getMethodName(method, lang),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 3. Juristic Method (Asr Madhab)
            item {
                SectionCard(title = if (lang.isRtl) "مذهب صلاة العصر" else "Asr Juristic Method") {
                    JuristicMethod.values().forEach { juristic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.settingsRepo.setJuristicMethod(juristic) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (juristicMethod == juristic),
                                onClick = { viewModel.settingsRepo.setJuristicMethod(juristic) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.getJuristicName(juristic, lang),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 4. Daylight Saving Time (DST)
            item {
                SectionCard(title = if (lang.isRtl) "التوقيت الصيفي (DST)" else "Daylight Saving Time") {
                    DstMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.settingsRepo.setDstMode(mode) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (dstMode == mode),
                                onClick = { viewModel.settingsRepo.setDstMode(mode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.getDstName(mode, lang),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 5. Time Format (12H / 24H)
            item {
                SectionCard(title = if (lang.isRtl) "تنسيق الوقت" else "Time Format") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = timeFormat == TimeFormatPreference.FORMAT_12H,
                            onClick = { viewModel.settingsRepo.setTimeFormat(TimeFormatPreference.FORMAT_12H) },
                            label = { Text("12h (AM / PM)") }
                        )
                        FilterChip(
                            selected = timeFormat == TimeFormatPreference.FORMAT_24H,
                            onClick = { viewModel.settingsRepo.setTimeFormat(TimeFormatPreference.FORMAT_24H) },
                            label = { Text("24h") }
                        )
                    }
                }
            }

            // 6. Hijri Calendar Adjustment
            item {
                SectionCard(title = if (lang.isRtl) "تعديل التقويم الهجري يدويًا" else "Manual Hijri Adjustment") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang.isRtl) "فارق الأيام: $hijriOffset يوم" else "Days offset: $hijriOffset days",
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.settingsRepo.setHijriOffsetDays(hijriOffset - 1) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MosqueGold)
                            }
                            Text(text = "$hijriOffset", color = MosqueGold, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.settingsRepo.setHijriOffsetDays(hijriOffset + 1) }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = MosqueGold)
                            }
                        }
                    }
                }
            }

            // 7. Manual Prayer Minutes Offsets (+/- minutes)
            item {
                SectionCard(title = if (lang.isRtl) "تعديل مواقيت الصلاة يدويًا (بالدقائق)" else "Manual Prayer Time Adjustments (Minutes)") {
                    PrayerOffsetRow(if (lang.isRtl) "الفجر" else "Fajr", fajrOffset, lang) { viewModel.settingsRepo.setPrayerOffset(PrayerType.FAJR, it) }
                    PrayerOffsetRow(if (lang.isRtl) "الشروق" else "Sunrise", sunriseOffset, lang) { viewModel.settingsRepo.setPrayerOffset(PrayerType.SUNRISE, it) }
                    PrayerOffsetRow(if (isFriday) (if (lang.isRtl) "الجمعة" else "Jumu'ah") else (if (lang.isRtl) "الظهر" else "Dhuhr"), dhuhrOffset, lang) { viewModel.settingsRepo.setPrayerOffset(PrayerType.DHUHR, it) }
                    PrayerOffsetRow(if (lang.isRtl) "العصر" else "Asr", asrOffset, lang) { viewModel.settingsRepo.setPrayerOffset(PrayerType.ASR, it) }
                    PrayerOffsetRow(if (lang.isRtl) "المغرب" else "Maghrib", maghribOffset, lang) { viewModel.settingsRepo.setPrayerOffset(PrayerType.MAGHRIB, it) }
                    PrayerOffsetRow(if (lang.isRtl) "العشاء" else "Isha", ishaOffset, lang) { viewModel.settingsRepo.setPrayerOffset(PrayerType.ISHA, it) }
                }
            }
        }
    }

    // Modal Sheet or Dialog for 150+ Offline World Locations
    if (showLocationPicker) {
        val filtered = remember(searchQuery, lang) {
            LocationRepository.searchLocations(searchQuery, lang)
        }

        AlertDialog(
            onDismissRequest = { showLocationPicker = false },
            title = {
                Text(
                    text = if (lang.isRtl) "اختر المدينة من قاعدة البيانات المحلية" else "Select City from Offline Database",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (lang.isRtl) "ابحث عن دولة أو مدينة..." else "Search country or city...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered) { loc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.settingsRepo.setLocation(loc, isAuto = false)
                                        showLocationPicker = false
                                        viewModel.calculateTodayPrayers()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = loc.getName(lang), fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = loc.getCountry(lang), fontSize = 12.sp, color = Color.Gray)
                                }
                                if (selectedLocation.id == loc.id) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MosqueGold)
                                }
                            }
                            HorizontalDivider(color = Color(0xFF1E293B))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationPicker = false }) {
                    Text(if (lang.isRtl) "إغلاق" else "Close")
                }
            }
        )
    }
}

@Composable
private fun PrayerOffsetRow(name: String, offset: Int, lang: Language, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = Color.White, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange(offset - 1) }) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = MosqueGold)
            }
            Text(
                text = "${if (offset > 0) "+$offset" else "$offset"} ${if (lang.isRtl) "د" else "m"}",
                color = MosqueGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            IconButton(onClick = { onChange(offset + 1) }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MosqueGold)
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPanel),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = MosqueGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchGpsLocation(context: Context, onLocationResult: (Double, Double) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
    var bestLocation: Location? = null

    for (provider in providers) {
        try {
            val loc = locationManager.getLastKnownLocation(provider)
            if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                bestLocation = loc
            }
        } catch (e: Exception) {
            // Ignore security exception if permission isn't granted yet
        }
    }

    if (bestLocation != null) {
        onLocationResult(bestLocation.latitude, bestLocation.longitude)
    }
}

package com.example.ui.alerts

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.data.repository.SettingsRepository
import com.example.media.SoundHelper
import com.example.ui.language.AppStrings
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class PrePrayerAlertActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen up and show over lockscreen with screen kept ON
        turnScreenOnAndKeepAwake()

        val prayerStr = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: PrayerType.MAGHRIB.name
        val prayerType = try { PrayerType.valueOf(prayerStr) } catch (e: Exception) { PrayerType.MAGHRIB }
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 15)
        val soundUri = intent.getStringExtra(EXTRA_SOUND_URI)

        val settingsRepo = SettingsRepository(applicationContext)
        val lang = settingsRepo.language.value
        val prayerName = AppStrings.getPrayerName(prayerType, lang)

        // Start playing alert sound. The screen stays lit until the sound finishes.
        val fallbackAlert = SoundHelper.getDefaultAlertPath(applicationContext)
        mediaPlayer = SoundHelper.playSound(
            context = applicationContext,
            uriString = soundUri,
            fallbackPath = fallbackAlert,
            isAlarm = true,
            onCompletion = {
                // When sound finishes, immediately close the screen as requested
                runOnUiThread {
                    dismissAndFinish()
                }
            }
        )

        setContent {
            MyApplicationTheme {
                PrePrayerAlertScreen(
                    prayerType = prayerType,
                    prayerName = prayerName,
                    minutesBefore = minutesBefore,
                    lang = lang,
                    onDismiss = {
                        dismissAndFinish()
                    }
                )
            }
        }
    }

    private fun turnScreenOnAndKeepAwake() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        // Keep screen on continuously while this activity is active
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun dismissAndFinish() {
        SoundHelper.stopCurrentSound()
        mediaPlayer?.release()
        mediaPlayer = null
        if (!isFinishing && !isDestroyed) {
            finishAndRemoveTask()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundHelper.stopCurrentSound()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        const val EXTRA_SOUND_URI = "extra_sound_uri"

        fun start(
            context: Context,
            prayerType: PrayerType,
            minutesBefore: Int,
            soundUri: String?
        ) {
            val intent = Intent(context, PrePrayerAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
                putExtra(EXTRA_MINUTES_BEFORE, minutesBefore)
                putExtra(EXTRA_SOUND_URI, soundUri)
            }
            context.startActivity(intent)
        }
    }
}

// Custom Islamic Arch Mihrab Shape
val IslamicArchMihrabShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val r = 24f

    reset()
    // Top Arch Point
    moveTo(w / 2f, 0f)
    // Top right curve of the arch
    cubicTo(w * 0.65f, 0f, w * 0.82f, h * 0.05f, w - r, h * 0.12f)
    cubicTo(w, h * 0.14f, w, h * 0.16f, w, h * 0.18f + r)
    // Right side down
    lineTo(w, h - r)
    cubicTo(w, h, w - r, h, w - r, h)
    // Bottom edge
    lineTo(r, h)
    cubicTo(0f, h, 0f, h - r, 0f, h - r)
    // Left side up
    lineTo(0f, h * 0.18f + r)
    cubicTo(0f, h * 0.16f, 0f, h * 0.14f, r, h * 0.12f)
    // Top left curve of the arch
    cubicTo(w * 0.18f, h * 0.05f, w * 0.35f, 0f, w / 2f, 0f)
    close()
}

@Composable
fun PrePrayerAlertScreen(
    prayerType: PrayerType,
    prayerName: String,
    minutesBefore: Int,
    lang: Language,
    onDismiss: () -> Unit
) {
    // Dynamic countdown timer in seconds
    var remainingSeconds by remember { mutableStateOf(minutesBefore * 60) }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60

    val hoursStr = String.format("%02d", hours)
    val minutesStr = String.format("%02d", minutes)
    val secondsStr = String.format("%02d", seconds)

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0507))
    ) {
        // 1. Photographic Al-Aqsa Sunset Background
        Image(
            painter = painterResource(id = R.drawable.img_pre_prayer_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x77000000),
                            Color(0x33000000),
                            Color(0x99000000)
                        )
                    )
                )
        )

        // Main Vertical Layout matching user image
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Top Header: Islamic Calligraphy "اللهم صل على محمد وعلى آل محمد"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ",
                    color = Color(0xFFFDE68A),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF000000),
                            offset = Offset(0f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
                Text(
                    text = "—  وَعَلَىٰ آلِ مُحَمَّدٍ  —",
                    color = Color(0xFFFDE68A).copy(alpha = 0.9f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )
            }

            // Central Ornate Mihrab Arched Card (Maroon/Burgundy with Gold Trim)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0xFFD4AF37))
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF420B13),
                                Color(0xFF33070E),
                                Color(0xFF220308)
                            )
                        )
                    )
                    .border(
                        width = 2.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFDE68A),
                                Color(0xFFD4AF37),
                                Color(0xFF92400E),
                                Color(0xFFFDE68A)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Small Mosque Silhouette at Top of Card
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        tint = Color(0xFFFDE68A),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // "باقتراب موعد الصلاة"
                    Text(
                        text = if (lang.isRtl) "باقتراب موعد الصلاة" else "Prayer Time Approaching",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Small Golden Ornament
                    Text(
                        text = "❖ ── ❖ ── ❖",
                        color = Color(0xFFD4AF37),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Prayer Name with Mosque Graphic Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = prayerName,
                            color = Color(0xFFFFFBEB),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            style = MaterialTheme.typography.displaySmall.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color(0xFFD4AF37),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 12f
                                )
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (lang.isRtl) "حان وقت الاستعداد للصلاة" else "Time to prepare for prayer",
                        color = Color(0xFFFCD34D),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "❖",
                        color = Color(0xFFD4AF37),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // Inner Countdown Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF240409))
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF92400E), Color(0xFFFDE68A), Color(0xFF92400E))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(vertical = 14.dp, horizontal = 12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (lang.isRtl) "المتبقي حتى أذان $prayerName" else "Time Remaining for $prayerName Azan",
                                color = Color(0xFFFDE68A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Large Digital Clock Digits: 00 : 28 : 17
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "$hoursStr : $minutesStr : $secondsStr",
                                    color = Color.White,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Units: ثانية | دقيقة | ساعة
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (lang.isRtl) "ثانية" else "Sec",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (lang.isRtl) "دقيقة" else "Min",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (lang.isRtl) "ساعة" else "Hour",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Golden Pill Button (Dismiss / Close)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF1F0307))
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFFD4AF37),
                                shape = RoundedCornerShape(30.dp)
                            )
                            .clickable { onDismiss() }
                            .padding(vertical = 10.dp, horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B0B12))
                                    .border(1.dp, Color(0xFFFDE68A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Bell",
                                    tint = Color(0xFFFDE68A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = if (lang.isRtl) "أذان $prayerName خلال $minutesBefore دقيقة (انقر للإغلاق)" else "$prayerName Azan in $minutesBefore min (Tap to close)",
                                color = Color(0xFFFFFBEB),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Bottom Footer Islamic Proverb
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "❖",
                    color = Color(0xFFD4AF37),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (lang.isRtl) "الصلاةُ نورٌ في القلب .. وراحةٌ في الدنيا والآخِرةّ" else "Prayer is light in the heart and serenity in life and eternity",
                    color = Color(0xFFFDE68A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF000000),
                            offset = Offset(0f, 2f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}

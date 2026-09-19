package com.example.ui.azan

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.example.data.model.AzanScreenMode
import com.example.data.model.Language
import com.example.data.model.PrayerType
import com.example.data.repository.SettingsRepository
import com.example.service.AzanMediaService
import com.example.ui.language.AppStrings
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.io.File
import java.util.Calendar

// Custom VideoView subclass that overrides onMeasure to stretch/fill 100% of the screen without borders
class FullScreenVideoView(context: Context) : VideoView(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}

class AzanScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lockscreen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val prayerName = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: PrayerType.FAJR.name
        val prayerType = try { PrayerType.valueOf(prayerName) } catch (e: Exception) { PrayerType.FAJR }
        val playVideoNow = intent.getBooleanExtra(EXTRA_PLAY_VIDEO_NOW, false)
        val videoUriExtra = intent.getStringExtra(EXTRA_VIDEO_URI)
        val titleOverride = intent.getStringExtra(EXTRA_TITLE_OVERRIDE)

        val settingsRepo = SettingsRepository(applicationContext)
        val lang = settingsRepo.language.value
        val config = settingsRepo.getAzanConfig(prayerType)

        setContent {
            MyApplicationTheme {
                AzanScreenContent(
                    prayerType = prayerType,
                    lang = lang,
                    images = config.extractedImages,
                    staticImageUri = config.staticImageUri,
                    screenMode = config.screenMode,
                    slideshowDuration = config.slideshowDurationSeconds,
                    duaaVideoUri = videoUriExtra ?: config.duaaVideoUri,
                    playVideoInitial = playVideoNow,
                    titleOverride = titleOverride,
                    onDismiss = {
                        AzanMediaService.stop(applicationContext)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
        const val EXTRA_PLAY_VIDEO_NOW = "extra_play_video_now"
        const val EXTRA_VIDEO_URI = "extra_video_uri"
        const val EXTRA_TITLE_OVERRIDE = "extra_title_override"
    }
}

@Composable
fun AzanScreenContent(
    prayerType: PrayerType,
    lang: Language,
    images: List<String>,
    staticImageUri: String?,
    screenMode: AzanScreenMode,
    slideshowDuration: Int,
    duaaVideoUri: String?,
    playVideoInitial: Boolean,
    titleOverride: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPlayingVideo by remember { mutableStateOf(playVideoInitial && !duaaVideoUri.isNullOrBlank()) }
    var currentImageIndex by remember { mutableStateOf(0) }

    // Handle horizontal / landscape orientation and full-screen edge-to-edge for Duaa video
    DisposableEffect(isPlayingVideo) {
        val activity = context as? Activity
        if (isPlayingVideo) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            activity?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Slideshow timer for background images
    LaunchedEffect(screenMode, images) {
        if (screenMode == AzanScreenMode.SLIDESHOW && images.isNotEmpty()) {
            while (true) {
                delay(slideshowDuration * 1000L)
                currentImageIndex = (currentImageIndex + 1) % images.size
            }
        }
    }

    val currentImage = remember(currentImageIndex, images, staticImageUri, screenMode) {
        if (screenMode == AzanScreenMode.STATIC && !staticImageUri.isNullOrBlank()) {
            staticImageUri
        } else if (images.isNotEmpty()) {
            images[currentImageIndex]
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isPlayingVideo && !duaaVideoUri.isNullOrBlank()) {
            // =========================================================================
            // FULLSCREEN HORIZONTAL POST-AZAN DUAA VIDEO PLAYER (EDGE-TO-EDGE NO BORDERS)
            // =========================================================================
            AndroidView(
                factory = { ctx ->
                    FullScreenVideoView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setVideoURI(Uri.parse(duaaVideoUri))
                        setOnPreparedListener { mp ->
                            mp.isLooping = false
                            try {
                                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                            } catch (e: Exception) {
                                // Ignore
                            }
                            start()
                        }
                        setOnCompletionListener {
                            isPlayingVideo = false
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Floating Header Bar for Duaa Video (Title + Close Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (lang.isRtl) "دعاء بعد الأذان 🤲" else "Post-Azan Duaa 🤲",
                        color = Color(0xFFFDE68A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = {
                        isPlayingVideo = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Duaa Video",
                        tint = Color.White
                    )
                }
            }

        } else {
            // =========================================================================
            // AZAN BACKGROUND & CALLIGRAPHY OVERLAY (PORTRAIT / SENSOR)
            // =========================================================================
            if (currentImage != null) {
                Crossfade(targetState = currentImage, label = "AzanImageCrossfade") { imgPath ->
                    AsyncImage(
                        model = if (imgPath.startsWith("content://")) Uri.parse(imgPath) else File(imgPath),
                        contentDescription = "Azan Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Elegant Dark Emerald & Gold Mosque Pattern Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF03221C),
                                    Color(0xFF0B3C34),
                                    Color(0xFF041915)
                                )
                            )
                        )
                )
            }

            // Dark Gradient Overlay for contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.65f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top Header: Required Position: "أذان + اسم الصلاة في الجزء العلوي جهة اليمين"
            val displayTitle = titleOverride ?: run {
                val pName = AppStrings.getPrayerName(prayerType, lang)
                when (lang) {
                    Language.ARABIC -> "أذان $pName"
                    Language.FRENCH -> "Adhan $pName"
                    Language.ENGLISH -> "$pName Azan"
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = if (lang.isRtl) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFF10B981)))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🕌",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = displayTitle,
                            color = Color(0xFFFFD700),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Action Bar: Stop / Dismiss & Play Duaa Video
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Optional button to view Duaa video if assigned
                if (!duaaVideoUri.isNullOrBlank()) {
                    FilledTonalButton(
                        onClick = { isPlayingVideo = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang.isRtl) "تشغيل فيديو دعاء بعد الأذان (ملء الشاشة أفقي)" else "Play Post-Azan Duaa (Horizontal Fullscreen)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Stop and Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE11D48),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.VolumeOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang.isRtl) "إيقاف الأذان وإغلاق" else "Stop Azan & Dismiss",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

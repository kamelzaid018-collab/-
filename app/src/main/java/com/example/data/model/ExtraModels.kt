package com.example.data.model

enum class AzanScreenMode {
    SLIDESHOW, // عرض متتابع
    STATIC     // صورة ثابتة
}

data class PrayerAzanConfig(
    val prayerType: PrayerType,
    val azanSoundUri: String? = null,
    val azanSoundName: String? = null,
    val timeAlertSoundUri: String? = null,
    val timeAlertSoundName: String? = null,
    val duaaVideoUri: String? = null,
    val duaaVideoName: String? = null,
    val screenMode: AzanScreenMode = AzanScreenMode.SLIDESHOW,
    val slideshowDurationSeconds: Int = 5, // 3, 5, 10, 15
    val staticImageUri: String? = null,
    val zipArchiveUri: String? = null,
    val extractedImages: List<String> = emptyList() // local file paths
)

data class RamadanConfig(
    val isRamadanMode: Boolean = true,
    val iftarCannonVideoUri: String? = null,
    val iftarCannonVideoName: String? = null,
    val musaharatiVideoUri: String? = null,
    val musaharatiVideoName: String? = null,
    val musaharatiIsFixedTime: Boolean = false, // true: fixed time, false: before Fajr
    val musaharatiFixedHour: Int = 2,
    val musaharatiFixedMinute: Int = 30,
    val musaharatiMinutesBeforeFajr: Int = 45,
    val isMusaharatiEnabled: Boolean = true,
    val isIftarCannonEnabled: Boolean = true
)

enum class SalawatPlayMode {
    ORDER,    // بالترتيب
    RANDOM,   // عشوائي
    SPECIFIC  // صوت محدد
}

data class SalawatConfig(
    val isEnabled: Boolean = true,
    val intervalMinutes: Int = 15, // 1, 5, 10, 15, 30, 60, etc.
    val playMode: SalawatPlayMode = SalawatPlayMode.ORDER,
    val specificSoundUri: String? = null,
    val specificSoundName: String? = null,
    val zipArchiveUri: String? = null,
    val extractedSounds: List<String> = emptyList(), // local file paths
    val lastPlayedIndex: Int = 0,
    val nextReminderTimeMillis: Long = 0L
)

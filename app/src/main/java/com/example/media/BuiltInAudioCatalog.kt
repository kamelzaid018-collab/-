package com.example.media

import com.example.data.model.Language

data class BuiltInAudioItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val isAzan: Boolean = false,
    val isAlert: Boolean = false,
    val isSalawat: Boolean = false
) {
    fun getTitle(lang: Language): String {
        return when (lang) {
            Language.ARABIC -> titleAr
            Language.ENGLISH, Language.FRENCH -> titleEn
        }
    }
}

object BuiltInAudioCatalog {

    fun getAlertSounds(): List<BuiltInAudioItem> = allBuiltInAudios.filter { it.isAlert }

    fun getAzanSounds(): List<BuiltInAudioItem> = allBuiltInAudios.filter { it.isAzan }

    fun getSalawatSounds(): List<BuiltInAudioItem> = allBuiltInAudios.filter { it.isSalawat }

    val allBuiltInAudios: List<BuiltInAudioItem> = listOf(
        BuiltInAudioItem(
            id = "builtin_azan_default",
            titleAr = "نغمة الأذان المدمجة (افتراضي)",
            titleEn = "Built-in Azan Tone (Default)",
            isAzan = true
        ),
        BuiltInAudioItem(
            id = "builtin_time_alert",
            titleAr = "نغمة التنبيه بالوقت (افتراضي)",
            titleEn = "Time Alert Tone (Default)",
            isAlert = true
        ),
        BuiltInAudioItem(
            id = "builtin_alert_default",
            titleAr = "نغمة تنبيه اقتراب الصلاة (افتراضي)",
            titleEn = "Pre-Prayer Alert Tone (Default)",
            isAlert = true
        ),
        BuiltInAudioItem(
            id = "builtin_salawat_1",
            titleAr = "نغمة التذكير بالصلاة على النبي ١",
            titleEn = "Salawat Remembrance Tone 1",
            isSalawat = true,
            isAlert = true
        ),
        BuiltInAudioItem(
            id = "builtin_salawat_2",
            titleAr = "نغمة التذكير بالصلاة على النبي ٢",
            titleEn = "Salawat Remembrance Tone 2",
            isSalawat = true,
            isAlert = true
        )
    )

    fun findById(id: String?): BuiltInAudioItem? {
        if (id == null) return null
        return allBuiltInAudios.firstOrNull { it.id == id }
    }
}

package com.example.ui.language

import com.example.data.model.CalculationMethod
import com.example.data.model.DstMode
import com.example.data.model.JuristicMethod
import com.example.data.model.Language
import com.example.data.model.PrayerType
import com.example.data.model.SalawatPlayMode
import com.example.data.model.TimeFormatPreference
import java.util.Calendar
import java.util.TimeZone

object AppStrings {

    fun isTodayFriday(timeZoneId: String? = null): Boolean {
        val cal = if (!timeZoneId.isNullOrBlank()) {
            Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
        } else {
            Calendar.getInstance()
        }
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }

    fun getPrayerName(prayer: PrayerType, lang: Language, isFriday: Boolean = isTodayFriday()): String {
        val effective = if (prayer == PrayerType.DHUHR && isFriday) PrayerType.JUMUAH else prayer
        return when (lang) {
            Language.ARABIC -> when (effective) {
                PrayerType.FAJR -> "الفجر"
                PrayerType.SUNRISE -> "الشروق"
                PrayerType.DHUHR -> "الظهر"
                PrayerType.ASR -> "العصر"
                PrayerType.MAGHRIB -> "المغرب"
                PrayerType.ISHA -> "العشاء"
                PrayerType.JUMUAH -> "الجمعة"
                PrayerType.ALL -> "كل الصلوات"
            }
            Language.ENGLISH -> when (effective) {
                PrayerType.FAJR -> "Fajr"
                PrayerType.SUNRISE -> "Sunrise"
                PrayerType.DHUHR -> "Dhuhr"
                PrayerType.ASR -> "Asr"
                PrayerType.MAGHRIB -> "Maghrib"
                PrayerType.ISHA -> "Isha"
                PrayerType.JUMUAH -> "Jumu'ah"
                PrayerType.ALL -> "All Prayers"
            }
            Language.FRENCH -> when (effective) {
                PrayerType.FAJR -> "Fajr"
                PrayerType.SUNRISE -> "Lever du soleil"
                PrayerType.DHUHR -> "Dhuhr"
                PrayerType.ASR -> "Asr"
                PrayerType.MAGHRIB -> "Maghrib"
                PrayerType.ISHA -> "Isha"
                PrayerType.JUMUAH -> "Joumou'a"
                PrayerType.ALL -> "Toutes les prières"
            }
        }
    }

    /**
     * Required Format:
     * يتبقى + عدد الدقائق + على أذان + اسم الصلاة
     * مثال: يتبقى 10 دقائق على أذان العصر
     */
    fun formatAlertMessage(minutes: Int, prayer: PrayerType, lang: Language): String {
        val pName = getPrayerName(prayer, lang)
        return when (lang) {
            Language.ARABIC -> {
                val minWord = if (minutes == 1) "دقيقة واحدة" else if (minutes == 2) "دقيقتان" else if (minutes in 3..10) "$minutes دقائق" else "$minutes دقيقة"
                "يتبقى $minWord على أذان $pName"
            }
            Language.ENGLISH -> {
                val minWord = if (minutes == 1) "1 minute" else "$minutes minutes"
                "$minWord remaining until $pName Azan"
            }
            Language.FRENCH -> {
                val minWord = if (minutes == 1) "1 minute" else "$minutes minutes"
                "Il reste $minWord avant l'Adhan de $pName"
            }
        }
    }

    fun getMethodName(method: CalculationMethod, lang: Language): String {
        return when (lang) {
            Language.ARABIC -> when (method) {
                CalculationMethod.EGYPT -> "الهيئة المصرية العامة للمساحة"
                CalculationMethod.MAKKAH -> "جامعة أم القرى - مكة المكرمة"
                CalculationMethod.MWL -> "رابطة العالم الإسلامي"
                CalculationMethod.ISNA -> "الجمعية الإسلامية لأمريكا الشمالية (ISNA)"
                CalculationMethod.KARACHI -> "جامعة العلوم الإسلامية بكراتشي"
                CalculationMethod.GULF -> "منطقة الخليج العربي / دبي"
                CalculationMethod.KUWAIT -> "وزارة الأوقاف والشؤون الإسلامية - الكويت"
                CalculationMethod.QATAR -> "وزارة الأوقاف القطرية"
                CalculationMethod.MUIS -> "مجلس الشؤون الإسلامية بسنغافورة (MUIS)"
                CalculationMethod.FRANCE -> "اتحاد المنظمات الإسلامية في فرنسا (UOIF)"
                CalculationMethod.TURKEY -> "رئاسة الشؤون الدينية التركية (Diyanet)"
                CalculationMethod.TEHRAN -> "معهد الجيوفيزياء بجامعة طهران"
                CalculationMethod.SHIA_ITHNA -> "الشيعة الإثنا عشرية (معهد ليفا بقم)"
            }
            Language.ENGLISH -> when (method) {
                CalculationMethod.EGYPT -> "Egyptian General Authority of Survey"
                CalculationMethod.MAKKAH -> "Umm Al-Qura University, Makkah"
                CalculationMethod.MWL -> "Muslim World League (MWL)"
                CalculationMethod.ISNA -> "Islamic Society of North America (ISNA)"
                CalculationMethod.KARACHI -> "University of Islamic Sciences, Karachi"
                CalculationMethod.GULF -> "Gulf Region / Dubai"
                CalculationMethod.KUWAIT -> "Kuwait Ministry of Awqaf"
                CalculationMethod.QATAR -> "Qatar Ministry of Awqaf"
                CalculationMethod.MUIS -> "Majlis Ugama Islam Singapura (MUIS)"
                CalculationMethod.FRANCE -> "Union of Islamic Organizations of France (UOIF)"
                CalculationMethod.TURKEY -> "Diyanet İşleri Başkanlığı, Turkey"
                CalculationMethod.TEHRAN -> "Institute of Geophysics, Univ. of Tehran"
                CalculationMethod.SHIA_ITHNA -> "Shia Ithna-Ashari (Leva Institute, Qum)"
            }
            Language.FRENCH -> when (method) {
                CalculationMethod.EGYPT -> "Autorité Générale Égyptienne de l'Arpentage"
                CalculationMethod.MAKKAH -> "Université Oumm Al-Qura, La Mecque"
                CalculationMethod.MWL -> "Ligue Islamique Mondiale (LIM)"
                CalculationMethod.ISNA -> "Société Islamique d'Amérique du Nord (ISNA)"
                CalculationMethod.KARACHI -> "Université des Sciences Islamiques, Karachi"
                CalculationMethod.GULF -> "Région du Golfe / Dubaï"
                CalculationMethod.KUWAIT -> "Koweït Ministère des Awqaf"
                CalculationMethod.QATAR -> "Qatar Ministère des Awqaf"
                CalculationMethod.MUIS -> "Majlis Ugama Islam Singapura (MUIS)"
                CalculationMethod.FRANCE -> "Union des Organisations Islamiques de France (UOIF)"
                CalculationMethod.TURKEY -> "Diyanet, Turquie"
                CalculationMethod.TEHRAN -> "Institut de Géophysique, Univ. de Téhéran"
                CalculationMethod.SHIA_ITHNA -> "Chiite Ithna-Ashari (Institut Leva, Qom)"
            }
        }
    }

    fun getJuristicName(juristic: JuristicMethod, lang: Language): String {
        return when (lang) {
            Language.ARABIC -> when (juristic) {
                JuristicMethod.STANDARD -> "الشافعي والمالكي والحنبلي (الجمهور)"
                JuristicMethod.HANAFI -> "الحنفي (ظل المثلين)"
            }
            Language.ENGLISH -> when (juristic) {
                JuristicMethod.STANDARD -> "Shafi'i, Maliki, Hanbali (Standard)"
                JuristicMethod.HANAFI -> "Hanafi (Double Shadow)"
            }
            Language.FRENCH -> when (juristic) {
                JuristicMethod.STANDARD -> "Chafi'i, Maliki, Hanbali (Standard)"
                JuristicMethod.HANAFI -> "Hanafi (Double Ombre)"
            }
        }
    }

    fun getDstName(dst: DstMode, lang: Language): String {
        return when (lang) {
            Language.ARABIC -> when (dst) {
                DstMode.AUTO -> "تلقائي (الخميس الأخير من أبريل - الخميس الأخير من أكتوبر)"
                DstMode.ALWAYS_ON -> "مفعل يدويًا دائمًا (+1 ساعة)"
                DstMode.ALWAYS_OFF -> "معطل يدويًا دائمًا"
            }
            Language.ENGLISH -> when (dst) {
                DstMode.AUTO -> "Auto (Last Thursday of April – Last Thursday of October)"
                DstMode.ALWAYS_ON -> "Manually ON (+1 hour)"
                DstMode.ALWAYS_OFF -> "Manually OFF"
            }
            Language.FRENCH -> when (dst) {
                DstMode.AUTO -> "Auto (Dernier jeudi d'avril – Dernier jeudi d'octobre)"
                DstMode.ALWAYS_ON -> "Toujours activé (+1 heure)"
                DstMode.ALWAYS_OFF -> "Toujours désactivé"
            }
        }
    }

    fun getSalawatModeName(mode: SalawatPlayMode, lang: Language): String {
        return when (lang) {
            Language.ARABIC -> when (mode) {
                SalawatPlayMode.ORDER -> "بالترتيب"
                SalawatPlayMode.RANDOM -> "عشوائي"
                SalawatPlayMode.SPECIFIC -> "صوت محدد"
            }
            Language.ENGLISH -> when (mode) {
                SalawatPlayMode.ORDER -> "Sequential (In Order)"
                SalawatPlayMode.RANDOM -> "Random"
                SalawatPlayMode.SPECIFIC -> "Specific Sound"
            }
            Language.FRENCH -> when (mode) {
                SalawatPlayMode.ORDER -> "En ordre séquentiel"
                SalawatPlayMode.RANDOM -> "Aléatoire"
                SalawatPlayMode.SPECIFIC -> "Son spécifique"
            }
        }
    }

    // Tabs titles
    fun tabMain(lang: Language) = when(lang) { Language.ARABIC -> "الرئيسية"; Language.ENGLISH -> "Home"; Language.FRENCH -> "Accueil" }
    fun tabMenu(lang: Language) = when(lang) { Language.ARABIC -> "القائمة"; Language.ENGLISH -> "Menu"; Language.FRENCH -> "Menu" }
    fun tabLanguage(lang: Language) = when(lang) { Language.ARABIC -> "اللغة"; Language.ENGLISH -> "Language"; Language.FRENCH -> "Langue" }
    fun tabLocationAndCalc(lang: Language) = when(lang) { Language.ARABIC -> "المكان والحساب"; Language.ENGLISH -> "Location & Calc"; Language.FRENCH -> "Lieu & Calcul" }
    fun tabAlerts(lang: Language) = when(lang) { Language.ARABIC -> "التنبيهات"; Language.ENGLISH -> "Alerts"; Language.FRENCH -> "Alertes" }
    fun tabAzan(lang: Language) = when(lang) { Language.ARABIC -> "الأذان"; Language.ENGLISH -> "Azan"; Language.FRENCH -> "Adhan" }
    fun tabRamadan(lang: Language) = when(lang) { Language.ARABIC -> "رمضان"; Language.ENGLISH -> "Ramadan"; Language.FRENCH -> "Ramadan" }
    fun tabSalawat(lang: Language) = when(lang) { Language.ARABIC -> "الصلاة على النبي ﷺ"; Language.ENGLISH -> "Salawat on Prophet ﷺ"; Language.FRENCH -> "Prières sur le Prophète ﷺ" }
    fun tabNotificationAndWidgets(lang: Language) = when(lang) { Language.ARABIC -> "شريط الإشعارات والتطبيقات المصغرة"; Language.ENGLISH -> "Notification Bar & Widgets"; Language.FRENCH -> "Barre de notifications & Widgets" }
    fun tabPermissions(lang: Language) = when(lang) { Language.ARABIC -> "الأذونات المطلوبة"; Language.ENGLISH -> "Permissions"; Language.FRENCH -> "Autorisations" }

    // Day names and Repeat string helper
    fun getDayName(calendarDayOfWeek: Int, lang: Language): String {
        return when (calendarDayOfWeek) {
            java.util.Calendar.SATURDAY -> when (lang) { Language.ARABIC -> "السبت"; Language.ENGLISH -> "Saturday"; Language.FRENCH -> "Samedi" }
            java.util.Calendar.SUNDAY -> when (lang) { Language.ARABIC -> "الأحد"; Language.ENGLISH -> "Sunday"; Language.FRENCH -> "Dimanche" }
            java.util.Calendar.MONDAY -> when (lang) { Language.ARABIC -> "الإثنين"; Language.ENGLISH -> "Monday"; Language.FRENCH -> "Lundi" }
            java.util.Calendar.TUESDAY -> when (lang) { Language.ARABIC -> "الثلاثاء"; Language.ENGLISH -> "Tuesday"; Language.FRENCH -> "Mardi" }
            java.util.Calendar.WEDNESDAY -> when (lang) { Language.ARABIC -> "الأربعاء"; Language.ENGLISH -> "Wednesday"; Language.FRENCH -> "Mercredi" }
            java.util.Calendar.THURSDAY -> when (lang) { Language.ARABIC -> "الخميس"; Language.ENGLISH -> "Thursday"; Language.FRENCH -> "Jeudi" }
            java.util.Calendar.FRIDAY -> when (lang) { Language.ARABIC -> "الجمعة"; Language.ENGLISH -> "Friday"; Language.FRENCH -> "Vendredi" }
            else -> ""
        }
    }

    fun formatRepeatDays(repeatDays: String, lang: Language): String {
        if (repeatDays.isBlank()) return when(lang) { Language.ARABIC -> "يومياً"; Language.ENGLISH -> "Daily"; Language.FRENCH -> "Tous les jours" }
        val days = repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.sorted()
        if (days.size == 7) {
            return when (lang) {
                Language.ARABIC -> "يومياً (كل الأيام)"
                Language.ENGLISH -> "Daily (Every day)"
                Language.FRENCH -> "Tous les jours"
            }
        }
        val order = listOf(
            java.util.Calendar.SATURDAY,
            java.util.Calendar.SUNDAY,
            java.util.Calendar.MONDAY,
            java.util.Calendar.TUESDAY,
            java.util.Calendar.WEDNESDAY,
            java.util.Calendar.THURSDAY,
            java.util.Calendar.FRIDAY
        )
        val sortedDays = order.filter { days.contains(it) }
        return sortedDays.joinToString(separator = "، ") { getDayName(it, lang) }
    }

    // Common labels
    fun nextPrayer(lang: Language) = when(lang) { Language.ARABIC -> "الصلاة القادمة"; Language.ENGLISH -> "Next Prayer"; Language.FRENCH -> "Prochaine Prière" }
    fun timeRemaining(lang: Language) = when(lang) { Language.ARABIC -> "الوقت المتبقي"; Language.ENGLISH -> "Time Remaining"; Language.FRENCH -> "Temps Restant" }
    fun currentTime(lang: Language) = when(lang) { Language.ARABIC -> "الوقت الحالي"; Language.ENGLISH -> "Current Time"; Language.FRENCH -> "Heure Actuelle" }
    fun bismillah(lang: Language) = "بِسْمِ اللَّـهِ الرَّحْمَـٰنِ الرَّحِيمِ"
}

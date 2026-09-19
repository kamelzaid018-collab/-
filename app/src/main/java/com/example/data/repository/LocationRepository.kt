package com.example.data.repository

import com.example.data.model.CalculationMethod
import com.example.data.model.Language
import com.example.data.model.LocationItem

object LocationRepository {

    val allLocations: List<LocationItem> = listOf(
        // Saudi Arabia
        LocationItem("sa_makkah", "مكة المكرمة", "Makkah", "La Mecque", "السعودية", "Saudi Arabia", "Arabie Saoudite", 21.4225, 39.8262, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_madinah", "المدينة المنورة", "Madinah", "Médine", "السعودية", "Saudi Arabia", "Arabie Saoudite", 24.4686, 39.6142, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_riyadh", "الرياض", "Riyadh", "Riyad", "السعودية", "Saudi Arabia", "Arabie Saoudite", 24.7136, 46.6753, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_jeddah", "جدة", "Jeddah", "Djeddah", "السعودية", "Saudi Arabia", "Arabie Saoudite", 21.5433, 39.1728, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_dammam", "الدمام", "Dammam", "Dammam", "السعودية", "Saudi Arabia", "Arabie Saoudite", 26.4207, 50.0888, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_taif", "الطائف", "Taif", "Taïf", "السعودية", "Saudi Arabia", "Arabie Saoudite", 21.2854, 40.4222, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_tabuk", "تبوك", "Tabuk", "Tabuk", "السعودية", "Saudi Arabia", "Arabie Saoudite", 28.3835, 36.5662, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_buraidah", "بريدة", "Buraidah", "Buraïdah", "السعودية", "Saudi Arabia", "Arabie Saoudite", 26.3260, 43.9750, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_abha", "أبها", "Abha", "Abha", "السعودية", "Saudi Arabia", "Arabie Saoudite", 18.2164, 42.5053, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_khobar", "الخبر", "Khobar", "Khobar", "السعودية", "Saudi Arabia", "Arabie Saoudite", 26.2172, 50.1971, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_hail", "حائل", "Hail", "Haïl", "السعودية", "Saudi Arabia", "Arabie Saoudite", 27.5114, 41.7208, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_jizan", "جازان", "Jizan", "Jizan", "السعودية", "Saudi Arabia", "Arabie Saoudite", 16.8892, 42.5706, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_najran", "نجران", "Najran", "Najran", "السعودية", "Saudi Arabia", "Arabie Saoudite", 17.4924, 44.1277, "Asia/Riyadh", CalculationMethod.MAKKAH),
        LocationItem("sa_yanbu", "ينبع", "Yanbu", "Yanbu", "السعودية", "Saudi Arabia", "Arabie Saoudite", 24.0895, 38.0637, "Asia/Riyadh", CalculationMethod.MAKKAH),

        // Egypt
        LocationItem("eg_cairo", "القاهرة", "Cairo", "Le Caire", "مصر", "Egypt", "Égypte", 30.0444, 31.2357, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_alexandria", "الإسكندرية", "Alexandria", "Alexandrie", "مصر", "Egypt", "Égypte", 31.2001, 29.9187, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_giza", "الجيزة", "Giza", "Gizeh", "مصر", "Egypt", "Égypte", 30.0131, 31.2089, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_portsaid", "بور سعيد", "Port Said", "Port-Saïd", "مصر", "Egypt", "Égypte", 31.2653, 32.3019, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_suez", "السويس", "Suez", "Suez", "مصر", "Egypt", "Égypte", 29.9668, 32.5498, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_mansoura", "المنصورة", "Mansoura", "Mansourah", "مصر", "Egypt", "Égypte", 31.0409, 31.3785, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_tanta", "طنطا", "Tanta", "Tanta", "مصر", "Egypt", "Égypte", 30.7865, 31.0004, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_asyut", "أسيوط", "Asyut", "Assiout", "مصر", "Egypt", "Égypte", 27.1783, 31.1859, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_ismailia", "الإسماعيلية", "Ismailia", "Ismaïlia", "مصر", "Egypt", "Égypte", 30.5965, 32.2715, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_fayoum", "الفيوم", "Fayoum", "Médinet el-Fayoum", "مصر", "Egypt", "Égypte", 29.3084, 30.8428, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_zagazig", "الزقازيق", "Zagazig", "Zagazig", "مصر", "Egypt", "Égypte", 30.5765, 31.5041, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_aswan", "أسوان", "Aswan", "Assouan", "مصر", "Egypt", "Égypte", 24.0889, 32.8998, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_damietta", "دمياط", "Damietta", "Damiette", "مصر", "Egypt", "Égypte", 31.4175, 31.8144, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_minya", "المنيا", "Minya", "Al-Minya", "مصر", "Egypt", "Égypte", 28.0871, 30.7618, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_benisuef", "بني سويف", "Beni Suef", "Beni Souef", "مصر", "Egypt", "Égypte", 29.0661, 31.0994, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_luxor", "الأقصر", "Luxor", "Louxor", "مصر", "Egypt", "Égypte", 25.6872, 32.6396, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_sohag", "سوهاج", "Sohag", "Sohag", "مصر", "Egypt", "Égypte", 26.5569, 31.6948, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_qena", "قنا", "قنا", "Qena", "مصر", "Egypt", "Égypte", 26.1551, 32.7160, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_hurghada", "الغردقة", "Hurghada", "Hurghada", "مصر", "Egypt", "Égypte", 27.2579, 33.8116, "Africa/Cairo", CalculationMethod.EGYPT),
        LocationItem("eg_sharm", "شرم الشيخ", "Sharm El Sheikh", "Charm el-Cheikh", "مصر", "Egypt", "Égypte", 27.9158, 34.3299, "Africa/Cairo", CalculationMethod.EGYPT),

        // UAE
        LocationItem("ae_abudhabi", "أبو ظبي", "Abu Dhabi", "Abou Dabi", "الإمارات", "UAE", "Émirats Arabes Unis", 24.4539, 54.3773, "Asia/Dubai", CalculationMethod.GULF),
        LocationItem("ae_dubai", "دبي", "Dubai", "Dubaï", "الإمارات", "UAE", "Émirats Arabes Unis", 25.2048, 55.2708, "Asia/Dubai", CalculationMethod.GULF),
        LocationItem("ae_sharjah", "الشارقة", "Sharjah", "Charjah", "الإمارات", "UAE", "Émirats Arabes Unis", 25.3463, 55.4209, "Asia/Dubai", CalculationMethod.GULF),
        LocationItem("ae_alain", "العين", "Al Ain", "Al-Aïn", "الإمارات", "UAE", "Émirats Arabes Unis", 24.2075, 55.7447, "Asia/Dubai", CalculationMethod.GULF),
        LocationItem("ae_ajman", "عجمان", "Ajman", "Ajman", "الإمارات", "UAE", "Émirats Arabes Unis", 25.4052, 55.5136, "Asia/Dubai", CalculationMethod.GULF),
        LocationItem("ae_rak", "رأس الخيمة", "Ras Al Khaimah", "Ras el Khaïmah", "الإمارات", "UAE", "Émirats Arabes Unis", 25.6741, 55.9804, "Asia/Dubai", CalculationMethod.GULF),
        LocationItem("ae_fujairah", "الفجيرة", "Fujairah", "Fujaïrah", "الإمارات", "UAE", "Émirats Arabes Unis", 25.1288, 56.3265, "Asia/Dubai", CalculationMethod.GULF),

        // Kuwait
        LocationItem("kw_kuwait", "مدينة الكويت", "Kuwait City", "Koweït", "الكويت", "Kuwait", "Koweït", 29.3759, 47.9774, "Asia/Kuwait", CalculationMethod.KUWAIT),
        LocationItem("kw_hawalli", "حولي", "Hawalli", "Hawalli", "الكويت", "Kuwait", "Koweït", 29.3328, 48.0286, "Asia/Kuwait", CalculationMethod.KUWAIT),
        LocationItem("kw_ahmadi", "الأحمدي", "Al Ahmadi", "Al Ahmadi", "الكويت", "Kuwait", "Koweït", 29.0769, 48.0839, "Asia/Kuwait", CalculationMethod.KUWAIT),

        // Qatar
        LocationItem("qa_doha", "الدوحة", "Doha", "Doha", "قطر", "Qatar", "Qatar", 25.2854, 51.5310, "Asia/Qatar", CalculationMethod.QATAR),
        LocationItem("qa_rayyan", "الريان", "Al Rayyan", "Al Rayyan", "قطر", "Qatar", "Qatar", 25.2919, 51.4244, "Asia/Qatar", CalculationMethod.QATAR),
        LocationItem("qa_wakrah", "الوكرة", "Al Wakrah", "Al Wakrah", "قطر", "Qatar", "Qatar", 25.1768, 51.6048, "Asia/Qatar", CalculationMethod.QATAR),

        // Bahrain
        LocationItem("bh_manama", "المنامة", "Manama", "Manama", "البحرين", "Bahrain", "Bahreïn", 26.2285, 50.5860, "Asia/Bahrain", CalculationMethod.MWL),
        LocationItem("bh_riffa", "الرفاع", "Riffa", "Riffa", "البحرين", "Bahrain", "Bahreïn", 26.1300, 50.5550, "Asia/Bahrain", CalculationMethod.MWL),

        // Oman
        LocationItem("om_muscat", "مسقط", "Muscat", "Mascate", "عمان", "Oman", "Oman", 23.5859, 58.4059, "Asia/Muscat", CalculationMethod.MWL),
        LocationItem("om_salalah", "صلالة", "Salalah", "Salalah", "عمان", "Oman", "Oman", 17.0151, 54.0924, "Asia/Muscat", CalculationMethod.MWL),
        LocationItem("om_sohar", "صحار", "Sohar", "Sohar", "عمان", "Oman", "Oman", 24.3644, 56.7468, "Asia/Muscat", CalculationMethod.MWL),

        // Jordan
        LocationItem("jo_amman", "عمّان", "Amman", "Amman", "الأردن", "Jordan", "Jordanie", 31.9454, 35.9284, "Asia/Amman", CalculationMethod.MWL),
        LocationItem("jo_zarqa", "الزرقاء", "Zarqa", "Zarqa", "الأردن", "Jordan", "Jordanie", 32.0728, 36.0880, "Asia/Amman", CalculationMethod.MWL),
        LocationItem("jo_irbid", "إربد", "Irbid", "Irbid", "الأردن", "Jordan", "Jordanie", 32.5568, 35.8469, "Asia/Amman", CalculationMethod.MWL),
        LocationItem("jo_aqaba", "العقبة", "Aqaba", "Aqaba", "الأردن", "Jordan", "Jordanie", 29.5321, 35.0063, "Asia/Amman", CalculationMethod.MWL),

        // Palestine
        LocationItem("ps_jerusalem", "القدس الشريف", "Jerusalem", "Jérusalem", "فلسطين", "Palestine", "Palestine", 31.7683, 35.2137, "Asia/Hebron", CalculationMethod.MWL),
        LocationItem("ps_gaza", "غزة", "Gaza", "Gaza", "فلسطين", "Palestine", "Palestine", 31.5017, 34.4668, "Asia/Gaza", CalculationMethod.MWL),
        LocationItem("ps_ramallah", "رام الله", "Ramallah", "Ramallah", "فلسطين", "Palestine", "Palestine", 31.9038, 35.2034, "Asia/Hebron", CalculationMethod.MWL),
        LocationItem("ps_hebron", "الخليل", "Hebron", "Hébron", "فلسطين", "Palestine", "Palestine", 31.5326, 35.0998, "Asia/Hebron", CalculationMethod.MWL),
        LocationItem("ps_nablus", "نابلس", "Nablus", "Naplouse", "فلسطين", "Palestine", "Palestine", 32.2227, 35.2621, "Asia/Hebron", CalculationMethod.MWL),

        // Lebanon
        LocationItem("lb_beirut", "بيروت", "Beirut", "Beyrouth", "لبنان", "Lebanon", "Liban", 33.8938, 35.5018, "Asia/Beirut", CalculationMethod.MWL),
        LocationItem("lb_tripoli", "طرابلس", "Tripoli", "Tripoli", "لبنان", "Lebanon", "Liban", 34.4333, 35.8333, "Asia/Beirut", CalculationMethod.MWL),
        LocationItem("lb_sidon", "صيدا", "Sidon", "Saïda", "لبنان", "Lebanon", "Liban", 33.5631, 35.3689, "Asia/Beirut", CalculationMethod.MWL),

        // Syria
        LocationItem("sy_damascus", "دمشق", "Damascus", "Damas", "سوريا", "Syria", "Syrie", 33.5138, 36.2765, "Asia/Damascus", CalculationMethod.MWL),
        LocationItem("sy_aleppo", "حلب", "Aleppo", "Alep", "سوريا", "Syria", "Syrie", 36.2021, 37.1343, "Asia/Damascus", CalculationMethod.MWL),
        LocationItem("sy_homs", "حمص", "Homs", "Homs", "سوريا", "Syria", "Syrie", 34.7324, 36.7137, "Asia/Damascus", CalculationMethod.MWL),
        LocationItem("sy_latakia", "اللاذقية", "Latakia", "Lattaquié", "سوريا", "Syria", "Syrie", 35.5317, 35.7900, "Asia/Damascus", CalculationMethod.MWL),

        // Iraq
        LocationItem("iq_baghdad", "بغداد", "Baghdad", "Bagdad", "العراق", "Iraq", "Irak", 33.3152, 44.3661, "Asia/Baghdad", CalculationMethod.MWL),
        LocationItem("iq_basra", "البصرة", "Basra", "Bassorah", "العراق", "Iraq", "Irak", 30.5081, 47.7835, "Asia/Baghdad", CalculationMethod.MWL),
        LocationItem("iq_erbil", "أربيل", "Erbil", "Erbil", "العراق", "Iraq", "Irak", 36.1911, 44.0092, "Asia/Baghdad", CalculationMethod.MWL),
        LocationItem("iq_mosul", "الموصل", "Mosul", "Mossoul", "العراق", "Iraq", "Irak", 36.3400, 43.1300, "Asia/Baghdad", CalculationMethod.MWL),
        LocationItem("iq_najaf", "النجف الأشرف", "Najaf", "Nadjaf", "العراق", "Iraq", "Irak", 32.0000, 44.3300, "Asia/Baghdad", CalculationMethod.SHIA_ITHNA),
        LocationItem("iq_karbala", "كربلاء المقدسة", "Karbala", "Kerbala", "العراق", "Iraq", "Irak", 32.6160, 44.0249, "Asia/Baghdad", CalculationMethod.SHIA_ITHNA),

        // Yemen
        LocationItem("ye_sanaa", "صنعاء", "Sanaa", "Sanaa", "اليمن", "Yemen", "Yémen", 15.3694, 44.1910, "Asia/Aden", CalculationMethod.MWL),
        LocationItem("ye_aden", "عدن", "Aden", "Aden", "اليمن", "Yemen", "Yémen", 12.7855, 45.0187, "Asia/Aden", CalculationMethod.MWL),
        LocationItem("ye_taiz", "تعز", "Taiz", "Taëz", "اليمن", "Yemen", "Yémen", 13.5789, 44.0189, "Asia/Aden", CalculationMethod.MWL),

        // Morocco
        LocationItem("ma_rabat", "الرباط", "Rabat", "Rabat", "المغرب", "Morocco", "Maroc", 34.0209, -6.8416, "Africa/Casablanca", CalculationMethod.MWL),
        LocationItem("ma_casablanca", "الدار البيضاء", "Casablanca", "Casablanca", "المغرب", "Morocco", "Maroc", 33.5731, -7.5898, "Africa/Casablanca", CalculationMethod.MWL),
        LocationItem("ma_marrakech", "مراكش", "Marrakech", "Marrakech", "المغرب", "Morocco", "Maroc", 31.6295, -7.9811, "Africa/Casablanca", CalculationMethod.MWL),
        LocationItem("ma_fes", "فاس", "Fes", "Fès", "المغرب", "Morocco", "Maroc", 34.0331, -5.0003, "Africa/Casablanca", CalculationMethod.MWL),
        LocationItem("ma_tangier", "طنجة", "Tangier", "Tanger", "المغرب", "Morocco", "Maroc", 35.7595, -5.8340, "Africa/Casablanca", CalculationMethod.MWL),
        LocationItem("ma_agadir", "أكادير", "Agadir", "Agadir", "المغرب", "Morocco", "Maroc", 30.4278, -9.5981, "Africa/Casablanca", CalculationMethod.MWL),
        LocationItem("ma_oujda", "وجدة", "Oujda", "Oujda", "المغرب", "Morocco", "Maroc", 34.6814, -1.9086, "Africa/Casablanca", CalculationMethod.MWL),

        // Algeria
        LocationItem("dz_algiers", "الجزائر العاصمة", "Algiers", "Alger", "الجزائر", "Algeria", "Algérie", 36.7538, 3.0588, "Africa/Algiers", CalculationMethod.MWL),
        LocationItem("dz_oran", "وهران", "Oran", "Oran", "الجزائر", "Algeria", "Algérie", 35.6987, -0.6349, "Africa/Algiers", CalculationMethod.MWL),
        LocationItem("dz_constantine", "قسنطينة", "Constantine", "Constantine", "الجزائر", "Algeria", "Algérie", 36.3650, 6.6147, "Africa/Algiers", CalculationMethod.MWL),
        LocationItem("dz_annaba", "عنابة", "Annaba", "Annaba", "الجزائر", "Algeria", "Algérie", 36.9000, 7.7667, "Africa/Algiers", CalculationMethod.MWL),
        LocationItem("dz_setif", "سطيف", "Setif", "Sétif", "الجزائر", "Algeria", "Algérie", 36.1900, 5.4100, "Africa/Algiers", CalculationMethod.MWL),
        LocationItem("dz_tlemcen", "تلمسان", "Tlemcen", "Tlemcen", "الجزائر", "Algeria", "Algérie", 34.8783, -1.3150, "Africa/Algiers", CalculationMethod.MWL),

        // Tunisia
        LocationItem("tn_tunis", "تونس العاصمة", "Tunis", "Tunis", "تونس", "Tunisia", "Tunisie", 36.8065, 10.1815, "Africa/Tunis", CalculationMethod.MWL),
        LocationItem("tn_sfax", "صفاقس", "Sfax", "Sfax", "تونس", "Tunisia", "Tunisie", 34.7406, 10.7603, "Africa/Tunis", CalculationMethod.MWL),
        LocationItem("tn_sousse", "سوسة", "Sousse", "Sousse", "تونس", "Tunisia", "Tunisie", 35.8256, 10.6370, "Africa/Tunis", CalculationMethod.MWL),
        LocationItem("tn_kairouan", "القيروان", "Kairouan", "Kairouan", "تونس", "Tunisia", "Tunisie", 35.6781, 10.0963, "Africa/Tunis", CalculationMethod.MWL),

        // Libya
        LocationItem("ly_tripoli", "طرابلس الغرب", "Tripoli", "Tripoli", "ليبيا", "Libya", "Libye", 32.8872, 13.1913, "Africa/Tripoli", CalculationMethod.MWL),
        LocationItem("ly_benghazi", "بنغازي", "Benghazi", "Benghazi", "ليبيا", "Libya", "Libye", 32.1167, 20.0667, "Africa/Tripoli", CalculationMethod.MWL),
        LocationItem("ly_misrata", "مصراتة", "Misrata", "Misrata", "ليبيا", "Libya", "Libye", 32.3754, 15.0925, "Africa/Tripoli", CalculationMethod.MWL),

        // Sudan
        LocationItem("sd_khartoum", "الخرطوم", "Khartoum", "Khartoum", "السودان", "Sudan", "Soudan", 15.5007, 32.5599, "Africa/Khartoum", CalculationMethod.MWL),
        LocationItem("sd_omdurman", "أم درمان", "Omdurman", "Omdourman", "السودان", "Sudan", "Soudan", 15.6500, 32.4800, "Africa/Khartoum", CalculationMethod.MWL),

        // Turkey
        LocationItem("tr_istanbul", "إسطنبول", "Istanbul", "Istanbul", "تركيا", "Turkey", "Turquie", 41.0082, 28.9784, "Europe/Istanbul", CalculationMethod.TURKEY),
        LocationItem("tr_ankara", "أنقرة", "Ankara", "Ankara", "تركيا", "Turkey", "Turquie", 39.9334, 32.8597, "Europe/Istanbul", CalculationMethod.TURKEY),
        LocationItem("tr_izmir", "إزمير", "Izmir", "Izmir", "تركيا", "Turkey", "Turquie", 38.4237, 27.1428, "Europe/Istanbul", CalculationMethod.TURKEY),
        LocationItem("tr_bursa", "بورصة", "Bursa", "Bourse", "تركيا", "Turkey", "Turquie", 40.1885, 29.0610, "Europe/Istanbul", CalculationMethod.TURKEY),
        LocationItem("tr_konya", "قونية", "Konya", "Konya", "تركيا", "Turkey", "Turquie", 37.8714, 32.4846, "Europe/Istanbul", CalculationMethod.TURKEY),

        // Indonesia & Malaysia
        LocationItem("id_jakarta", "جاكرتا", "Jakarta", "Jakarta", "إندونيسيا", "Indonesia", "Indonésie", -6.2088, 106.8456, "Asia/Jakarta", CalculationMethod.MWL),
        LocationItem("id_surabaya", "سورابايا", "Surabaya", "Surabaya", "إندونيسيا", "Indonesia", "Indonésie", -7.2575, 112.7521, "Asia/Jakarta", CalculationMethod.MWL),
        LocationItem("id_bandung", "باندونغ", "Bandung", "Bandung", "إندونيسيا", "Indonesia", "Indonésie", -6.9175, 107.6191, "Asia/Jakarta", CalculationMethod.MWL),
        LocationItem("my_kl", "كوالالمبور", "Kuala Lumpur", "Kuala Lumpur", "ماليزيا", "Malaysia", "Malaisie", 3.1390, 101.6869, "Asia/Kuala_Lumpur", CalculationMethod.MWL),

        // Pakistan & Bangladesh
        LocationItem("pk_karachi", "كراتشي", "Karachi", "Karachi", "باكستان", "Pakistan", "Pakistan", 24.8607, 67.0011, "Asia/Karachi", CalculationMethod.KARACHI),
        LocationItem("pk_lahore", "لاهور", "Lahore", "Lahore", "باكستان", "Pakistan", "Pakistan", 31.5204, 74.3587, "Asia/Karachi", CalculationMethod.KARACHI),
        LocationItem("pk_islamabad", "إسلام أباد", "Islamabad", "Islamabad", "باكستان", "Pakistan", "Pakistan", 33.6844, 73.0479, "Asia/Karachi", CalculationMethod.KARACHI),
        LocationItem("bd_dhaka", "دكا", "Dhaka", "Dacca", "بنغلاديش", "Bangladesh", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka", CalculationMethod.KARACHI),

        // Europe & West
        LocationItem("uk_london", "لندن", "London", "Londres", "بريطانيا", "United Kingdom", "Royaume-Uni", 51.5074, -0.1278, "Europe/London", CalculationMethod.MWL),
        LocationItem("uk_birmingham", "برمنغهام", "Birmingham", "Birmingham", "بريطانيا", "United Kingdom", "Royaume-Uni", 52.4862, -1.8904, "Europe/London", CalculationMethod.MWL),
        LocationItem("uk_manchester", "مانشستر", "Manchester", "Manchester", "بريطانيا", "United Kingdom", "Royaume-Uni", 53.4808, -2.2426, "Europe/London", CalculationMethod.MWL),
        LocationItem("fr_paris", "باريس", "Paris", "Paris", "فرنسا", "France", "France", 48.8566, 2.3522, "Europe/Paris", CalculationMethod.FRANCE),
        LocationItem("fr_marseille", "مارسيليا", "Marseille", "Marseille", "فرنسا", "France", "France", 43.2965, 5.3698, "Europe/Paris", CalculationMethod.FRANCE),
        LocationItem("fr_lyon", "ليون", "Lyon", "Lyon", "فرنسا", "France", "France", 45.7640, 4.8357, "Europe/Paris", CalculationMethod.FRANCE),
        LocationItem("de_berlin", "برلين", "Berlin", "Berlin", "ألمانيا", "Germany", "Allemagne", 52.5200, 13.4050, "Europe/Berlin", CalculationMethod.MWL),
        LocationItem("de_frankfurt", "فرانكفورت", "Frankfurt", "Francfort", "ألمانيا", "Germany", "Allemagne", 50.1109, 8.6821, "Europe/Berlin", CalculationMethod.MWL),
        LocationItem("us_newyork", "نيويورك", "New York", "New York", "أمريكا", "United States", "États-Unis", 40.7128, -74.0060, "America/New_York", CalculationMethod.ISNA),
        LocationItem("us_losangeles", "لوس أنجلوس", "Los Angeles", "Los Angeles", "أمريكا", "United States", "États-Unis", 34.0522, -118.2437, "America/Los_Angeles", CalculationMethod.ISNA),
        LocationItem("us_chicago", "شيكاغو", "Chicago", "Chicago", "أمريكا", "United States", "États-Unis", 41.8781, -87.6298, "America/Chicago", CalculationMethod.ISNA),
        LocationItem("ca_toronto", "تورونتو", "Toronto", "Toronto", "كندا", "Canada", "Canada", 43.6532, -79.3832, "America/Toronto", CalculationMethod.ISNA),
        LocationItem("ca_montreal", "مونتريال", "Montreal", "Montréal", "كندا", "Canada", "Canada", 45.5017, -73.5673, "America/Toronto", CalculationMethod.ISNA),
        LocationItem("sg_singapore", "سنغافورة", "Singapore", "Singapour", "سنغافورة", "Singapore", "Singapour", 1.3521, 103.8198, "Asia/Singapore", CalculationMethod.MUIS),
        LocationItem("au_sydney", "سيدني", "Sydney", "Sydney", "أستراليا", "Australia", "Australie", -33.8688, 151.2093, "Australia/Sydney", CalculationMethod.MWL)
    )

    fun searchLocations(query: String, language: Language): List<LocationItem> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return allLocations
        return allLocations.filter { loc ->
            loc.nameAr.lowercase().contains(q) ||
            loc.nameEn.lowercase().contains(q) ||
            loc.nameFr.lowercase().contains(q) ||
            loc.countryAr.lowercase().contains(q) ||
            loc.countryEn.lowercase().contains(q) ||
            loc.countryFr.lowercase().contains(q)
        }
    }

    fun findById(id: String): LocationItem? {
        return allLocations.find { it.id == id }
    }

    fun getDefaultLocation(): LocationItem {
        return allLocations[0] // Makkah
    }
}

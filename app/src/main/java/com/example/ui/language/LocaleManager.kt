package com.example.ui.language

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.Language
import java.util.Locale

object LocaleManager {

    fun updateLocale(context: Context, language: Language): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun getLayoutDirection(language: Language): LayoutDirection {
        return if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    }
}

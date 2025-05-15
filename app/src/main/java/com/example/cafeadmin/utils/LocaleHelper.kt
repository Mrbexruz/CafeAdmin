package com.example.cafeadmin.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleManager {

    fun setLocale(context: Context): Context {
        return updateResources(context, getLanguage(context))
    }

    fun setNewLocale(context: Context, language: String): Context {
        saveLanguage(context, language)
        return updateResources(context, language)
    }

    private fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("lang", "uz") ?: "uz"
    }

    private fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("lang", language).apply()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            return context.createConfigurationContext(config)
        } else {
            config.locale = locale
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }
}


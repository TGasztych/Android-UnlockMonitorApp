package com.example.unlockmonitorapp

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences: SharedPreferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun saveWebhookUrl(url: String) {
        preferences.edit().putString("webhookUrl", url).apply()
    }

    fun getWebhookUrl(): String = preferences.getString("webhookUrl", "") ?: ""
}

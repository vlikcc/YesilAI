package com.yesilai.app

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "yesilai_prefs")

class YesilAIApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

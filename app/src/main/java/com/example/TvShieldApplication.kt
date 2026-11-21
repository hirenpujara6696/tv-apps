package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ShieldPreferences
import com.example.data.ShieldRepository
import com.example.vpn.DomainFilterEngine

class TvShieldApplication : Application() {

    lateinit var repository: ShieldRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        val preferences = ShieldPreferences(this)
        val filterEngine = DomainFilterEngine()
        repository = ShieldRepository(
            dao = database.shieldDao(),
            preferences = preferences,
            filterEngine = filterEngine
        )
    }
}

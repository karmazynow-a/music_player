package com.example.musicplayer

import android.app.Application
import timber.log.Timber

class MusicPlayerApplication : Application() {
    override fun onCreate() {
        Timber.plant(Timber.DebugTree())
        super.onCreate()
    }
}
package com.example.musicplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import timber.log.Timber
import java.io.File

class PlayerService :  Service () {

    var mediaPlayer : MediaPlayer = MediaPlayer()

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val path = intent!!.getStringExtra("path")
        val songFile = File(path)
        if (songFile.exists()) {
            val uri = Uri.fromFile(songFile)

            mediaPlayer.setDataSource(applicationContext, uri)
            //mediaPlayer.setOnPreparedListener{onInitPrepared()}
            mediaPlayer.prepareAsync()

            Timber.d("Preparing")
        } else {
            Timber.d("File not found: 0 " + path)
            Toast.makeText(applicationContext, "Nie znaleziono pliku!", Toast.LENGTH_SHORT).show()
        }
        return START_STICKY
    }

    fun onPause() {
        mediaPlayer.pause()
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        super.onDestroy()
    }

    fun load() {

    }
}
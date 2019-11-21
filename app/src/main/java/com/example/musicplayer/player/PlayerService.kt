package com.example.musicplayer.player

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import com.example.musicplayer.R
import timber.log.Timber
import java.io.File

class PlayerService :  Service () {

    private var mediaPlayer : MediaPlayer = MediaPlayer()
    private var currentPlaylist : MutableList<String> = mutableListOf()
    private var currentPosition : Int = 0
    private var isPrepared : Boolean = false
    private val binder : IBinder = PlayerBinder()
    private var durationHandler : Handler = Handler()
    private lateinit var notificationManager : NotificationManager


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("Service started")

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        return START_STICKY
    }

    //*******************BINDING SECTION
    //client binder
    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }

    private fun notifyChange(what : String) {
        val i = Intent(what)
        i.putExtra("path", currentPlaylist[currentPosition])
        i.putExtra("isPrepared", isPrepared)
        i.putExtra("isPlaying", mediaPlayer.isPlaying)

        if (what.equals(PROGRESS_CHANGED)){
            val songProgress = mediaPlayer.getCurrentPosition()
            val finalTime = mediaPlayer.getDuration()
            i.putExtra("progress", songProgress * 100 / finalTime)
        }

        //sendStickyBroadcast(i)
        sendBroadcast(i)
    }

    //*******************MUSIC PLAYER SECTION
    //set playlist info and currentPos
    fun open (pos : Int, playlist : MutableList<String>){
        currentPlaylist = playlist
        currentPosition = pos

        //load current song
        load()
    }

    private fun load() {
        val songPath = currentPlaylist[currentPosition]
        val songFile = File(songPath)
        if (songFile.exists()) {
            val uri = Uri.fromFile(songFile)

            if (mediaPlayer.isPlaying){
                Timber.d("Player is stopper - called for new song")
                mediaPlayer.stop()
                mediaPlayer.reset()
            }

            mediaPlayer.setDataSource(this, uri)
            mediaPlayer.setOnPreparedListener{onPrepared()}

            Timber.d("Player is preparing")
            notifyChange(PREPARED_CHANGED)
            notifyChange(TRACK_CHANGED)

            isPrepared = false
            mediaPlayer.prepareAsync()


        } else {
            Timber.d("File not found: 0 " + songPath)
        }

    }

    fun start(){
        if (!mediaPlayer.isPlaying) {
            Timber.d("Player start")
            mediaPlayer.start()
            notifyChange(PLAYING_CHANGED)
            Timber.d("is Playing: " + mediaPlayer.isPlaying)

            //TODO create valid notification
            var notification = Notification()
            notification.tickerText = getString(R.string.app_name)
            notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
            notification.icon = R.drawable.ic_cd

            val NOTIFICATION_IDFOREGROUND_SERVICE = 78945
            notificationManager.notify(NOTIFICATION_IDFOREGROUND_SERVICE, notification)
        }
    }

    fun stop(){
        mediaPlayer.pause()
        notifyChange(PLAYING_CHANGED)
    }

    fun next() {
        if (currentPosition + 1 == currentPlaylist.size){
            Timber.d ("No more songs on playlist!")
            mediaPlayer.pause()
        }
        else {
            currentPosition += 1
            Timber.d("Currently playing " + currentPlaylist.elementAt(currentPosition) + " with index " + currentPosition )
            mediaPlayer.stop()
            mediaPlayer.reset()
            load()
        }
    }

    fun prev() {
        if (currentPosition == 0){
            Timber.d ("No more songs on playlist!")
            mediaPlayer.pause()
        }
        else {
            currentPosition -= 1
            Timber.d("Currently playing " + currentPlaylist.elementAt(currentPosition) + " with index " + currentPosition )
            mediaPlayer.stop()
            mediaPlayer.reset()
            load()
        }
    }

    private fun onPrepared() {
        Timber.d("Player is prepared")

        //set progress bar updating
        val updateSeekBarTime = object: Runnable {
            override fun run() {
                try {
                    if (mediaPlayer.isPlaying) {
                        notifyChange(PROGRESS_CHANGED)
                    }
                    //repeat yourself that again in 100 miliseconds
                    durationHandler.postDelayed(this, 100)
                }
                catch (e : IllegalStateException) {
                    Timber.d("MusicPlayer is released - illegal state")
                }
            }
        }
        durationHandler.postDelayed(updateSeekBarTime, 100)

        mediaPlayer.setOnCompletionListener{next()}
        isPrepared = true
        notifyChange(PREPARED_CHANGED)
        start()
    }

    companion object {
        //state changes to be notified
        const val PROGRESS_CHANGED = "com.example.musicplayer.progresschanged"
        const val TRACK_CHANGED = "com.example.musicplayer.trackchanged"
        const val PREPARED_CHANGED = "com.example.musicplayer.preparedchanged"
        const val PLAYING_CHANGED = "com.example.musicplayer.playingchanged"
    }
}

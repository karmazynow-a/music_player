package com.example.musicplayer.player


import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.musicplayer.R
import com.example.musicplayer.SongNameResolver
import timber.log.Timber
import java.io.File

class PlayerService :  Service () {

    private var mediaPlayer : MediaPlayer
    private var currentPlaylist : MutableList<String>
    private var currentPosition : Int
    private var isPrepared : Boolean
    private var isShuffle : Boolean
    private var isReady : Boolean
    private val binder : IBinder
    private var durationHandler : Handler
    private lateinit var notificationManager : NotificationManager

    init {
        isPrepared = false
        isShuffle = false
        isReady = false
        currentPosition = 0
        currentPlaylist = mutableListOf()
        mediaPlayer = MediaPlayer()
        binder = PlayerBinder()
        durationHandler = Handler()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("Service started")

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        return START_STICKY
    }

    fun isReady() : Boolean {
        return isReady
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
        else if (what.equals(SHUFFLE_CHANGED)) {
            i.putExtra("isShuffle", isShuffle)
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

            if (mediaPlayer.isPlaying){ //TODO OR IS STOPPED
                Timber.d("Player is stopped - called for new song")
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

            val notification = NotificationCompat.Builder(this, "" )
                .setSmallIcon(R.drawable.ic_cd)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Playing " + SongNameResolver.getSongName(currentPlaylist[currentPosition]))

            // NOTIFICATION_IDFOREGROUND_SERVICE is 78945
            notificationManager.notify(78945, notification.build())
        }
    }

    fun stop(){
        mediaPlayer.pause()
        notifyChange(PLAYING_CHANGED)
    }

    fun reset(){
        mediaPlayer.stop()
        mediaPlayer.reset()
    }

    fun next() {
        if (currentPosition + 1 == currentPlaylist.size){
            Timber.d ("No more songs on playlist!")
            mediaPlayer.pause()
            notifyChange(PLAYING_CHANGED)
        }
        else {
            currentPosition += 1
            mediaPlayer.stop()
            mediaPlayer.reset()
            load()
        }
    }

    fun prev() {
        if (currentPosition == 0){
            Timber.d ("No more songs on playlist!")
            mediaPlayer.pause()
            notifyChange(PLAYING_CHANGED)
        }
        else {
            currentPosition -= 1
            mediaPlayer.stop()
            mediaPlayer.reset()
            load()
        }
    }

    fun shuffle(){
        if (isShuffle) {
            Timber.d("Playlist is sorted")
            //currentPlaylist.sort()
            currentPlaylist.sortWith(compareBy { SongNameResolver.getSongName(it) })
            isShuffle = false
        } else {
            Timber.d("Playlist is shuffled")
            currentPlaylist.shuffle()
            isShuffle = true
        }
        notifyChange(SHUFFLE_CHANGED)
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
        isReady = true
        notifyChange(PREPARED_CHANGED)
        start()
    }

    companion object {
        //state changes to be notified
        const val PROGRESS_CHANGED = "com.example.musicplayer.progresschanged"
        const val TRACK_CHANGED = "com.example.musicplayer.trackchanged"
        const val PREPARED_CHANGED = "com.example.musicplayer.preparedchanged"
        const val PLAYING_CHANGED = "com.example.musicplayer.playingchanged"
        const val SHUFFLE_CHANGED = "com.example.musicplayer.shufflechanged"
    }
}


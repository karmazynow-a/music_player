package com.example.musicplayer.player

import android.content.*
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.musicplayer.*
import com.example.musicplayer.databinding.FragmentPlayerBinding
import timber.log.Timber

class PlayerFragment : Fragment() {

    private lateinit var binding : FragmentPlayerBinding
    private lateinit var service: PlayerService
    private var isBounded : Boolean = false
    private var isPlaying : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentPlayerBinding>(
            inflater,
            R.layout.fragment_player, container, false
        )

        enableBtns (false)
        binding.playBtn.setOnClickListener { play() }
        binding.nextBtn.setOnClickListener { next() }
        binding.prevBtn.setOnClickListener { prev() }
        binding.shuffleBtn.setOnClickListener { shuffle() }
        binding.addBtn.setOnClickListener { add() }

        //navbar styling
        (activity as AppCompatActivity).supportActionBar?.title = ""

        return binding.root
    }

    //*****************************NOTIFICATION SECTION
    override fun onStart() {
        super.onStart()

        var intent = Intent(context, PlayerService::class.java)
        (activity as MainActivity).bindService(intent, connection, Context.BIND_AUTO_CREATE)

        var filter = IntentFilter()
        filter.addAction(PlayerService.PROGRESS_CHANGED)
        filter.addAction(PlayerService.PREPARED_CHANGED)
        filter.addAction(PlayerService.TRACK_CHANGED)
        filter.addAction(PlayerService.PLAYING_CHANGED)
        filter.addAction(PlayerService.SHUFFLE_CHANGED)
        (activity as MainActivity).registerReceiver(statusChange, filter)
    }

    override fun onStop() {
        (activity as MainActivity).unregisterReceiver(statusChange)
        super.onStop()
    }

    override fun onDestroy() {
        if (isBounded) {
            (activity as MainActivity).unbindService(connection)
            isBounded = false
        }
        super.onDestroy()
    }

    private var statusChange = object : BroadcastReceiver(){
        override fun onReceive(contxt: Context?, intent: Intent?) {
            val action = intent?.action
            //Timber.d ("Notification received: " + action)
            when (action) {
                PlayerService.PROGRESS_CHANGED -> {
                    binding.progressBar.progress = intent.getIntExtra("progress", 0)
                }

                PlayerService.PREPARED_CHANGED -> {
                    enableBtns(intent.getBooleanExtra("isPrepared", false))
                }

                PlayerService.TRACK_CHANGED -> {
                    setSongInfo(intent.getStringExtra("path"))
                }

                PlayerService.SHUFFLE_CHANGED -> {
                    if (intent.getBooleanExtra("isShuffle", false)){
                        binding.shuffleBtn.setColorFilter(R.color.colorAccent)
                    }
                    else {
                        binding.shuffleBtn.setColorFilter(R.color.transparent)
                    }
                }

                PlayerService.PLAYING_CHANGED -> {
                    if (intent.getBooleanExtra("isPlaying", false)){
                        Timber.d("Preparing to be paused")
                        isPlaying = true
                        binding.playBtn.setImageResource(R.drawable.ic_pause)
                    } else {
                        Timber.d("Preparing to be played")
                        isPlaying = false
                        binding.playBtn.setImageResource(R.drawable.ic_play)
                    }
                }
            }
        }
    }

    private var connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            var binder = (p1 as PlayerService.PlayerBinder)
            service = binder.getService()
            isBounded = true

            //open
            val args = PlayerFragmentArgs.fromBundle(arguments!!)
            val playlist = (activity as MainActivity).getList(args.playlist)

            //TODO change argument to index, not song
            service.open( playlist.indexOf(args.songPath), playlist )
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBounded = false
        }
    }

    //*************************MUSIC PLAYER SECTION
    private fun play() {
        if (!isPlaying) start()
        else pause()
    }

    private fun start(){
        Timber.d("Start")
        service.start()
    }

    private fun pause(){
        Timber.d("Pause")
        service.stop()
    }

    private fun next(){
        service.next()
    }

    private fun prev(){
        service.prev()
    }

    private fun shuffle(){
        service.shuffle()
    }

    private fun add(){
        //TODO add to selected playlist
    }

    private fun setSongInfo( path : String ) {
        var mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource( path )
        var author = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        if (author.isNullOrEmpty()) author = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
        binding.authorName.text = if (author.isNullOrEmpty()) "Nieznany" else author

        val name = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        binding.songName.text = if (name.isNullOrEmpty()) getNameFromPath(path) else name
    }

    private fun enableBtns ( enable : Boolean ){
        binding.playBtn.isEnabled = enable
        binding.nextBtn.isEnabled = enable
        binding.prevBtn.isEnabled = enable
    }
}

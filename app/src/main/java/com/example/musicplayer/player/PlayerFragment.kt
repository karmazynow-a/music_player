package com.example.musicplayer.player

import android.content.*
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.musicplayer.*
import com.example.musicplayer.databinding.FragmentPlayerBinding
import timber.log.Timber

class PlayerFragment : Fragment() {

    private lateinit var binding : FragmentPlayerBinding
    private lateinit var service: PlayerService
    private var isBounded : Boolean
    private lateinit var viewModel : MainViewModel

    init {
        isBounded = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentPlayerBinding>(
            inflater,
            R.layout.fragment_player, container, false
        )

        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        enableBtns (false)
        binding.playBtn.setOnClickListener { play() }
        binding.nextBtn.setOnClickListener { next() }
        binding.prevBtn.setOnClickListener { prev() }
        binding.shuffleBtn.setOnClickListener { shuffle() }
        binding.addBtn.setOnClickListener { add() }
        binding.progressBar.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {
                service.setProgress(p0!!.progress)
            }
        })

        //set proper buttons' states
        if ( !viewModel.isPlaying.value!! ){
            binding.playBtn.setImageResource(R.drawable.ic_play)
        } else {
            binding.playBtn.setImageResource(R.drawable.ic_pause)
        }

        if ( viewModel.isShuffle.value!! ){
            var value = TypedValue()
           context!!.theme.resolveAttribute (R.attr.colorAccent, value, true)
            binding.shuffleBtn.setColorFilter(value.data)
        } else {
            binding.shuffleBtn.setColorFilter(R.color.btnBlack)
        }

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
        filter.addAction(PlayerService.ALL)
        (activity as MainActivity).registerReceiver(statusChange, filter)
    }

    override fun onStop() {
        (activity as MainActivity).unregisterReceiver(statusChange)
        super.onStop()
    }

    override fun onDestroyView() {
        if (isBounded) {
            (activity as MainActivity).unbindService(connection)
            isBounded = false
        }
        super.onDestroyView()
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
                    viewModel.setCurrentSongFromPath(intent.getStringExtra("path"))
                    Timber.d("Current track is " + viewModel.currentSong.value)
                }

                PlayerService.SHUFFLE_CHANGED -> {
                    if (intent.getBooleanExtra("isShuffle", false)){
                        viewModel.setIsShuffle(true)
                        var value = TypedValue()
                        context!!.theme.resolveAttribute (R.attr.colorAccent, value, true)
                        binding.shuffleBtn.setColorFilter(value.data)
                    }
                    else {
                        viewModel.setIsShuffle(false)
                        binding.shuffleBtn.setColorFilter(R.color.btnBlack)
                    }
                }

                PlayerService.PLAYING_CHANGED -> {
                    if (intent.getBooleanExtra("isPlaying", false)){
                        Timber.d("Preparing to be paused")
                        viewModel.setIsPlaying(true)
                        binding.playBtn.setImageResource(R.drawable.ic_pause)
                    } else {
                        Timber.d("Preparing to be played")
                        viewModel.setIsPlaying(false)
                        binding.playBtn.setImageResource(R.drawable.ic_play)
                    }
                }

                PlayerService.ALL -> {
                    Timber.d("inPlaying: " + intent.getBooleanExtra("isPlaying", false)
                            + "songPath" + setSongInfo(intent.getStringExtra("path")))
                    //change playing btn
                    if (intent.getBooleanExtra("isPlaying", false)){
                        viewModel.setIsPlaying(true)
                        binding.playBtn.setImageResource(R.drawable.ic_pause)
                    } else {
                        viewModel.setIsPlaying(false)
                        binding.playBtn.setImageResource(R.drawable.ic_play)
                    }

                    //set current track
                    setSongInfo(intent.getStringExtra("path"))
                    viewModel.setCurrentSongFromPath(intent.getStringExtra("path"))

                    //set progress
                    binding.progressBar.progress = intent.getIntExtra("progress", 0)

                    enableBtns(intent.getBooleanExtra("isPrepared", false))
                }
            }
        }
    }

    private var connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            var binder = (p1 as PlayerService.PlayerBinder)
            service = binder.getService()
            isBounded = true
            Timber.d("Loading song " + viewModel.currentSong.value)

            service.open(viewModel.currentSong.value!!, viewModel.currentPlaylist.value!!)

        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBounded = false
        }
    }

    //*************************MUSIC PLAYER SECTION
    private fun play() {
        if (!viewModel.isPlaying.value!!) start()
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
        var popUp = PopupMenu(context, binding.addBtn)

        for ( name in viewModel.getPlaylistNames()){
            popUp.menu.add(name)
        }

        popUp.setOnMenuItemClickListener { item: MenuItem? ->
            viewModel.addSongToPlaylist( item?.title.toString() )
            true
        }

        popUp.show()
    }

    private fun setSongInfo( path : String ) {
        var mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource( path )
        var author = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        if (author.isNullOrEmpty()) author = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
        binding.authorName.text = if (author.isNullOrEmpty()) "Nieznany" else author

        val name = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        binding.songName.text = if (name.isNullOrEmpty()) SongNameResolver.getNameFromPath(path) else name
    }

    private fun enableBtns ( enable : Boolean ){
        binding.playBtn.isEnabled = enable
        binding.nextBtn.isEnabled = enable
        binding.prevBtn.isEnabled = enable
    }
}

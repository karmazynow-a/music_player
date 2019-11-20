package com.example.musicplayer

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.musicplayer.databinding.FragmentPlayerBinding
import timber.log.Timber
import java.io.File

class PlayerFragment : Fragment() {

    private lateinit var binding : FragmentPlayerBinding
    private lateinit var mediaPlayer : MediaPlayer
    private var currentSong : String = ""
    private var isPlaying : Boolean = false
    private var songProgress : Int = 0
    private lateinit var durationHandler : Handler

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

        //navbar styling
        (activity as AppCompatActivity).supportActionBar?.title = ""

        //get current song path
        val args = PlayerFragmentArgs.fromBundle(arguments!!)
        currentSong = args.songPath

        mediaPlayer = MediaPlayer()

        load()

        return binding.root
    }

    private fun load (){
        if (ContextCompat.checkSelfPermission( context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context, "Brak zezwolenia na dostęp do dysku", Toast.LENGTH_SHORT).show()

            //ask for data storage permission
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this.requireActivity(), permissions,0)
        }
        else {
            enableBtns(false)

            val songFile = File(currentSong)
            if (songFile.exists()) {
                val uri = Uri.fromFile(songFile)

                mediaPlayer.setDataSource(context!!, uri)
                mediaPlayer.setOnPreparedListener{onInitPrepared()}
                mediaPlayer.prepareAsync()

                var mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(currentSong)
                var author = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                if (author.isNullOrEmpty()) author = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                binding.authorName.text = if (author.isNullOrEmpty()) "Nieznany" else author

                val name = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                binding.songName.text = if (name.isNullOrEmpty()) getNameFromPath(currentSong) else name

                Timber.d("Preparing")
            } else {
                Timber.d("File not found: 0 " + currentSong)
                Toast.makeText(context, "Nie znaleziono pliku!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onInitPrepared (){
        Timber.d("Player is prepared to initialization")

        //preparing based on previous state
        //TODO - change according to real state
        start()

        //progress bar
        val updateSeekBarTime = object: Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    songProgress = mediaPlayer.getCurrentPosition()
                    val finalTime = mediaPlayer.getDuration()

                    binding.progressBar.setProgress(songProgress * 100 / finalTime)
                }
                //repeat yourself that again in 100 miliseconds
                durationHandler.postDelayed(this, 100)
            }
        }

        //set normal onPrepared
        mediaPlayer.setOnPreparedListener{onPrepared()}
        //set next song mechanism
        mediaPlayer.setOnCompletionListener{next()}

        durationHandler = Handler()
        durationHandler.postDelayed(updateSeekBarTime, 100)

        enableBtns(true)
    }

    private fun onPrepared (){
        Timber.d("Player is prepared")
        binding.playBtn.isEnabled = true
    }

    private fun play() {
        if (!isPlaying) start()
        else pause()
    }

    private fun start(){
        Timber.d("Start")
        //mediaPlayer.seekTo(songProgress)
        mediaPlayer.start()
        isPlaying = true
        binding.playBtn.setImageResource(R.drawable.ic_pause)
    }

    private fun pause(){
        Timber.d("Pause")
        mediaPlayer.pause()
        isPlaying = false
        binding.playBtn.setImageResource(R.drawable.ic_play)
    }

    private fun next(){
        currentSong = (activity as MainActivity).getNextSong()
        pause()
        if (currentSong.isNullOrEmpty()){
            Timber.d("Last song")
            //TODO stop media player
        }
        else {
            mediaPlayer.stop()
            mediaPlayer.reset()
            load()
        }
    }

    private fun prev(){
        currentSong = (activity as MainActivity).getPrevSong()
        pause()
        if (currentSong.isNullOrEmpty()){
            Timber.d("First song")
            //TODO stop media player
        }
        else {
            mediaPlayer.stop()
            mediaPlayer.reset()
            load()
        }
    }

    private fun enableBtns ( enable : Boolean ){
        binding.playBtn.isEnabled = enable
        binding.nextBtn.isEnabled = enable
        binding.prevBtn.isEnabled = enable
    }
}

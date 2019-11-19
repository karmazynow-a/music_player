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

const val KEY_CURRENT_SONG= "current_song_key"
const val KEY_IS_PLAYING= "in_playing_key"

class PlayerFragment : Fragment() {

    private lateinit var binding : FragmentPlayerBinding
    private lateinit var mediaPlayer : MediaPlayer
    private var currentSong : String = ""
    private var isPlaying : Boolean = false
    private var songProgress : Int = 0
    private lateinit var durationHandler : Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            currentSong = savedInstanceState.getString(KEY_CURRENT_SONG, "")
            isPlaying = savedInstanceState.getBoolean(KEY_IS_PLAYING, false)
        }

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentPlayerBinding>(
            inflater,
            R.layout.fragment_player, container, false
        )

        binding.playBtn.isEnabled = false

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
        binding.playBtn.isEnabled = false
        if (ContextCompat.checkSelfPermission( context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context, "Brak zezwolenia na dostęp do dysku", Toast.LENGTH_SHORT).show()

            //ask for data storage permission
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this.requireActivity(), permissions,0)
        }
        else {
            val songFile = File(currentSong)
            if (songFile.exists()) {
                val uri = Uri.fromFile(songFile)

                mediaPlayer.setDataSource(context!!, uri)
                mediaPlayer.setOnPreparedListener{onInitPrepared()}
                mediaPlayer.prepareAsync()

                var mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(currentSong)
                val author = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
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

        //set normal onPrepared
        mediaPlayer.setOnPreparedListener{onPrepared()}

        //preparing based on previous state
        //TODO - change according to real state
        Timber.d("Start")
        mediaPlayer.start()
        isPlaying = true
        binding.playBtn.setImageResource(R.drawable.ic_pause)

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

        durationHandler = Handler()
        durationHandler.postDelayed(updateSeekBarTime, 100)

        binding.playBtn.setOnClickListener{play()}
        binding.playBtn.isEnabled = true
    }

    private fun onPrepared (){
        Timber.d("Player is prepared")
        binding.playBtn.isEnabled = true
    }

    private fun play() {
        if (!isPlaying) {
            Timber.d("Start")
            //mediaPlayer.seekTo(songProgress)
            mediaPlayer.start()
            isPlaying = true
            binding.playBtn.setImageResource(R.drawable.ic_pause)
        } else {
            Timber.d("Pause")
            mediaPlayer.pause()
            isPlaying = false
            binding.playBtn.setImageResource(R.drawable.ic_play)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_PLAYING, mediaPlayer.isPlaying())
        outState.putString(KEY_CURRENT_SONG, currentSong)

        super.onSaveInstanceState(outState)
    }
}

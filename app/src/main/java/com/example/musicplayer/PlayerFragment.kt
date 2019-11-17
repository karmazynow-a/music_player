package com.example.musicplayer

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
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
    private var isPrepared : Boolean = false

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

        binding = DataBindingUtil.inflate<com.example.musicplayer.databinding.FragmentPlayerBinding>(
            inflater,
            R.layout.fragment_player, container, false
        )

        //navbar styling
        (activity as AppCompatActivity).supportActionBar?.title = ""

        mediaPlayer = MediaPlayer()

        //TODO change to real progress
        binding.progressBar.setProgress(50, true)
        load()
        //if (isPlaying) play()
        binding.playBtn.setOnClickListener{play()}


        return binding.root
    }

    private fun load (){
        isPrepared = false
        if (ContextCompat.checkSelfPermission( context!!, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context, "Brak zezwolenia na dostęp do dysku", Toast.LENGTH_SHORT).show()

            //ask for data storage permission
            val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this.requireActivity(), permissions,0)
        }
        else {
            //TODO get songfile from current song
            var songFile = File("/sdcard/feng_suave_sink_into_the_floor.mp3")
            if (songFile.exists()) {
                val uri = Uri.fromFile(songFile)
                //mediaPlayer = MediaPlayer.create(context!!, uri)
                mediaPlayer.setDataSource(context, uri)
                mediaPlayer.setOnPreparedListener{onPrepared()}
                mediaPlayer.prepareAsync()

                //TODO set name, author etc
                Timber.d("Preparing")
            } else {
                Toast.makeText(context, "Nie znaleziono pliku!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onPrepared (){
        Timber.d("Player is prepared")
        isPrepared = true
    }

    private fun play () {
        if (isPrepared) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start()
                binding.playBtn.setImageResource(R.drawable.ic_pause)
            } else {
                mediaPlayer.stop()
                binding.playBtn.setImageResource(R.drawable.ic_play)
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_PLAYING, mediaPlayer.isPlaying())
        outState.putString(KEY_CURRENT_SONG, currentSong)

        super.onSaveInstanceState(outState)
    }
}

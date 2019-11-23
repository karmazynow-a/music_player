package com.example.musicplayer

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaMetadataRetriever
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import java.io.File
import java.io.FilenameFilter


//viewModel shared between Playlist and Player
class MainViewModel( application: Application) : AndroidViewModel(application){
    private var sharedPref: SharedPreferences
    private var path: String

    private val _currentPlaylist: MutableLiveData<MutableList<String>> = MutableLiveData()
    val currentPlaylist : LiveData<MutableList<String>>
        get() = _currentPlaylist

    private val _currentPlaylistName: MutableLiveData<String> = MutableLiveData()
    val currentPlaylistName : LiveData<String>
        get() = _currentPlaylistName

    private val _currentSong = MutableLiveData<Int>()
    val currentSong : LiveData<Int>
        get() = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying : LiveData<Boolean>
        get() = _isPlaying

    private val _isShuffle = MutableLiveData<Boolean>()
    val isShuffle : LiveData<Boolean>
        get() = _isShuffle

    init {
        Timber.d("Create ViewModel")

        //get path from shared preferences
        sharedPref = application.getSharedPreferences(application.packageName, Context.MODE_PRIVATE)
        path = sharedPref.getString("path", "/sdcard/")!!

        _currentSong.value = sharedPref.getInt("songIndex", 0)
        _currentPlaylistName.value = sharedPref.getString("playlist", "Wszystkie utwory")!!

        Timber.d("Song index is " + _currentSong.value + " playlist is " + _currentPlaylistName.value)

        _currentPlaylist.value = getAllSongs(path)
        _isPlaying.value = false
        _isShuffle.value = false
    }

    override fun onCleared() {
        Timber.d("Destroy ViewModel")
        super.onCleared()
        val editor: SharedPreferences.Editor = sharedPref.edit()

        Timber.d("SAVING Song index is " + _currentSong.value + " playlist is " + _currentPlaylistName.value)
        editor.putString("path", path)
        editor.putString("playlist", _currentPlaylistName.value)
        editor.putInt("songIndex", _currentSong.value?:0)
        editor.apply()
    }

    private fun getAllSongs( path : String ) : MutableList<String> {
        var songs = mutableListOf<String>()
        val home = File(path)
        Timber.d("Loading currentSongs from " + path)
        if (home.listFiles(FileExtensionFilter()) != null) {
            for (file in home.listFiles(FileExtensionFilter())) {
                songs.add(file.path)
            }
        }
        songs.sortWith(compareBy { getSongName(it) })
        return songs
    }

    fun getPlaylistNames() : MutableList<String> {
        var playlist = mutableListOf("Wybierz playlistę:", "Wszystkie utwory")
        //TODO get playlist names from JSON
        return playlist
    }

    fun setCurrentSong ( index : Int ) {
        _currentSong.value = index
    }

    fun setCurrentSongFromPath ( path : String ) {
        val index = _currentPlaylist.value!!.indexOf(path)
        _currentSong.value = index
    }

    fun setCurrentPlaylist ( playlist : MutableList<String> ){
        _currentPlaylist.value = playlist
    }

    fun setCurrentPlaylistName ( name : String ){
        _currentPlaylistName.value = name
    }

    fun setIsPlaying ( v : Boolean ){
        _isPlaying.value = v
    }

    fun setIsShuffle ( v : Boolean ){
        _isShuffle.value = v
    }
}


fun getNameFromPath(name : String) : String {
    return name.split("/").last().split(".").first()
}

fun getSongName(path : String) : String {
    var mediaMetadataRetriever = MediaMetadataRetriever()
    mediaMetadataRetriever.setDataSource( path )
    return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?: getNameFromPath(path)
}

internal class FileExtensionFilter : FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return name.endsWith(".mp3") || name.endsWith(".MP3")
    }
}
package com.example.musicplayer

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaMetadataRetriever
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FilenameFilter
import java.lang.Exception
import kotlin.contracts.contract


//viewModel shared between Playlist and Player
class MainViewModel( application: Application) : AndroidViewModel(application){
    private var sharedPref: SharedPreferences
    private var path: String
    private var playlistData : JSONObject

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

        if(sharedPref.getString("playlistData","").isNullOrEmpty()){
            //create new json object
            playlistData = JSONObject()
            playlistData.put("playlistNames", JSONArray())
        }
        else {
            playlistData = JSONObject(sharedPref.getString("playlistData",""))
        }

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
        editor.putString("playlistData", playlistData.toString())
        editor.apply()
    }

    fun getAllSongs( p : String = path) : MutableList<String> {
        var songs = mutableListOf<String>()
        val home = File(p)
        Timber.d("Loading currentSongs from " + path)
        if (home.listFiles(FileExtensionFilter()) != null) {
            for (file in home.listFiles(FileExtensionFilter())) {
                songs.add(file.path)
            }
        }
        songs.sortWith(compareBy { SongNameResolver.getSongName(it)})
        return songs
    }

    fun getPlaylistSongs(name : String) : MutableList<String>{
        var songs = mutableListOf<String>()

        if (name.equals("Wszystkie utwory")) return getAllSongs()
        else {
            var jsonSongs = playlistData.getJSONArray("playlistNames")
            if(jsonSongs != null) {
                for (i in 0 until jsonSongs.length()) {
                    if ( jsonSongs.getJSONObject(i).getString("name").equals(name) ){
                        var songsArr = jsonSongs.getJSONObject(i).getJSONArray("songs")
                        for ( j in 0 until songsArr.length())
                            songs.add(songsArr.getString(j))
                    }
                }
            }
        }
        Timber.d("Songs are " + songs.toString())

        return songs
    }

    fun getPlaylistNames() : MutableList<String> {
        var playlist = mutableListOf("Wybierz playlistę:", "Wszystkie utwory")

        var jsonNames = playlistData.getJSONArray("playlistNames")
        if(jsonNames != null) {
            for (i in 0 until jsonNames.length()) {
                var list = jsonNames.getJSONObject(i)
                var name = jsonNames.getJSONObject(i).getString("name")
                Timber.d("PLAYLISTA: " + name)
                playlist.add(name)
            }
        }

        Timber.d("Playlists are " + playlist.toString())

        return playlist
    }

    fun setPlaylist(name : String){
        _currentPlaylistName.value = name
        _currentPlaylist.value = getPlaylistSongs(name)
    }

    fun createNewPlaylist(name : String, songs : MutableList<Int>) {
        Timber.d("Create playlist " + name + " with songs: " + songs.toString())
        if(getPlaylistNames().contains(name)){
            throw Exception("Playlista o tej nazwie już istnieje!")
        }
        else{
            var songsArray = JSONArray()
            for ( index in songs ){
                songsArray.put(getAllSongs()[index])
            }
            var newPlaylist = JSONObject()
            newPlaylist.put("name", name)
            newPlaylist.put("songs", songsArray)
            playlistData.getJSONArray("playlistNames").put(newPlaylist)
        }
        Timber.d("JSON: " + playlistData.toString())
    }

    fun addSongToPlaylist ( playlistName : String? ) {
        val songPath = currentPlaylist.value!![currentSong.value!!]
        Timber.d("Adding " + songPath + " to " + playlistName)

        var jsonSongs = playlistData.getJSONArray("playlistNames")
        if(jsonSongs != null) {
            for (i in 0 until jsonSongs.length()) {
                if ( jsonSongs.getJSONObject(i).getString("name").equals(playlistName) ){
                    jsonSongs.getJSONObject(i).getJSONArray("songs").put(songPath)
                    break
                }
            }
        }
    }

    fun setCurrentSong ( index : Int ) {
        _currentSong.value = index
    }

    fun setCurrentSongFromPath ( path : String ) {
        val index = _currentPlaylist.value!!.indexOf(path)
        _currentSong.value = index
    }

    fun setIsPlaying ( v : Boolean ){
        _isPlaying.value = v
    }

    fun setIsShuffle ( v : Boolean ){
        _isShuffle.value = v
    }
}

//class to store mapping between song names and paths
class SongNameResolver{
    companion object{
        private var pathToName = mutableMapOf<String, String>()
        fun getSongName (path : String) : String{
            if (!pathToName.containsKey(path)){
                var mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource( path )
                pathToName[path] = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?: getNameFromPath(path)
            }
            return pathToName[path]!!
        }

        fun getNameFromPath(name : String) : String {
            return name.split("/").last().split(".").first()
        }
    }
}

internal class FileExtensionFilter : FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return name.endsWith(".mp3") || name.endsWith(".MP3")
    }
}
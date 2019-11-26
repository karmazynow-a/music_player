package com.example.musicplayer

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaMetadataRetriever
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FilenameFilter
import java.lang.Exception


//viewModel shared between Playlist and Player
class MainViewModel( application: Application) : AndroidViewModel(application){
    private var sharedPref: SharedPreferences
    private var path: String
    private var playlistData : JSONObject

    //current playlist with songs
    private val _currentPlaylist: MutableLiveData<MutableList<String>> = MutableLiveData()
    val currentPlaylist : LiveData<MutableList<String>>
        get() = _currentPlaylist

    //name of current playlist
    private val _currentPlaylistName: MutableLiveData<String> = MutableLiveData()
    val currentPlaylistName : LiveData<String>
        get() = _currentPlaylistName

    //position of current song on playlist
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

        Timber.d("Current song index is %s and current playlist is %s",_currentSong.value , _currentPlaylistName.value)

        _currentPlaylist.value = getAllSongs(path)
        _isPlaying.value = false
        _isShuffle.value = false
    }

    override fun onCleared() {
        Timber.d("Destroy ViewModel")
        super.onCleared()
        val editor: SharedPreferences.Editor = sharedPref.edit()

        Timber.d("SAVING Song index is %s playlist is %s" ,_currentSong.value, _currentPlaylistName.value)
        editor.putString("path", path)
        editor.putString("playlist", _currentPlaylistName.value)
        editor.putInt("songIndex", _currentSong.value?:0)
        editor.putString("playlistData", playlistData.toString())
        editor.apply()
    }

    fun getAllSongs( p : String = path) : MutableList<String> {
        val songs = mutableListOf<String>()
        val home = File(p)
        Timber.d("Loading .mp3 files from %s", path)
        if (home.listFiles(FileExtensionFilter()) != null) {
            for (file in home.listFiles(FileExtensionFilter())) {
                songs.add(file.path)
            }
        }
        songs.sortWith(compareBy { SongNameResolver.getSongName(it)})
        return songs
    }

    private fun getPlaylistSongs(name : String) : MutableList<String>{
        val songs = mutableListOf<String>()

        if (name.equals("Wszystkie utwory")) return getAllSongs()
        else {
            val jsonSongs = playlistData.getJSONArray("playlistNames")
            if(jsonSongs != null) {
                for (i in 0 until jsonSongs.length()) {
                    if ( jsonSongs.getJSONObject(i).getString("name").equals(name) ){
                        val songsArr = jsonSongs.getJSONObject(i).getJSONArray("songs")
                        for ( j in 0 until songsArr.length())
                            songs.add(songsArr.getString(j))
                    }
                }
            }
        }
        Timber.d("Found songs from playslist %s: %s", name, songs.toString())

        return songs
    }

    fun getPlaylistNames() : MutableList<String> {
        val playlist = mutableListOf("Wybierz playlistę:", "Wszystkie utwory")

        val jsonNames = playlistData.getJSONArray("playlistNames")
        if(jsonNames != null) {
            for (i in 0 until jsonNames.length()) {
                var name = jsonNames.getJSONObject(i).getString("name")
                playlist.add(name)
            }
        }

        Timber.d("Found playlists: %s", playlist.toString())

        return playlist
    }

    fun createNewPlaylist(name : String, songs : MutableList<Int>) {
        Timber.d("Create playlist %s with songs: %s", name, songs.toString())
        if(getPlaylistNames().contains(name)){
            throw Exception("Playlista o tej nazwie już istnieje!")
        }
        else{
            val songsArray = JSONArray()
            for ( index in songs ){
                songsArray.put(getAllSongs()[index])
            }
            val newPlaylist = JSONObject()
            newPlaylist.put("name", name)
            newPlaylist.put("songs", songsArray)
            playlistData.getJSONArray("playlistNames").put(newPlaylist)
        }
    }

    fun deletePlaylist(pos : Int){
        // skipping first two positions - helper text and all songs cannot be deleted
        val playlistName = getPlaylistNames()[pos + 2]
        val jsonSongs = playlistData.getJSONArray("playlistNames")
        if(jsonSongs != null) {
            for (i in 0 until jsonSongs.length()) {
                if ( jsonSongs.getJSONObject(i).getString("name").equals(playlistName) ){
                    Timber.d("Removing %s", jsonSongs.getJSONObject(i).getString("name"))
                    jsonSongs.remove(i)
                }
            }
        }
    }

    fun addSongToPlaylist ( playlistName : String? ) {
        val songPath = currentPlaylist.value!![currentSong.value!!]
        Timber.d("Adding song %s to playlist %s", songPath, playlistName)

        val jsonSongs = playlistData.getJSONArray("playlistNames")
        if(jsonSongs != null) {
            for (i in 0 until jsonSongs.length()) {
                if ( jsonSongs.getJSONObject(i).getString("name").equals(playlistName) ){
                    jsonSongs.getJSONObject(i).getJSONArray("songs").put(songPath)
                    break
                }
            }
        }
    }

    fun setPlaylist(name : String){
        _currentPlaylistName.value = name
        _currentPlaylist.value = getPlaylistSongs(name)
        _currentSong.value = 0
    }

    fun setPath(path : String){
        this.path = path
        _currentPlaylist.value = getAllSongs(path)
        _currentSong.value = 0
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
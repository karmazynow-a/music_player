package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.player.PlayerService
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FilenameFilter
import timber.log.Timber


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private var currentPlaylist : String = "Wszystkie utwory"
    var currentSongIndex : Int = 0
    var currentSongs : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref : SharedPreferences = getPreferences( Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        if (sharedPref.getInt("theme", 0) == 0){
            editor.putInt("theme", R.style.Gradient_Theme_Teal)
        }
        if (sharedPref.getString("path", "").isNullOrEmpty()){
            editor.putString("path", Environment.getExternalStorageDirectory().path)
        }
        editor.apply()

        setTheme(sharedPref.getInt("theme", 0))

        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout

        val navController = this.findNavController(R.id.myNavHostFragment)

        //add hamburger
        NavigationUI.setupActionBarWithNavController(this,navController, drawerLayout)

        //set drawer
        NavigationUI.setupWithNavController(binding.navView, navController)

        //set on item in drawer selected
        binding.navView.setNavigationItemSelectedListener(this)

        //load currentSongs
        getAllSongs(sharedPref.getString("path", Environment.getExternalStorageDirectory().path)!!)

        //ask for data storage permission
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions,0)

        //start player service
        startService( Intent(this, PlayerService::class.java))
        }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)
        return NavigationUI.onNavDestinationSelected(menuItem, this.findNavController(R.id.myNavHostFragment))
                || super.onOptionsItemSelected(menuItem)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun getAllSongs( path : String ) {
        val home = File(path)
        Timber.d("Loading currentSongs from " + path)
        if (home.listFiles(FileExtensionFilter()) != null) {
            for (file in home.listFiles(FileExtensionFilter())) {
                currentSongs.add(file.path)
            }
        }
    }

    fun getPlaylistNames() : MutableList<String> {
        var playlist = mutableListOf("Wybierz playlistę:", "Wszystkie utwory")
        //TODO get playlist names from JSON
        return playlist
    }

    fun getList(playlist : String) : MutableList<String>{
        var playlistNames = getPlaylistNames()

        if (playlist != "Wszystkie utwory"){
            val index = playlistNames.indexOf(playlist)
            currentPlaylist = playlistNames[index]

            //TODO get playlist from JSON to currentSongs
            return mutableListOf()
        }
        else {
            currentPlaylist = playlistNames[1]
            return this.currentSongs
        }
    }
}

fun getNameFromPath(name : String) : String {
    return name.split("/").last().split(".").first()
}

internal class FileExtensionFilter : FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return name.endsWith(".mp3") || name.endsWith(".MP3")
    }
}
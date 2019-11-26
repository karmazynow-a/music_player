package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
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
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref= getPreferences( Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        if (sharedPref.getInt("theme", 0) == 0){
            editor.putInt("theme", R.style.Gradient_Theme_Teal)
        }
        if (!sharedPref.getBoolean("permissionGranted", false)){
            //ask for data storage permission
            val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.putBoolean("permissionGranted", true)
                editor.apply()
                val i = Intent(baseContext, MainActivity::class.java)
                startActivity(i)
            } else {
                ActivityCompat.requestPermissions(this, permissions, 0)
            }
        }
    }
}
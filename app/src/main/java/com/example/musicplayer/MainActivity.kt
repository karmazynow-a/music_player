package com.example.musicplayer

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import androidx.lifecycle.ViewModel



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setTheme(R.style.gradient_t_dp)
        val sharedPref : SharedPreferences = getPreferences( Context.MODE_PRIVATE)
        if (sharedPref.getInt("theme", 0) == 0){
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putInt("theme", R.style.Gradient_Theme_Teal)
            editor.apply()
        }

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
}

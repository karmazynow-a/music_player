package com.example.musicplayer

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.musicplayer.databinding.FragmentThemeBinding


class ThemeFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<com.example.musicplayer.databinding.FragmentThemeBinding>(
            inflater,
            R.layout.fragment_theme, container, false
        )

        //navbar styling
        (activity as AppCompatActivity).supportActionBar?.title = "Wybierz motyw"

        //theme buttons binding
        binding.themePt.setOnClickListener{view : View -> changeTheme(R.style.Gradient_Theme_Teal, view) }
        binding.themePa.setOnClickListener{ view : View -> changeTheme(R.style.Gradient_Theme_Pink, view) }
        binding.themeBg.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Grey, view)}
        binding.themeCosmic.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Cosmic, view)}
        binding.themeBrady.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Brady, view)}
        binding.themeDawn.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Dawn, view)}
        binding.themeJanipur.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Janipur, view)}
        binding.themeMild.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Mild, view)}
        binding.themeRadar.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Radar, view)}
        binding.themeForrest.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Forrest, view)}
        binding.themeMorning.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Morning, view)}
        binding.themeSun.setOnClickListener { view : View -> changeTheme(R.style.Gradient_Theme_Sun, view)}

        return binding.root
    }

    fun changeTheme(theme : Int, view : View){
        val sharedPref: SharedPreferences = activity!!.getPreferences(Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt("theme", theme)
        editor.apply()
        (activity as AppCompatActivity).recreate()
        view.findNavController().navigate(R.id.action_themeFragment_to_playerFragment)
    }

}

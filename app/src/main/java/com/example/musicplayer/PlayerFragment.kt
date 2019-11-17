package com.example.musicplayer

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*


class PlayerFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
/*
        Log.v("Theme",(activity as AppCompatActivity).theme.toString())
        (activity as AppCompatActivity).setTheme(R.style.Gradient_Theme_Pink )
        Log.v("Theme",(activity as AppCompatActivity).theme.toString())

        (activity as AppCompatActivity).recreate()
*/
        super.onCreate(savedInstanceState)
/*
        Log.v("Theme",(activity as AppCompatActivity).theme.toString())
        (activity as AppCompatActivity).setTheme(R.style.Gradient_Theme_Pink )
        Log.v("Theme",(activity as AppCompatActivity).theme.toString())
*/
        //(activity as AppCompatActivity).recreate()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DataBindingUtil.inflate<com.example.musicplayer.databinding.FragmentPlayerBinding>(
            inflater,
            R.layout.fragment_player, container, false
        )

        //navbar styling
        (activity as AppCompatActivity).supportActionBar?.title = ""

        //TODO change to real progress
        binding.progressBar.setProgress(50, true)

        return binding.root
    }

}

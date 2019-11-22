package com.example.musicplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


//viewModel shared between Playlist and Player
class MainViewModel( application: Application) : AndroidViewModel(application){
    private var playlist: MutableLiveData<MutableList<String>> = MutableLiveData()

    init {
        playlist.value = mutableListOf<String>()
    }

    override fun onCleared() {
        super.onCleared()
    }
}
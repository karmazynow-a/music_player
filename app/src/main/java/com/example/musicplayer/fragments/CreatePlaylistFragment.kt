package com.example.musicplayer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.musicplayer.viewModels.MainViewModel
import com.example.musicplayer.R
import com.example.musicplayer.viewModels.SongNameResolver
import com.example.musicplayer.databinding.FragmentCreatePlaylistBinding


class CreatePlaylistFragment : Fragment() {
    private lateinit var binding : FragmentCreatePlaylistBinding
    private lateinit var viewModel : MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<com.example.musicplayer.databinding.FragmentCreatePlaylistBinding>(
            inflater,
            R.layout.fragment_create_playlist, container, false
        )

        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        //load songs to list
        createSongList()

        binding.createPlaylistBtn.setOnClickListener{ createPlaylist() }

        (activity as AppCompatActivity).supportActionBar?.title = "Stwórz playlistę"

        return binding.root
    }

    private fun createSongList () {
        val songs = viewModel.getAllSongs()

        val listItems = arrayOfNulls<String>(songs.size)
        for ( i in 0 until songs.size){
            listItems[i] = SongNameResolver.getSongName(songs.elementAt(i))
        }

        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_multiple_choice, listItems)
        binding.chooseSongList.adapter = adapter
    }

    private fun createPlaylist() {
        val playlistName = binding.newPlaylistName.text.toString()
        val chosenSongs = mutableListOf<Int>()

        val checkedSongs = binding.chooseSongList.checkedItemPositions
        for (pos in 0 until binding.chooseSongList.count - 1){
            if(checkedSongs.get(pos)){
                chosenSongs.add(pos)
            }
        }

        try {
            viewModel.createNewPlaylist(playlistName, chosenSongs)
        } catch (e : java.lang.Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }

        view!!.findNavController().navigate(R.id.action_createPlaylistFragment_to_playlistFragment)
    }


}

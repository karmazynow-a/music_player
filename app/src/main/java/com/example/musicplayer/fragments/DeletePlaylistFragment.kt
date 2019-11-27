package com.example.musicplayer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.musicplayer.viewModels.MainViewModel

import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentDeletePlaylistBinding

class DeletePlaylistFragment : Fragment() {
    private lateinit var binding : FragmentDeletePlaylistBinding
    private lateinit var viewModel : MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<com.example.musicplayer.databinding.FragmentDeletePlaylistBinding>(
            inflater,
            R.layout.fragment_delete_playlist, container, false
        )

        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        (activity as AppCompatActivity).supportActionBar?.title = "Usuń playlistę"

        val playlistNames = viewModel.getPlaylistNames().drop(2)
        binding.chooseSongList.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, playlistNames)
        binding.chooseSongList.setOnItemClickListener { _, view, position, _ ->
            viewModel.deletePlaylist(position)
            view!!.findNavController().navigate(R.id.action_deletePlaylistFragment_to_playlistFragment)
        }

        return binding.root
    }

}

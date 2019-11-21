package com.example.musicplayer.playlist

import android.media.MediaMetadataRetriever
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.musicplayer.MainActivity
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlaylistBinding
import com.example.musicplayer.getNameFromPath
import timber.log.Timber


class PlaylistFragment : Fragment() {

    private lateinit var binding : FragmentPlaylistBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<com.example.musicplayer.databinding.FragmentPlaylistBinding>(
            inflater,
            R.layout.fragment_playlist, container, false
        )

        //TODO load playlist names from json
        val playlistNames = (activity as MainActivity).getPlaylistNames()
        binding.choosePlaylistSpinner.adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, playlistNames)

        binding.choosePlaylistSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Timber.d("No playlist selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0){
                    Timber.d("Selected helper text")
                }
                else {
                    Timber.d("Selected: " + playlistNames[position])
                    //TODO switch to new playlist
                    //createList(playlistName)
                }
            }
        }
        createList("Wszystkie utwory")

        return binding.root
    }

    private fun createList (playlist : String) {
        var currentSongs = (activity as MainActivity).getList(playlist)
        (activity as AppCompatActivity).supportActionBar?.title = playlist

        val listItems = arrayOfNulls<String>(currentSongs.size)
        val mediaMetadataRetriever = MediaMetadataRetriever()
        for ( i in 0 until currentSongs.size){
            //get song name
            mediaMetadataRetriever.setDataSource(currentSongs.elementAt(i))

            val name = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            listItems[i] = if (name.isNullOrEmpty()) getNameFromPath(currentSongs.elementAt(i)) else name
        }

        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, listItems)
        binding.songsList.adapter = adapter
        binding.songsList.setOnItemClickListener { _, view, position, _ ->
            view.findNavController().navigate(
                PlaylistFragmentDirections.actionPlaylistFragmentToPlayerFragment(
                    currentSongs.elementAt(position), playlist
                )
            )
            (activity as MainActivity).currentSongIndex = position
        }
    }
}

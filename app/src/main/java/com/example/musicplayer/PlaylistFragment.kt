package com.example.musicplayer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
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
import com.example.musicplayer.databinding.FragmentPlaylistBinding
import kotlinx.android.synthetic.main.fragment_playlist.*
import timber.log.Timber


class PlaylistFragment : Fragment() {

    private lateinit var binding : FragmentPlaylistBinding
    private var allSongs : MutableSet<String> = mutableSetOf()
    private var playlistNames : MutableList<String> = mutableListOf("Wybierz playlistę:", "Wszystkie utwory")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<com.example.musicplayer.databinding.FragmentPlaylistBinding>(
            inflater,
            R.layout.fragment_playlist, container, false
        )

        loadSongs()

        //TODO load playlist names from json
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
        createList("all")


        return binding.root
    }

    private fun loadSongs(){
        //TODO search through disc to find songs
        //add them to list

        allSongs.add("/sdcard/feng_suave_sink_into_the_floor.mp3")
    }

    private fun createList(playlist : String){
        var songs : MutableSet<String>

        if (playlist != "all"){
            //get playlist from JSON to songs
            songs = mutableSetOf()
            val index = playlistNames.indexOf(playlist)
            (activity as AppCompatActivity).supportActionBar?.title = playlistNames[index]
        }
        else {
            (activity as AppCompatActivity).supportActionBar?.title = playlistNames[1]
            songs = allSongs
        }

        val listItems = arrayOfNulls<String>(songs.size)
        val mediaMetadataRetriever = MediaMetadataRetriever()
        for ( i in 0 until songs.size){
            //get song name
            mediaMetadataRetriever.setDataSource(songs.elementAt(i))

            val name = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            listItems[i] = if (name.isNullOrEmpty()) getNameFromPath(songs.elementAt(i)) else name
        }

        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, listItems)
        binding.songsList.adapter = adapter
        binding.songsList.setOnItemClickListener { _, view, position, _ ->
            view.findNavController().navigate(PlaylistFragmentDirections.actionPlaylistFragmentToPlayerFragment(songs.elementAt(position)))

        }
    }
}

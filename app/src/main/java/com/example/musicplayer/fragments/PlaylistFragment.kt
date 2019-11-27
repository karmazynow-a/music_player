package com.example.musicplayer.fragments

import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.musicplayer.*
import com.example.musicplayer.databinding.FragmentPlaylistBinding
import com.example.musicplayer.services.PlayerService
import com.example.musicplayer.viewModels.MainViewModel
import com.example.musicplayer.viewModels.SongNameResolver
import timber.log.Timber


class PlaylistFragment : Fragment() {
    private lateinit var binding : FragmentPlaylistBinding
    private lateinit var service: PlayerService
    private var isBounded : Boolean
    private lateinit var viewModel : MainViewModel

    init{
        isBounded = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<com.example.musicplayer.databinding.FragmentPlaylistBinding>(
            inflater,
            R.layout.fragment_playlist, container, false
        )
        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        val playlistNames = viewModel.getPlaylistNames()
        binding.choosePlaylistSpinner.adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, playlistNames)

        binding.choosePlaylistSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Timber.d("No playlist selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0){}
                else {
                    Timber.d("Selected playlist: %s", playlistNames[position])
                    viewModel.setPlaylist(playlistNames[position])
                    createList()
                }
            }
        }

        createList()

        binding.miniPlayBtn.setOnClickListener { play() }
        binding.miniNextBtn.setOnClickListener { next() }
        binding.miniPrevBtn.setOnClickListener { prev() }
        binding.miniPlayer.setOnClickListener{
            Timber.d("Switching to player view")
            view!!.findNavController().navigate(R.id.action_playlistFragment_to_playerFragment)
        }

        if ( !viewModel.isPlaying.value!! ){
            binding.miniPlayBtn.setImageResource(R.drawable.ic_mini_play)
        } else {
            binding.miniPlayBtn.setImageResource(R.drawable.ic_mini_pause)
        }

        return binding.root
    }

    //*****************************NOTIFICATION SECTION
    override fun onStart() {
        super.onStart()

        val intent = Intent(context, PlayerService::class.java)
        (activity as MainActivity).bindService(intent, connection, Context.BIND_AUTO_CREATE)

        val filter = IntentFilter()
        filter.addAction(PlayerService.PREPARED_CHANGED)
        filter.addAction(PlayerService.TRACK_CHANGED)
        filter.addAction(PlayerService.PLAYING_CHANGED)
        (activity as MainActivity).registerReceiver(statusChange, filter)
    }

    override fun onStop() {
        (activity as MainActivity).unregisterReceiver(statusChange)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBounded) {
            (activity as MainActivity).unbindService(connection)
            isBounded = false
        }
    }

    private var statusChange = object : BroadcastReceiver(){
        override fun onReceive(contxt: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                PlayerService.PREPARED_CHANGED -> {
                    enableBtns(intent.getBooleanExtra("isPrepared", false))
                }

                PlayerService.PLAYING_CHANGED -> {
                    if (intent.getBooleanExtra("isPlaying", false)){
                        viewModel.setIsPlaying(true)
                        binding.miniPlayBtn.setImageResource(R.drawable.ic_mini_pause)
                    } else {
                        viewModel.setIsPlaying(false)
                        binding.miniPlayBtn.setImageResource(R.drawable.ic_mini_play)
                    }
                }

                PlayerService.TRACK_CHANGED -> {}
            }
        }
    }

    private var connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = (p1 as PlayerService.PlayerBinder)
            service = binder.getService()
            isBounded = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBounded = false
        }
    }

    //*************************MINI MUSIC PLAYER SECTION
    private fun play() {
        if (!viewModel.isPlaying.value!!) start()
        else pause()
    }

    private fun start(){
        Timber.d("Start")

        //if it's the first click after opening
        if ( !service.isReady() ) { //MEDIA_PLAYER_IDLE
            service.open(viewModel.currentSong.value!!, viewModel.currentPlaylist.value!!)
        }
        else {
            service.start()
        }
    }

    private fun pause(){
        Timber.d("Pause")
        if ( service.isReady() ){
            service.stop()
        }
    }

    private fun next(){
        if ( !service.isReady() ) {
            service.open(viewModel.currentSong.value!! + 1, viewModel.currentPlaylist.value!!)
        }
        else{
            service.next()
        }
    }

    private fun prev(){
        if ( !service.isReady() ) {
            service.open(viewModel.currentSong.value!! - 1, viewModel.currentPlaylist.value!!)
        }
        else {
            service.prev()
        }
    }

    //*************************PLAYLIST SECTION
    private fun createList () {
        val currentSongs = viewModel.currentPlaylist.value!!
        (activity as AppCompatActivity).supportActionBar?.title = viewModel.currentPlaylistName.value!!

        if(currentSongs.isEmpty()){
            Toast.makeText(context, "Nie znaleziono utworów!", Toast.LENGTH_LONG).show()
        }

        val listItems = arrayOfNulls<String>(currentSongs.size)
        for ( i in 0 until currentSongs.size){
            listItems[i] = SongNameResolver.getSongName(currentSongs.elementAt(i))
        }

        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, listItems)
        binding.songsList.adapter = adapter
        binding.songsList.setOnItemClickListener { _, view, position, _ ->
            view!!.findNavController().navigate(R.id.action_playlistFragment_to_playerFragment)
            Timber.d("Setting current track to %s", currentSongs[position])
            viewModel.setCurrentSong (position)
        }
    }

    private fun enableBtns ( enable : Boolean ){
        binding.miniPlayBtn.isEnabled = enable
        binding.miniNextBtn.isEnabled = enable
        binding.miniPrevBtn.isEnabled = enable
    }
}

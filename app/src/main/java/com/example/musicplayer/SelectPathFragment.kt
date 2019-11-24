package com.example.musicplayer

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.databinding.DataBindingUtil.inflate
import androidx.navigation.findNavController
import com.example.musicplayer.databinding.FragmentSelectPathBinding
import timber.log.Timber


class SelectPathFragment : Fragment() {
    private lateinit var viewModel : MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding = inflate<FragmentSelectPathBinding>(
            inflater,
            R.layout.fragment_select_path, container, false
        )

        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        (activity as AppCompatActivity).supportActionBar?.title = "Wybierz ścieżkę"

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, 9999)

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 9999){

            val path = FileUtil.getFullPathFromTreeUri(data!!.data,context)
            Timber.d("new path is" + path )
            viewModel.setPath(path!!)
        }
        view!!.findNavController().navigate(R.id.action_selectPathFragment_to_playlistFragment)
    }
}

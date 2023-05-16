package com.riobener.sonicsoul.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.databinding.MusicDetailsBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MusicDetailsFragment : Fragment() {

    private var _binding: MusicDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = MusicDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backArrow.setOnClickListener {
            findNavController().navigate(R.id.action_MusicDetails_to_MusicList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
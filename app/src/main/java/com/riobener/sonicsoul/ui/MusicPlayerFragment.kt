package com.riobener.sonicsoul.ui


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.riobener.sonicsoul.data.music.TrackInfo
import com.riobener.sonicsoul.databinding.MusicPlayerBinding
import com.riobener.sonicsoul.player.PlayerViewModel
import com.riobener.sonicsoul.utils.launchAndCollectIn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.music_items.view.*
import kotlinx.android.synthetic.main.music_player.*

class MusicPlayerFragment : Fragment() {

    private var _binding: MusicPlayerBinding? = null
    private val playerViewModel by activityViewModels<PlayerViewModel>()

    private var progressBarFlag = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MusicPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
        playerViewModel.currentTrack.launchAndCollectIn(this) {
            it?.let { currentTrack ->
                setupTrackInfo(currentTrack)
            }
        }
        playerViewModel.trackDuration.launchAndCollectIn(this) {
            val seconds = ((it / 1000)).toInt()
            binding.songProgress.max = seconds
            binding.songDurationTotal.text = seconds.toString()
        }
        playerViewModel.currentPosition.launchAndCollectIn(this) {
            val seconds = ((it / 1000)).toInt()
            binding.songProgress.progress = seconds
            binding.songDurationPlayed.text = seconds.toString()
        }
        binding.songBack.setOnClickListener {
            playerViewModel.playPreviousTrack()
        }
        binding.songNext.setOnClickListener {
            playerViewModel.playNextTrack()
        }
        binding.songProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(progressBarFlag)
                playerViewModel.changeTrackProgress((progress * 1000).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                progressBarFlag = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                progressBarFlag = false
            }
        })
    }

    private fun setupTrackInfo(currentTrack: TrackInfo){
        currentTrack.bigImageSource?.let{ image ->
            Glide.with(this).load(image).into(song_card_image)
        }
        binding.songName.text = currentTrack.title
        binding.songAuthor.text = currentTrack.artistName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
    }
}

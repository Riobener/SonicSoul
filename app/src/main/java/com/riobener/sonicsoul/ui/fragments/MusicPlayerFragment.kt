package com.riobener.sonicsoul.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.data.music.TrackInfo
import com.riobener.sonicsoul.databinding.MusicPlayerBinding
import com.riobener.sonicsoul.ui.viewmodels.PlayerViewModel
import com.riobener.sonicsoul.ui.viewmodels.MusicViewModel
import com.riobener.sonicsoul.utils.launchAndCollectIn

class MusicPlayerFragment : Fragment() {

    private var _binding: MusicPlayerBinding? = null
    private val playerViewModel by activityViewModels<PlayerViewModel>()
    private val musicViewModel by activityViewModels<MusicViewModel>()
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
        playerViewModel.isPlaying.launchAndCollectIn(this) { isPlaying ->
            if (isPlaying) {
                binding.songPlayPause.setImageResource(R.drawable.pause_button)
            } else {
                binding.songPlayPause.setImageResource(R.drawable.play_button)
            }
        }
        binding.songPlayPause.setOnClickListener {
            playerViewModel.currentTrack.value?.let { currentTrack -> playerViewModel.chooseAndPlayTrack(currentTrack) }
        }
        binding.songProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progressBarFlag)
                    playerViewModel.changeTrackProgress((progress * 1000).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                progressBarFlag = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                progressBarFlag = false
            }
        })
        binding.backArrow.setOnClickListener {
            activity?.onBackPressed();
        }
    }

    private fun setupTrackInfo(currentTrack: TrackInfo) {
        currentTrack.bigImageSource?.let { image ->
            Glide.with(this).load(image).into(binding.songCardImage)
        }?: binding.songCardImage.setImageResource(R.drawable.icon)
        binding.songName.text = currentTrack.title
        binding.songAuthor.text = currentTrack.artist
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
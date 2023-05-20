package com.riobener.sonicsoul.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.data.music.TrackInfo
import com.riobener.sonicsoul.databinding.MusicListFragmentBinding
import com.riobener.sonicsoul.player.PlayerViewModel
import com.riobener.sonicsoul.ui.adapters.MusicAdapter
import com.riobener.sonicsoul.ui.viewmodels.MusicViewModel
import com.riobener.sonicsoul.ui.viewmodels.SpotifyViewModel
import com.riobener.sonicsoul.utils.launchAndCollectIn
import com.riobener.sonicsoul.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.music_list_fragment.*
import kotlinx.android.synthetic.main.music_player.*
import kotlinx.android.synthetic.main.music_player_mini.*
import net.openid.appauth.*

@AndroidEntryPoint
class MusicListFragment : Fragment() {

    private var _binding: MusicListFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel by viewModels<SpotifyViewModel>()
    private val playerViewModel by activityViewModels<PlayerViewModel>()
    private val musicViewModel by activityViewModels<MusicViewModel>()

    private lateinit var musicAdapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MusicListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            (it as AppCompatActivity).supportActionBar?.show()
        }
        initAdapter(binding.root)
        setupMiniPlayer()
        viewModel.toastFlow.launchAndCollectIn(viewLifecycleOwner) {
            toast(it)
        }
        if (musicViewModel.alreadyLoaded) {
            fillMusicContent(music = playerViewModel.getPlaylist())
        } else {
            processTokenExisting()
        }
    }

    fun processAuth() {
        binding.tokenEmptyWindow.isVisible = true
        binding.loginButton.isEnabled = true
        binding.loginButton.setOnClickListener {
            viewModel.openAuthPage()
        }
        viewModel.authSuccessFlow.launchAndCollectIn(viewLifecycleOwner) {
            processMusicLoad()
        }
        viewModel.openAuthPageFlow.launchAndCollectIn(viewLifecycleOwner) {
            openAuthPage(it)
        }
    }

    fun processMusicLoad() {
        binding.tokenEmptyWindow.isVisible = false
        binding.loginButton.isEnabled = false
        musicViewModel.loadOnlineMusic()
        musicViewModel.musicInfoFlow.launchAndCollectIn(viewLifecycleOwner) { music ->
            fillMusicContent(music)
        }
    }

    fun fillMusicContent(music: List<TrackInfo>) {
        val musicList = music.filter { it.trackSource != null }
        musicAdapter.differ.submitList(musicList)
        playerViewModel.setPlaylist(musicList)
        musicAdapter.notifyDataSetChanged()
    }

    fun setupMiniPlayer() {
        playerViewModel.currentTrack.launchAndCollectIn(viewLifecycleOwner) { currentTrack ->
            if (currentTrack != null) {
                binding.miniPlayer.miniPlayerLayout.visibility = View.VISIBLE
                currentTrack.imageSource?.let { image ->
                    Glide.with(this@MusicListFragment).load(image).into(binding.miniPlayer.miniPlayerMusicImg)
                }
                binding.miniPlayer.miniMusicTitle.text = currentTrack.title
                binding.miniPlayer.miniMusicAuthor.text = currentTrack.artist
            } else {
                binding.miniPlayer.miniPlayerLayout.visibility = View.GONE
            }
        }
        binding.miniPlayer.miniSongBack.setOnClickListener {
            playerViewModel.playPreviousTrack()
        }
        binding.miniPlayer.miniSongNext.setOnClickListener {
            playerViewModel.playNextTrack()
        }
        binding.miniPlayer.miniSongPlayPause.setOnClickListener {
            playerViewModel.currentTrack.value?.let { currentTrack -> playerViewModel.chooseAndPlayTrack(currentTrack) }
        }
        playerViewModel.isPlaying.launchAndCollectIn(viewLifecycleOwner) { isPlaying ->
            if (isPlaying) {
                binding.miniPlayer.miniSongPlayPause.setImageResource(R.drawable.pause_button)
            } else {
                binding.miniPlayer.miniSongPlayPause.setImageResource(R.drawable.play_button)
            }
            musicAdapter.notifyDataSetChanged()
        }
        binding.miniPlayer.miniPlayerLayout.setOnClickListener {
            val action = MusicListFragmentDirections.actionMusicListToMusicPlayer()
            Navigation.findNavController(it).navigate(action)
        }
    }

    private fun processTokenExisting() {
        musicViewModel.loadingFlow.launchAndCollectIn(viewLifecycleOwner) { isLoading ->
            binding.musicListProgressBar.isVisible = isLoading
            binding.musicList.isVisible = !isLoading
        }
        viewModel.getServiceCredentials()
        viewModel.serviceCredentialsFlow.launchAndCollectIn(viewLifecycleOwner) { serviceCredentials ->
            serviceCredentials?.let {
                processMusicLoad()
            } ?: processAuth()
        }
    }

    private val getAuthResponse = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val dataIntent = it.data ?: return@registerForActivityResult
        handleAuthResponseIntent(dataIntent)
    }

    private fun handleAuthResponseIntent(intent: Intent) {
        // пытаемся получить ошибку из ответа. null - если все ок
        val exception = AuthorizationException.fromIntent(intent)
        // пытаемся получить запрос для обмена кода на токен, null - если произошла ошибка
        val tokenExchangeRequest = AuthorizationResponse.fromIntent(intent)
            ?.createTokenExchangeRequest()
        when {
            // авторизация завершались ошибкой
            exception != null -> viewModel.onAuthCodeFailed(exception)
            // авторизация прошла успешно, меняем код на токен
            tokenExchangeRequest != null ->
                viewModel.onAuthCodeReceived(tokenExchangeRequest)
        }
    }

    private fun initAdapter(view: View) {
        musicAdapter = MusicAdapter()
        musicAdapter.onItemClick = {
            playerViewModel.chooseAndPlayTrack(it)
        }
        binding.musicList.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        playerViewModel.currentTrack.launchAndCollectIn(viewLifecycleOwner) {
            musicAdapter.notifyDataSetChanged()
        }
    }

    private fun openAuthPage(intent: Intent) {
        getAuthResponse.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
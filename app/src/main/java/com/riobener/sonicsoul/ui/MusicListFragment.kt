package com.riobener.sonicsoul.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.riobener.sonicsoul.data.music.TrackInfo
import com.riobener.sonicsoul.databinding.MusicListFragmentBinding
import com.riobener.sonicsoul.player.PlayerViewModel
import com.riobener.sonicsoul.ui.adapters.MusicAdapter
import com.riobener.sonicsoul.ui.viewmodels.SpotifyViewModel
import com.riobener.sonicsoul.utils.launchAndCollectIn
import com.riobener.sonicsoul.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.music_list_fragment.*
import net.openid.appauth.*

@AndroidEntryPoint
class MusicListFragment : Fragment() {

    private var _binding: MusicListFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel by viewModels<SpotifyViewModel>()
    private val playerViewModel by activityViewModels<PlayerViewModel>()

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
        if (viewModel.alreadyLoaded) {
            fillMusicContent(music = playerViewModel.getPlaylist())
        } else {
            processTokenExisting()
        }
        viewModel.toastFlow.launchAndCollectIn(viewLifecycleOwner) {
            toast(it)
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
        viewModel.loadMusic()
        viewModel.musicInfoFlow.launchAndCollectIn(viewLifecycleOwner) { music ->
            fillMusicContent(music)
        }
    }

    fun fillMusicContent(music: List<TrackInfo>) {
        music.forEach { Log.d("TRACK123", it.isPlaying.toString()) }
        val musicList = music.filter { it.trackSource != null }
        musicAdapter.differ.submitList(musicList)
        playerViewModel.setPlaylist(musicList)
        musicAdapter.notifyDataSetChanged()
    }

    private fun processTokenExisting() {
        viewModel.loadingFlow.launchAndCollectIn(viewLifecycleOwner) { isLoading ->
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
            val action = MusicListFragmentDirections.actionMusicListToMusicPlayer()
            Navigation.findNavController(view).navigate(action)
        }
        binding.musicList.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        playerViewModel.currentTrack.launchAndCollectIn(viewLifecycleOwner) { currentTrack ->
            currentTrack?.let {
                val currentIndex = musicAdapter.differ.currentList.indexOf(currentTrack)
                musicAdapter.differ.currentList[currentIndex].isPlaying = currentTrack.isPlaying
                musicAdapter.differ.currentList.forEach { Log.d("LIST1", it.isPlaying.toString()) }
            }
            playerViewModel.previousTrack.value?.let { previous ->
                val lastIndex = musicAdapter.differ.currentList.indexOf(previous)
                musicAdapter.differ.currentList[lastIndex].isPlaying = previous.isPlaying
                musicAdapter.differ.currentList.forEach { Log.d("LIST2", it.isPlaying.toString()) }
            }
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
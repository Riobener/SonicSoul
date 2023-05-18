package com.riobener.sonicsoul.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.databinding.ActivityMainBinding
import com.riobener.sonicsoul.databinding.MusicListFragmentBinding
import com.riobener.sonicsoul.player.PlayerViewModel
import com.riobener.sonicsoul.ui.adapters.MusicAdapter
import com.riobener.sonicsoul.ui.viewmodels.SpotifyViewModel
import com.riobener.sonicsoul.utils.launchAndCollectIn
import com.riobener.sonicsoul.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.music_list_fragment.*
import kotlinx.coroutines.launch
import net.openid.appauth.*

@AndroidEntryPoint
class MusicListFragment : Fragment() {

    private var _binding: MusicListFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel by viewModels<SpotifyViewModel>()
    private val playerViewModel by viewModels<PlayerViewModel>()

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
        initAdapter(binding.root)
        processTokenExisting()
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
            val musicList = music.filter { it.trackSource != null }
            musicAdapter.differ.submitList(musicList)
            playerViewModel.setPlaylist(musicList)
        }
    }

    private fun processTokenExisting() {
        viewModel.loadingFlow.launchAndCollectIn(viewLifecycleOwner) { isLoading ->
            binding.musicListProgressBar.isVisible = isLoading
            binding.musicList.isVisible = !isLoading
        }
        viewModel.getServiceCredentials()
        viewModel.serviceCredentialsFlow.launchAndCollectIn(viewLifecycleOwner) { serviceCredentials ->
            serviceCredentials?.let { credentials ->
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
            playerViewModel.chooseTrack(it)
        }
        binding.musicList.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(activity)
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
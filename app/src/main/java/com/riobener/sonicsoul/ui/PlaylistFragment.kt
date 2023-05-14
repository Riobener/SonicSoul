package com.riobener.sonicsoul.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.databinding.PlaylistFragmentBinding
import com.riobener.sonicsoul.ui.viewmodels.SpotifyViewModel
import com.riobener.sonicsoul.utils.launchAndCollectIn
import com.riobener.sonicsoul.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import net.openid.appauth.*

@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    private var _binding: PlaylistFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel by viewModels<SpotifyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PlaylistFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.authButton.setOnClickListener {
            viewModel.openAuthPage()
        }
        binding.musicButton.setOnClickListener {
            viewModel.getSpotifyMusicList()
        }
        viewModel.openAuthPageFlow.launchAndCollectIn(viewLifecycleOwner) {
            openAuthPage(it)
        }
        viewModel.toastFlow.launchAndCollectIn(viewLifecycleOwner) {
            toast(it)
        }
        viewModel.authSuccessFlow.launchAndCollectIn(viewLifecycleOwner) {
            viewModel.getSpotifyMusicList()
            /*findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)*/
        }
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
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

    private fun openAuthPage(intent: Intent) {
        getAuthResponse.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
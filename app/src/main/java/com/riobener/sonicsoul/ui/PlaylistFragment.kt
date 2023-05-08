package com.riobener.sonicsoul.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.riobener.sonicsoul.BuildConfig
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.data.entity.ServiceCredentials
import com.riobener.sonicsoul.data.entity.ServiceName
import com.riobener.sonicsoul.databinding.PlaylistFragmentBinding
import com.riobener.sonicsoul.ui.viewmodels.ServiceCredentialsViewModel
import com.riobener.sonicsoul.utils.SpotifyConstants
import dagger.hilt.android.AndroidEntryPoint
import net.openid.appauth.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    private var _binding: PlaylistFragmentBinding? = null

    private lateinit var service: AuthorizationService

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel by viewModels<ServiceCredentialsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PlaylistFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            val ex = AuthorizationException.fromIntent(it.data!!)
            val result = AuthorizationResponse.fromIntent(it.data!!)

            if (ex != null) {
                Log.e("SPOTIFY AUTH", "launcher: $ex")
            } else {
                val secret = ClientSecretBasic(BuildConfig.SPOTIFY_CLIENT_SECRET)
                val tokenRequest = result?.createTokenExchangeRequest()
                service.performTokenRequest(tokenRequest!!, secret) { res, exception ->
                    if (exception == null) {
                        res?.let { result ->
                            result.accessToken?.let { accessToken ->
                                viewModel.saveServiceCredentials(
                                    ServiceCredentials.create(
                                        serviceName = ServiceName.SPOTIFY,
                                        accessToken = accessToken,
                                        refreshToken = result.refreshToken
                                    )
                                )
                            } ?: Log.e("SPOTIFY AUTH ERROR", "accessToken is null")
                        } ?: Log.e("SPOTIFY AUTH ERROR", "res is null")
                    }
                }
            }
        }
    }

    fun authSpotify() {
        val redirectUri = Uri.parse(SpotifyConstants.REDIRECT_URI)

        val authorizeUri = Uri.parse(SpotifyConstants.AUTHORIZE_URL)
        val tokenUri = Uri.parse(SpotifyConstants.TOKEN_URL)

        val config = AuthorizationServiceConfiguration(authorizeUri, tokenUri)

        val request =
            AuthorizationRequest.Builder(config, BuildConfig.SPOTIFY_CLIENT_ID, ResponseTypeValues.CODE, redirectUri)
                .build()
        val intent = service.getAuthorizationRequestIntent(request)
        launcher.launch(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        service = activity?.let { AuthorizationService(it.applicationContext) }!!
        binding.authButton.setOnClickListener {
            authSpotify()
        }
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        service.dispose()
    }

}
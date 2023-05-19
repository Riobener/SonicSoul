package com.riobener.sonicsoul.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.*
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.data.auth.ServiceCredentials
import com.riobener.sonicsoul.data.auth.ServiceCredentialsRepository
import com.riobener.sonicsoul.data.auth.ServiceName
import com.riobener.sonicsoul.data.auth.spotify.SpotifyAuthRepository
import com.riobener.sonicsoul.data.music.TrackInfo
import com.riobener.sonicsoul.data.music.spotify.SpotifyMusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenRequest
import javax.inject.Inject

@HiltViewModel
class SpotifyViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
    private val state: SavedStateHandle,
    private val authRepository: SpotifyAuthRepository,
    private val serviceCredentialsRepository: ServiceCredentialsRepository,
    private val spotifyMusicRepository: SpotifyMusicRepository,
) : ViewModel() {
    //Auth
    private val authService: AuthorizationService = AuthorizationService(applicationContext)
    private val openAuthPageEventChannel = Channel<Intent>(Channel.BUFFERED)
    private val toastEventChannel = Channel<Int>(Channel.BUFFERED)
    private val authSuccessEventChannel = Channel<Unit>(Channel.BUFFERED)
    val openAuthPageFlow: Flow<Intent>
        get() = openAuthPageEventChannel.receiveAsFlow()
    val authSuccessFlow: Flow<Unit>
        get() = authSuccessEventChannel.receiveAsFlow()
    val toastFlow: Flow<Int>
        get() = toastEventChannel.receiveAsFlow()

    private val serviceCredentialsMutableStateFlow = MutableStateFlow<ServiceCredentials?>(null)
    val serviceCredentialsFlow: Flow<ServiceCredentials?>
        get() = serviceCredentialsMutableStateFlow.asStateFlow()


    //Loading
    private val loadingMutableStateFlow = MutableStateFlow(false)
    val loadingFlow: Flow<Boolean>
        get() = loadingMutableStateFlow.asStateFlow()

    //Music
    private val musicInfoMutableStateFlow = MutableStateFlow<List<TrackInfo>>(emptyList())
    val musicInfoFlow: Flow<List<TrackInfo>>
        get() = musicInfoMutableStateFlow.asStateFlow()

    fun getServiceCredentials(){
        viewModelScope.launch {
            serviceCredentialsMutableStateFlow.value = serviceCredentialsRepository.findByServiceName(ServiceName.SPOTIFY)
        }
    }

    fun loadMusic() {
        viewModelScope.launch {
            loadingMutableStateFlow.value = true
            runCatching {
                spotifyMusicRepository.getTracks()
            }.onSuccess {
                musicInfoMutableStateFlow.value = it
                loadingMutableStateFlow.value = false
            }.onFailure {
                loadingMutableStateFlow.value = false
                musicInfoMutableStateFlow.value = emptyList()
                toastEventChannel.trySendBlocking(R.string.music_load_exception)
            }
        }
    }

    fun openAuthPage() {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        val authRequest = authRepository.getAuthRequest()
        val authPageIntent = authService.getAuthorizationRequestIntent(
            authRequest,
            customTabsIntent
        )
        Log.d("OAuth", "1. Open auth page: ${authRequest.toUri()}")
        openAuthPageEventChannel.trySendBlocking(authPageIntent)
    }

    fun onAuthCodeReceived(tokenRequest: TokenRequest) {
        Log.d("OAuth", "2. Received code = ${tokenRequest.authorizationCode}")
        viewModelScope.launch {
            loadingMutableStateFlow.value = true
            runCatching {
                Log.d(
                    "OAuth",
                    "3. Change code to token. Url = ${tokenRequest.configuration.tokenEndpoint}, verifier = ${tokenRequest.codeVerifier}"
                )
                authRepository.performTokenRequest(
                    authService = authService,
                    tokenRequest = tokenRequest
                )
            }.onSuccess {
                loadingMutableStateFlow.value = false
                authSuccessEventChannel.send(Unit)
            }.onFailure {
                loadingMutableStateFlow.value = false
                toastEventChannel.send(R.string.login_exception)
            }
        }
    }

    fun onAuthCodeFailed(exception: AuthorizationException) {
        toastEventChannel.trySendBlocking(R.string.app_name)
    }

    override fun onCleared() {
        super.onCleared()
        authService.dispose()
    }
}
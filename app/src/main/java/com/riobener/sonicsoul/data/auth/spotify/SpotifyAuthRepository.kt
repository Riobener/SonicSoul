package com.riobener.sonicsoul.data.auth.spotify

import android.util.Log
import com.riobener.sonicsoul.data.auth.AppAuth
import com.riobener.sonicsoul.data.auth.ServiceCredentials
import com.riobener.sonicsoul.data.auth.ServiceCredentialsRepository
import com.riobener.sonicsoul.data.auth.ServiceName
import com.riobener.sonicsoul.utils.SpotifyConfig
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenRequest
import javax.inject.Inject

class SpotifyAuthRepository @Inject constructor(
    private val serviceCredentialsRepository: ServiceCredentialsRepository,
) {
    suspend fun performTokenRequest(
        authService: AuthorizationService,
        tokenRequest: TokenRequest,
    ) {
        val tokens = AppAuth(SpotifyConfig).performTokenRequestSuspend(authService, tokenRequest)
        //обмен кода на токен произошел успешно, сохраняем токены и завершаем авторизацию
        serviceCredentialsRepository.save(
            ServiceCredentials.create(
                serviceName = ServiceName.SPOTIFY,
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken
            )
        )
        Log.d("OAuth", "6. Tokens accepted:\n access=${tokens.accessToken}\nrefresh=${tokens.refreshToken}")
    }

    fun getAuthRequest(): AuthorizationRequest {
        return AppAuth(SpotifyConfig).getAuthRequest()
    }
}

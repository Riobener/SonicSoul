package com.riobener.sonicsoul.data.auth

import android.net.Uri
import com.riobener.sonicsoul.utils.ServiceConfig
import net.openid.appauth.*
import kotlin.coroutines.suspendCoroutine

data class AppAuth(
    private val serviceConfig: ServiceConfig,
) {
    private val serviceConfiguration = AuthorizationServiceConfiguration(
        Uri.parse(serviceConfig.AUTHORIZE_URL),
        Uri.parse(serviceConfig.TOKEN_URL),
/*        null, // registration endpoint
        Uri.parse(serviceConfig.END_SESSION_URI)//TODO logout*/
    )

    fun getAuthRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfiguration,
            serviceConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(serviceConfig.REDIRECT_URI),
        ).setScopes(serviceConfig.SCOPES).build()
    }
//TODO logout
/*    fun getEndSessionRequest(): EndSessionRequest {
        return EndSessionRequest.Builder(serviceConfiguration)
            .setPostLogoutRedirectUri(AuthConfig.LOGOUT_CALLBACK_URL.toUri())
            .build()
    }*/

    fun getRefreshTokenRequest(refreshToken: String): TokenRequest {
        return TokenRequest.Builder(
            serviceConfiguration,
            serviceConfig.CLIENT_ID
        ).setGrantType(GrantTypeValues.REFRESH_TOKEN)
            .setScopes(serviceConfig.SCOPES)
            .setRefreshToken(refreshToken)
            .build()
    }

    suspend fun performTokenRequestSuspend(
        authService: AuthorizationService,
        tokenRequest: TokenRequest,
    ): TokenInfo {
        return suspendCoroutine { continuation ->
            authService.performTokenRequest(tokenRequest, getClientAuthentication()) { response, ex ->
                when {
                    response != null -> {
                        //получение токена произошло успешно
                        val tokens = TokenInfo(
                            accessToken = response.accessToken.orEmpty(),
                            refreshToken = response.refreshToken.orEmpty(),
                        )
                        continuation.resumeWith(Result.success(tokens))
                    }
                    //получение токенов произошло неуспешно, показываем ошибку
                    ex != null -> {
                        continuation.resumeWith(Result.failure(ex))
                    }
                    else -> error("unreachable")
                }
            }
        }
    }

    private fun getClientAuthentication(): ClientAuthentication {
        return ClientSecretPost(serviceConfig.CLIENT_SECRET)
    }
}
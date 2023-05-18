package com.riobener.sonicsoul.data.network

import android.content.Context
import android.util.Log
import com.riobener.sonicsoul.data.auth.AppAuth
import com.riobener.sonicsoul.data.auth.ServiceCredentials
import com.riobener.sonicsoul.data.auth.ServiceCredentialsRepository
import com.riobener.sonicsoul.data.auth.ServiceName
import com.riobener.sonicsoul.utils.SpotifyConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationService
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthorizationFailedInterceptor @Inject constructor(
    private val repository: ServiceCredentialsRepository,
    @ApplicationContext applicationContext: Context,
) : Interceptor {
    private val authorizationService: AuthorizationService = AuthorizationService(applicationContext)

    private val credentials = runBlocking { repository.findByServiceName(ServiceName.SPOTIFY) }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequestTimestamp = System.currentTimeMillis()
        val originalResponse = chain.proceed(chain.request())
        return originalResponse
            .takeIf { it.code != 401 }
            ?: handleUnauthorizedResponse(chain, originalResponse, originalRequestTimestamp)
    }

    private fun handleUnauthorizedResponse(
        chain: Interceptor.Chain,
        originalResponse: Response,
        requestTimestamp: Long
    ): Response {
        val latch = getLatch()
        return when {
            latch != null && latch.count > 0 -> handleTokenIsUpdating(chain, latch, requestTimestamp,originalResponse)
                ?: originalResponse
            tokenUpdateTime > requestTimestamp -> updateTokenAndProceedChain(chain,originalResponse)
            else -> handleTokenNeedRefresh(chain,originalResponse) ?: originalResponse
        }
    }

    private fun handleTokenIsUpdating(
        chain: Interceptor.Chain,
        latch: CountDownLatch,
        requestTimestamp: Long,
        oldResponse: Response,
    ): Response? {
        return if (latch.await(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            && tokenUpdateTime > requestTimestamp
        ) {
            updateTokenAndProceedChain(chain,oldResponse)
        } else {
            null
        }
    }

    private fun handleTokenNeedRefresh(
        chain: Interceptor.Chain,
        oldResponse: Response,
    ): Response? {
        return if (refreshToken()) {
            updateTokenAndProceedChain(chain,oldResponse)
        } else {
            null
        }
    }

    private fun updateTokenAndProceedChain(
        chain: Interceptor.Chain,
        oldResponse: Response,
    ): Response {
        oldResponse.close()
        val newRequest = updateOriginalCallWithNewToken(chain.request())
        return chain.proceed(newRequest)
    }

    private fun refreshToken(): Boolean {
        initLatch()
        val tokenRefreshed = runBlocking {
            runCatching {
                val refreshRequest = AppAuth(SpotifyConfig).getRefreshTokenRequest(credentials?.refreshToken.orEmpty())
                AppAuth(SpotifyConfig).performTokenRequestSuspend(authorizationService, refreshRequest)
            }.getOrNull()
                ?.let { tokens ->
                    repository.save(
                        ServiceCredentials.create(
                            serviceName = ServiceName.SPOTIFY,
                            accessToken = tokens.accessToken,
                            refreshToken = tokens.refreshToken
                        )
                    )
                    credentials?.accessToken = tokens.accessToken
                    true
                } ?: false
        }

        if (tokenRefreshed) {
            tokenUpdateTime = System.currentTimeMillis()
        } else {
            // не удалось обновить токен, произвести логаут
            Log.d("OAuth", "10. EXCEPTION")
        }
        getLatch()?.countDown()
        return tokenRefreshed
    }

    private fun updateOriginalCallWithNewToken(request: Request): Request {
        return credentials?.let{ creds ->
            request.addTokenHeader(creds.accessToken)
        } ?: request
    }


    fun Request.addTokenHeader(token: String?): Request {
        val authHeaderName = "Authorization"
        return newBuilder()
            .apply {
                if (token != null) {
                    header(authHeaderName, token.withBearer())
                }
            }
            .build()
    }

    private fun String.withBearer() = "Bearer $this"

    companion object {

        private const val REQUEST_TIMEOUT = 30L

        @Volatile
        private var tokenUpdateTime: Long = 0L

        private var countDownLatch: CountDownLatch? = null

        @Synchronized
        fun initLatch() {
            countDownLatch = CountDownLatch(1)
        }

        @Synchronized
        fun getLatch() = countDownLatch
    }
}
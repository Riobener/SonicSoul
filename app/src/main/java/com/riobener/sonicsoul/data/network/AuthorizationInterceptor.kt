package com.riobener.sonicsoul.data.network

import com.riobener.sonicsoul.data.auth.ServiceCredentialsRepository
import com.riobener.sonicsoul.data.auth.ServiceName
import com.riobener.sonicsoul.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class AuthorizationInterceptor(
    private val repository: ServiceCredentialsRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { repository.findByServiceName(ServiceName.SPOTIFY)?.accessToken }
        return chain.request()
            .addTokenHeader(token)
            .let { chain.proceed(it) }
    }

    private fun Request.addTokenHeader(token: String?): Request {
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
}
package com.riobener.sonicsoul.di

import android.content.Context
import com.riobener.sonicsoul.data.auth.ServiceCredentials
import com.riobener.sonicsoul.data.auth.ServiceCredentialsDao
import com.riobener.sonicsoul.data.auth.ServiceCredentialsRepository
import com.riobener.sonicsoul.data.auth.ServiceName
import com.riobener.sonicsoul.data.auth.spotify.SpotifyApi
import com.riobener.sonicsoul.data.auth.spotify.SpotifyAuthRepository
import com.riobener.sonicsoul.data.network.AuthorizationFailedInterceptor
import com.riobener.sonicsoul.data.network.AuthorizationInterceptor
import com.riobener.sonicsoul.utils.ServiceConfig
import com.riobener.sonicsoul.utils.SpotifyConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    fun logging() = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    @Provides
    fun provideAuthorizationFailedInterceptor(
        serviceCredentialsRepository: ServiceCredentialsRepository,
        @ApplicationContext applicationContext: Context
    ): AuthorizationFailedInterceptor {
        return AuthorizationFailedInterceptor(serviceCredentialsRepository, applicationContext)
    }

    @Provides
    fun provideAuthorizationInterceptor(
        serviceCredentialsRepository: ServiceCredentialsRepository,
    ): AuthorizationInterceptor {
        return AuthorizationInterceptor(serviceCredentialsRepository)
    }

    @Provides
    fun okHttpClient(
        authorizationInterceptor: AuthorizationInterceptor,
        authorizationFailedInterceptor: AuthorizationFailedInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging())
            .addInterceptor(authorizationInterceptor)
            .addInterceptor(authorizationFailedInterceptor)
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(SpotifyConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()


    @Provides
    @Singleton
    fun spotifyApi(retrofit: Retrofit): SpotifyApi {
        return retrofit.create(SpotifyApi::class.java)
    }
}
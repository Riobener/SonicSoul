package com.riobener.sonicsoul.di


import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
object ExoPlayerModule {

    @Provides
    fun provideDataSourceFactory(@ApplicationContext context: Context): DataSource.Factory {
        return DefaultDataSourceFactory(context, "user-agent")
    }

    @Provides
    fun provideDatabaseProvider(@ApplicationContext context: Context): DatabaseProvider {
        return ExoDatabaseProvider(context)
    }

    @Provides
    fun provideMediaSourceFactory(@ApplicationContext context: Context, dataSourceFactory: DataSource.Factory, provider: DatabaseProvider): ProgressiveMediaSource.Factory {
        val maxCacheSize: Long = 100 * 1024 * 1024
        val cache = SimpleCache(context.cacheDir, LeastRecentlyUsedCacheEvictor(maxCacheSize),provider)
        return ProgressiveMediaSource.Factory(
            cache.let {
                CacheDataSourceFactory(
                    it, dataSourceFactory
                )
            }
        )

    }

    @Provides
    fun provideAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): SimpleExoPlayer = SimpleExoPlayer.Builder(context).build().apply {
        setAudioAttributes(audioAttributes, true)
        setHandleAudioBecomingNoisy(true)
    }
}
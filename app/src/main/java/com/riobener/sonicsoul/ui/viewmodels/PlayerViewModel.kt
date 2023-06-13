package com.riobener.sonicsoul.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.*
import com.riobener.sonicsoul.data.music.TrackInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
    private val state: SavedStateHandle,
    private val exoPlayer: SimpleExoPlayer,
    private val mediaSourceFactory: ProgressiveMediaSource.Factory,
    private val databaseProvider: DatabaseProvider,
) : ViewModel() {

    private val _currentTrack: MutableStateFlow<TrackInfo?> = MutableStateFlow(null)
    val currentTrack: StateFlow<TrackInfo?> = _currentTrack

    private val _currentPosition: MutableStateFlow<Long> = MutableStateFlow(0)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _trackDuration: MutableStateFlow<Long> = MutableStateFlow(0)
    val trackDuration: StateFlow<Long> = _trackDuration

    private val _isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isLooping: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping

    private val playlist: MutableStateFlow<MutableList<TrackInfo>> = MutableStateFlow(mutableListOf())
    val playlistFlow: StateFlow<MutableList<TrackInfo>>  = playlist

    private var currentMediaSource: MediaSource? = null

    private var currentTrackIndex: Int = -1

    init {
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPositionDiscontinuity(reason: Int) {
                // Update the current position when the track changes
                _currentPosition.value = exoPlayer.currentPosition
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                // Update the track duration and current position when the player state changes
                if (playbackState == ExoPlayer.STATE_READY) {
                    _trackDuration.value = exoPlayer.duration
                    _currentPosition.value = exoPlayer.currentPosition
                } else if (playbackState == ExoPlayer.STATE_ENDED) {
                    // The current track has ended, handle looping or play next track
                    if (_isLooping.value && currentTrackIndex != -1) {
                        //exoPlayer.seekToDefaultPosition()
                        exoPlayer.playWhenReady = true
                    } else {
                        playNextTrack()
                    }
                }
            }
        })
    }

    fun setCurrentTrack() {
        if (currentTrackIndex != -1) {
            _currentTrack.value = playlist.value[currentTrackIndex]
        } else {
            _currentTrack.value = null
        }
    }

    private fun startPlayback() {
        viewModelScope.launch {
            while (true) {
                Log.d("Player duration (ms): ", exoPlayer.currentPosition.toString())
                _currentPosition.value = exoPlayer.currentPosition
                delay(100)
            }
        }
    }

    fun changeTrackProgress(progress: Long) {
        exoPlayer.seekTo(progress)
    }

    fun stopPlayback() {
        viewModelScope.coroutineContext.cancelChildren()
    }

    private fun play() {
        startPlayback()
        exoPlayer.playWhenReady = true
        _isPlaying.value = exoPlayer.playWhenReady
        playlist.value[currentTrackIndex].isPlaying = true
    }

    private fun pause() {
        stopPlayback()
        exoPlayer.playWhenReady = false
        _isPlaying.value = false
        playlist.value[currentTrackIndex].isPlaying = false
    }

    fun chooseAndPlayTrack(trackInfo: TrackInfo) {
        stopPlayback()
        var index = playlist.value.indexOfFirst {
            (it.id == trackInfo.id && it.externalId == trackInfo .externalId)
                    ||(it.title == trackInfo.title && it.artist == trackInfo.artist)
        }
        if (index == currentTrackIndex && exoPlayer.isPlaying) {
            pause()
        } else if (index == currentTrackIndex && !exoPlayer.isPlaying) {
            play()
        } else {
            if (currentTrackIndex != -1){
                playlist.value[currentTrackIndex].isPlaying = false
            }
            currentTrackIndex = index

            val mediaSource = trackToMediaSource(playlist.value[currentTrackIndex])
            //TODO Gapless effect
            /*Thread.sleep(500)*/
            exoPlayer.prepare(mediaSource)
            currentMediaSource = mediaSource
            play()
        }
        setCurrentTrack()
    }

    fun playNextTrack() {
        if (currentMediaSource == null) return
        stopPlayback()
        if (currentTrackIndex != -1){
            playlist.value[currentTrackIndex].isPlaying = false
        }
        currentTrackIndex++
        if (currentTrackIndex > playlist.value.size - 1) {
            currentTrackIndex = 0
        }
        val mediaSource = trackToMediaSource(playlist.value[currentTrackIndex])
        exoPlayer.prepare(mediaSource)
        currentMediaSource = mediaSource
        play()
        setCurrentTrack()
    }

    fun playPreviousTrack() {
        stopPlayback()
        if (currentTrackIndex != -1){
            playlist.value[currentTrackIndex].isPlaying = false
        }
        currentTrackIndex--
        if (currentTrackIndex < 0) {
            currentTrackIndex = playlist.value.size - 1
        }
        val mediaSource = trackToMediaSource(playlist.value[currentTrackIndex])
        exoPlayer.prepare(mediaSource)
        currentMediaSource = mediaSource
        play()
        setCurrentTrack()
    }

    fun setLooping(looping: Boolean) {
        _isLooping.value = looping
    }

    fun setPlaylist(newplaylist: List<TrackInfo>) {
        newplaylist.indexOfFirst { (it.id == _currentTrack.value?.id && it.externalId == _currentTrack.value?.externalId)||(it.title == _currentTrack.value?.title && it.artist == _currentTrack.value?.artist) }.let{
            currentTrackIndex = it
            if(currentTrackIndex != -1){
                newplaylist[currentTrackIndex].isPlaying = _isPlaying.value
            }
        }
        playlist.value = newplaylist as MutableList<TrackInfo>
    }

    private fun trackToMediaSource(trackInfo: TrackInfo): MediaSource {
        return trackInfo.onlineSource?.let{
            mediaSourceFactory.createMediaSource(Uri.parse(it))
        } ?: mediaSourceFactory.createMediaSource(Uri.parse(trackInfo.localPath))
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
        currentMediaSource = null
    }
}
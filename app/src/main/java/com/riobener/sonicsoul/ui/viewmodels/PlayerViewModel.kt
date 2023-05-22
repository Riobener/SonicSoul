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
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.riobener.sonicsoul.data.music.TrackInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
    private val state: SavedStateHandle,
    private val exoPlayer: SimpleExoPlayer,
    private val mediaSourceFactory: ProgressiveMediaSource.Factory
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

    private var currentMediaSource: MediaSource? = null

    private val playlist: MutableList<TrackInfo> = mutableListOf()
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
            _currentTrack.value = playlist[currentTrackIndex]
        } else {
            _currentTrack.value = null
        }
    }

    private fun startPlayback() {
        viewModelScope.launch {
            while (true) {
                Log.d("Player duration (ms): ", exoPlayer.currentPosition.toString())
                _currentPosition.value = exoPlayer.currentPosition
                delay(1000)
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
        playlist[currentTrackIndex].isPlaying = true
    }

    private fun pause() {
        stopPlayback()
        exoPlayer.playWhenReady = false
        _isPlaying.value = false
        playlist[currentTrackIndex].isPlaying = false
    }

    fun chooseAndPlayTrack(trackInfo: TrackInfo) {
        getPlaylist().forEach{
            Log.d("LIST BEFORE", it.isPlaying.toString())
        }
        stopPlayback()
        val index = playlist.indexOf(trackInfo)
        if (index == currentTrackIndex && exoPlayer.isPlaying) {
            pause()
        } else if (index == currentTrackIndex && !exoPlayer.isPlaying) {
            play()
        } else {
            if (currentTrackIndex != -1){
                playlist[currentTrackIndex].isPlaying = false
            }
            currentTrackIndex = index
            val mediaSource = trackToMediaSource(playlist[currentTrackIndex])
            //TODO Gapless effect
            /*Thread.sleep(500)*/
            exoPlayer.prepare(mediaSource)
            currentMediaSource = mediaSource
            play()
        }
        Log.d("LIST AFTER", "\n")
        getPlaylist().forEach{
            Log.d("LIST AFTER", it.isPlaying.toString())
        }
        Log.d("LIST AFTER", "\n")
        setCurrentTrack()
    }

    fun playNextTrack() {
        if (currentMediaSource == null) return
        stopPlayback()
        if (currentTrackIndex != -1){
            playlist[currentTrackIndex].isPlaying = false
        }
        currentTrackIndex++
        if (currentTrackIndex > playlist.size - 1) {
            currentTrackIndex = 0
        }
        val mediaSource = trackToMediaSource(playlist[currentTrackIndex])
        exoPlayer.prepare(mediaSource)
        currentMediaSource = mediaSource
        play()
        setCurrentTrack()
    }

    fun playPreviousTrack() {
        stopPlayback()
        if (currentTrackIndex != -1){
            playlist[currentTrackIndex].isPlaying = false
        }
        currentTrackIndex--
        if (currentTrackIndex < 0) {
            currentTrackIndex = playlist.size - 1
        }
        val mediaSource = trackToMediaSource(playlist[currentTrackIndex])
        exoPlayer.prepare(mediaSource)
        currentMediaSource = mediaSource
        play()
        setCurrentTrack()
    }

    fun setLooping(looping: Boolean) {
        _isLooping.value = looping
    }

    fun setPlaylist(newPlaylist: List<TrackInfo>, fromStart: Boolean) {
        if(fromStart)
            currentTrackIndex = -1
        playlist.clear()
        playlist.addAll(newPlaylist)
    }

    fun getPlaylist() : MutableList<TrackInfo>{
        return playlist
    }

    private fun trackToMediaSource(trackInfo: TrackInfo): MediaSource {
        return trackInfo.trackSource?.let{
            mediaSourceFactory.createMediaSource(Uri.parse(it))
        } ?: mediaSourceFactory.createMediaSource(Uri.parse(trackInfo.localPath))
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
        currentMediaSource = null
    }
}
package com.riobener.sonicsoul.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.riobener.sonicsoul.data.music.TrackInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
                checkForNextTrack()
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                // Update the track duration and current position when the player state changes
                if (playbackState == ExoPlayer.STATE_READY) {
                    _trackDuration.value = exoPlayer.duration
                    _currentPosition.value = exoPlayer.currentPosition
                } else if (playbackState == ExoPlayer.STATE_ENDED) {
                    // The current track has ended, handle looping or play next track
                    if (_isLooping.value && currentTrackIndex != -1) {
                        exoPlayer.seekToDefaultPosition()
                        exoPlayer.playWhenReady = true
                    } else {
                        playNextTrack()
                    }
                }
            }
        })
    }

    fun play() {
        if (exoPlayer.playbackState == SimpleExoPlayer.STATE_ENDED) {
            // Restart the playback from the beginning
            exoPlayer.seekToDefaultPosition()
        }
        exoPlayer.playWhenReady = true
        _isPlaying.value = exoPlayer.playWhenReady
    }

    fun pause() {
        exoPlayer.playWhenReady = false
        _isPlaying.value = false
    }

    fun chooseTrack(trackInfo: TrackInfo) {
        val index = playlist.indexOf(trackInfo)
        currentTrackIndex = index
        val mediaSource = trackToMediaSource(playlist[currentTrackIndex])
        exoPlayer.prepare(mediaSource)
        currentMediaSource = mediaSource
        play()
    }

    fun playNextTrack() {
        // Increment the track index
        currentTrackIndex++
        if (currentTrackIndex < playlist.size) {
            val mediaSource = trackToMediaSource(playlist[currentTrackIndex])
            exoPlayer.prepare(mediaSource)
            currentMediaSource = mediaSource
        } else {
            // End of the playlist, stop playback
            exoPlayer.stop()
            currentMediaSource = null
            currentTrackIndex = -1
            _currentTrack.value = null
        }
    }

    fun playPreviousTrack() {
        // Decrement the track index
        currentTrackIndex--
        if (currentTrackIndex < 0) {
            currentTrackIndex = playlist.size - 1
        }
        val mediaSource = trackToMediaSource(playlist[currentTrackIndex])
        exoPlayer.prepare(mediaSource)
        currentMediaSource = mediaSource
    }

    private fun checkForNextTrack() {
        if (currentMediaSource == null) return
        val totalTracks = playlist.size
        if (currentTrackIndex >= totalTracks - 1 && exoPlayer.playbackState == ExoPlayer.STATE_ENDED) {
            // The last track in the playlist has ended, stop playback
            if (isLooping.value) {
                currentTrackIndex = 0
                val mediaSource = trackToMediaSource(playlist[currentTrackIndex])
                exoPlayer.prepare(mediaSource)
                currentMediaSource = mediaSource
            } else {
                exoPlayer.stop()
                currentMediaSource = null
                currentTrackIndex = -1
                _currentTrack.value = null
            }
        }
    }

    fun setLooping(looping: Boolean) {
        _isLooping.value = looping
    }

    fun setPlaylist(newPlaylist: List<TrackInfo>) {
        playlist.clear()
        playlist.addAll(newPlaylist)
        currentTrackIndex = -1
    }

    fun trackToMediaSource(trackInfo: TrackInfo): MediaSource {
        return mediaSourceFactory.createMediaSource(Uri.parse(trackInfo.trackSource))
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
        currentMediaSource = null
    }
}
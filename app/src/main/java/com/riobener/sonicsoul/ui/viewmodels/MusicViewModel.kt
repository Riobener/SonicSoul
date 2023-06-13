package com.riobener.sonicsoul.ui.viewmodels

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riobener.sonicsoul.data.music.TrackInfo
import com.riobener.sonicsoul.data.music.TrackRepository
import com.riobener.sonicsoul.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.riobener.sonicsoul.data.music.Track
import com.riobener.sonicsoul.data.music.TrackSource
import com.riobener.sonicsoul.utils.HashUtils
import kotlinx.coroutines.flow.filter
import java.io.File
import java.util.*


@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
    private val trackRepository: TrackRepository,
) : ViewModel() {
    //Loading
    private val loadingMutableStateFlow = MutableStateFlow(false)
    val loadingFlow: Flow<Boolean>
        get() = loadingMutableStateFlow.asStateFlow()

    //Music
    private val musicInfoMutableStateFlow = MutableStateFlow<List<TrackInfo>>(emptyList())
    val musicInfoFlow: Flow<List<TrackInfo>>
        get() = musicInfoMutableStateFlow.asStateFlow()

    //SearchMusic
    private val musicSearchMutableStateFlow = MutableStateFlow<List<TrackInfo>>(emptyList())
    val musicSearch: Flow<List<TrackInfo>>
        get() = musicSearchMutableStateFlow.asStateFlow()

    var needToReload = true
    var isOffline = false

    fun loadMusic(withRefresh: Boolean = false) {
        if (withRefresh) {
            musicInfoMutableStateFlow.value = emptyList()
            musicSearchMutableStateFlow.value = emptyList()
        }
        if (isOffline) {
            loadOfflineMusic()
        } else {
            loadOnlineMusic()
        }
    }

    fun searchMusic(query: String?) {
        query?.lowercase()?.let { querySearch ->
            musicSearchMutableStateFlow.value = musicInfoMutableStateFlow.value.filter {
                it.artist.lowercase().contains(querySearch) || it.title.lowercase().contains(querySearch)
            }
            Log.d("SEARCH", musicSearchMutableStateFlow.value.toString())
        }
    }

    fun sortMusic() {
        musicInfoMutableStateFlow.value = musicInfoMutableStateFlow.value.reversed()
    }

    fun loadOnlineMusic() {
        viewModelScope.launch {
            loadingMutableStateFlow.value = true
            runCatching {
                trackRepository.getOnlineTracks()
            }.onSuccess {
                musicInfoMutableStateFlow.value = it
                loadingMutableStateFlow.value = false
            }.onFailure {
                loadingMutableStateFlow.value = false
                musicInfoMutableStateFlow.value = emptyList()
            }
        }
    }

    fun loadOfflineMusic() {
        viewModelScope.launch {
            loadingMutableStateFlow.value = true
            runCatching {
                trackRepository.findLocalTrackBySource(TrackSource.LOCAL)
            }.onSuccess {
                musicInfoMutableStateFlow.value = it
                loadingMutableStateFlow.value = false
            }.onFailure {
                loadingMutableStateFlow.value = false
                musicInfoMutableStateFlow.value = emptyList()
            }
        }
    }

    fun saveSong(trackInfo: TrackInfo) {
        trackInfo.localPath = trackInfo.onlineSource
        viewModelScope.launch {
            trackInfo.localPath?.let {
                trackRepository.save(
                    Track.create(
                        externalId = trackInfo.externalId,
                        title = trackInfo.title,
                        artist = trackInfo.artist,
                        source = trackInfo.trackSource,
                        imageSource = trackInfo.imageSource,
                        bigImageSource = trackInfo.bigImageSource,
                        localPath = it,
                        hash = HashUtils.hashStringWithSHA256(trackInfo.artist + trackInfo.title + trackInfo.externalId)
                    )
                )
                loadMusic()
            }
        }
    }

    fun deleteSong(id: UUID) {
        viewModelScope.launch {
            trackRepository.deleteById(id)
            loadMusic()
        }
    }

    fun saveLocalMusicToDatabase(localPath: String) {
        viewModelScope.launch {
            loadingMutableStateFlow.value = true
            runCatching {
                trackRepository.deleteAllBySource(TrackSource.LOCAL)
                val mmr = MediaMetadataRetriever()
                Log.d("Files", "Path: $localPath")
                val directory = File(localPath)
                val files = directory.listFiles()
                files?.let {
                    Log.d("Files", "Size: " + files.size)
                    for (i in files.indices) {
                        val musicPath = localPath + "/" + files[i].getName()
                        val fileName = files[i].getName()
                        Log.d("Files", "FileName:" + files[i].getName())
                        if (fileName.contains(".mp3", ignoreCase = true) || fileName.contains(
                                ".wav",
                                ignoreCase = true
                            )
                        ) {
                            mmr.setDataSource(musicPath)
                            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "NoAuthor"
                            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: fileName
                            val date = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                            val bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                            val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            val hash = HashUtils.hashStringWithSHA256(artist + title + date + bitrate + duration)
                            trackRepository.save(
                                Track.create(
                                    externalId = null,
                                    title = title,
                                    artist = artist,
                                    source = TrackSource.LOCAL,
                                    imageSource = null,
                                    bigImageSource = null,
                                    localPath = musicPath,
                                    hash = hash
                                )
                            )
                        }
                    }
                }
            }.onSuccess {
                loadingMutableStateFlow.value = false
            }.onFailure {
                loadingMutableStateFlow.value = false
                throw it
            }
        }

    }

}
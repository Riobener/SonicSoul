package com.riobener.sonicsoul.ui.viewmodels

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.data.auth.ServiceCredentialsRepository
import com.riobener.sonicsoul.data.auth.spotify.SpotifyAuthRepository
import com.riobener.sonicsoul.data.music.TrackInfo
import com.riobener.sonicsoul.data.music.TrackRepository
import com.riobener.sonicsoul.data.music.spotify.SpotifyMusicRepository
import com.riobener.sonicsoul.data.settings.SettingsName
import com.riobener.sonicsoul.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.os.Environment
import android.util.Log
import com.riobener.sonicsoul.data.music.Track
import com.riobener.sonicsoul.data.music.TrackSource
import com.riobener.sonicsoul.utils.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.*


@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
    private val spotifyMusicRepository: SpotifyMusicRepository,
    private val trackRepository: TrackRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    //Loading
    private val loadingMutableStateFlow = MutableStateFlow(false)
    val loadingFlow: Flow<Boolean>
        get() = loadingMutableStateFlow.asStateFlow()

    //Music
    private val musicInfoMutableStateFlow = MutableStateFlow<List<TrackInfo>>(emptyList())
    val musicInfoFlow: Flow<List<TrackInfo>>
        get() = musicInfoMutableStateFlow.asStateFlow()

    var alreadyLoaded = false
    var isOffline = false

    fun loadMusic() {
        if (isOffline) {
            loadOfflineMusic()
        } else {
            loadOnlineMusic()
        }
    }

    fun loadOnlineMusic() {
        viewModelScope.launch {
            loadingMutableStateFlow.value = true
            runCatching {
                spotifyMusicRepository.getTracks()
            }.onSuccess {
                musicInfoMutableStateFlow.value = it
                alreadyLoaded = true
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
                trackRepository.findAllBySource(TrackSource.LOCAL)
            }.onSuccess {
                musicInfoMutableStateFlow.value = it
                alreadyLoaded = true
                loadingMutableStateFlow.value = false
            }.onFailure {
                loadingMutableStateFlow.value = false
                musicInfoMutableStateFlow.value = emptyList()
            }
        }
    }

    fun saveLocalMusicToDatabase(localPath: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                trackRepository.deleteAllBySource(TrackSource.LOCAL)
            }
            loadingMutableStateFlow.value = true

            val mmr = MediaMetadataRetriever()
            Log.d("Files", "Path: $localPath")
            val formatedFilePaths = mutableListOf<FilePathWithName>()
            val directory = File(localPath)
            val files = directory.listFiles()
            files?.let {
                Log.d("Files", "Size: " + files.size)
                for (i in files.indices) {
                    Log.d("Files", "FileName:" + files[i].getName())
                    formatedFilePaths.add(FilePathWithName(localPath + "/" + files[i].getName(), files[i].getName()))
                }
            }
            formatedFilePaths.forEach { musicPath ->
                if (musicPath.name.contains(".mp3", ignoreCase = true) || musicPath.name.contains(
                        ".wav",
                        ignoreCase = true
                    )
                ) {
                    mmr.setDataSource(musicPath.path)
                    val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "NoAuthor"
                    val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: musicPath.name
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
                            localPath = musicPath.path,
                            hash = hash
                        )
                    )
                }
            }
            loadingMutableStateFlow.value = false
        }

    }

    data class FilePathWithName(
        val path: String,
        val name: String,
    )

}
package com.riobener.sonicsoul.ui.viewmodels

import android.content.Context
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
import java.io.File


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

    fun loadOnlineMusic() {
        viewModelScope.launch {
            loadingMutableStateFlow.value = true
            runCatching {
                spotifyMusicRepository.getTracks()
            }.onSuccess {
                musicInfoMutableStateFlow.value = it
                alreadyLoaded = true
                loadingMutableStateFlow.value = false
                getAllFilesInLocal()
            }.onFailure {
                loadingMutableStateFlow.value = false
                musicInfoMutableStateFlow.value = emptyList()
            }
        }
    }

    fun getAllFilesInLocal(){
        viewModelScope.launch {
            settingsRepository.findBySettingsName(settingsName = SettingsName.LOCAL_DIRECTORY_PATH)?.value?.let{ path ->
                Log.d("Files", "Path: $path")
                val directory = File(path)
                val files = directory.listFiles()
                files?.let{
                    Log.d("Files", "Size: " + files.size)
                    for (i in files.indices) {
                        Log.d("Files", "FileName:" + files[i].getName())
                    }
                }
            }
        }
    }

}
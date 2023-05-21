package com.riobener.sonicsoul.data.music.spotify

import com.riobener.sonicsoul.data.music.TrackInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SpotifyMusicRepository
@Inject constructor(private val spotifyApi: SpotifyApi) {

    suspend fun getTracks(): List<TrackInfo> {
        return spotifyApi.tracks("50", "0").items.map { it.toTrackInfo() }
    }


}
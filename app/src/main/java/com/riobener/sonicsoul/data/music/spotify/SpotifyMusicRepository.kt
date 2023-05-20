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

/*    val spotifyTracks: Flow<List<TrackInfo>> = flow {
        while(true) {
            val latestTracks = spotifyApi.tracks("50", "0").items.map { it.toTrackInfo() }
            emit(latestTracks) // Emits the result of the request to the flow
            delay(5000) // Suspends the coroutine for some time
        }
    }*/

}
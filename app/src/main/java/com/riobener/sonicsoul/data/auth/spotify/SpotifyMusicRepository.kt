package com.riobener.sonicsoul.data.auth.spotify

import javax.inject.Inject

class SpotifyMusicRepository
@Inject constructor(private val spotifyApi: SpotifyApi) {

    suspend fun getTracks(): SpotifyTrackResponse {
        return spotifyApi.tracks("10", "0")
    }

}
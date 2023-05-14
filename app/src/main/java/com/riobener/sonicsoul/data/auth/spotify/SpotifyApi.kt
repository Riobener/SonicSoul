package com.riobener.sonicsoul.data.auth.spotify

import retrofit2.http.GET
import retrofit2.http.Query

interface SpotifyApi {
    @GET("me/tracks")
    suspend fun tracks(
        @Query(value = "limit") limit: String,
        @Query(value = "offset") offset: String,
    ): SpotifyTrackResponse
}
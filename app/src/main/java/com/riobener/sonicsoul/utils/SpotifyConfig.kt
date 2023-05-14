package com.riobener.sonicsoul.utils

import com.riobener.sonicsoul.BuildConfig

object SpotifyConfig : ServiceConfig {
    override val BASE_URL: String = "https://api.soundcloud.com/"
    override val REDIRECT_URI: String = "com.riobener.sonicsoul://spotifyauth"
    override val AUTHORIZE_URL: String = "https://accounts.spotify.com/authorize"
    override val TOKEN_URL: String = "https://accounts.spotify.com/api/token"
    override val SCOPES: List<String> = listOf("playlist-read-private", "user-library-read")
    override val CLIENT_ID: String = BuildConfig.SPOTIFY_CLIENT_ID
    override val CLIENT_SECRET: String = BuildConfig.SPOTIFY_CLIENT_SECRET
}
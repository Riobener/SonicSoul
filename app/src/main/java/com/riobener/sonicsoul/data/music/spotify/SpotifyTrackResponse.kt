package com.riobener.sonicsoul.data.music.spotify

import com.google.gson.annotations.SerializedName

data class SpotifyTrackResponse(
    @SerializedName("items")
    val items: List<Item>
)

data class Item(
    @SerializedName("track")
    val track: Track
)

data class Track(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("album")
    val album: Album,
    @SerializedName("artists")
    val artists: List<Artist>
)

data class Album(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("images")
    val images: List<Image>
)

data class Image(
    @SerializedName("url")
    val url: String,
)

data class Artist(
    @SerializedName("name")
    val name: String
)
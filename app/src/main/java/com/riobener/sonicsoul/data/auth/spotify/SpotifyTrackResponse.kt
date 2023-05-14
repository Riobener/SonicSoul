package com.riobener.sonicsoul.data.auth.spotify

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
    val name: String
)
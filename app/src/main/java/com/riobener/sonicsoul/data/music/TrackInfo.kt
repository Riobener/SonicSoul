package com.riobener.sonicsoul.data.music

data class TrackInfo(
    val externalId: String,
    val title: String,
    val artistName: String,
    val imageSource: String?,
    val bigImageSource: String?,
    val trackSource: String?,
    var isPlaying: Boolean,
)
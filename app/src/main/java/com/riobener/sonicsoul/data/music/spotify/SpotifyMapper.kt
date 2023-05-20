package com.riobener.sonicsoul.data.music.spotify

import com.riobener.sonicsoul.data.music.TrackInfo

fun Item.toTrackInfo() = TrackInfo(
    externalId = track.id,
    title = track.name,
    artist = track.artists.joinToString(separator = ", ") { it.name },
    imageSource = track.album.images.lastOrNull()?.url,
    bigImageSource = track.album.images.firstOrNull()?.url,
    trackSource = track.url,
    isPlaying = false,
    id = null,
    localPath = null,
)
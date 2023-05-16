package com.riobener.sonicsoul.data.music.spotify

import com.riobener.sonicsoul.data.music.TrackInfo

fun Item.toTrackInfo() = TrackInfo(
    externalId = track.id,
    title = track.name,
    artistName = track.artists.joinToString(separator = ", ") { it.name },
    imageSource = track.album.images.lastOrNull()?.url
)
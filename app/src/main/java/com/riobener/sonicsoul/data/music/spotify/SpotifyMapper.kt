package com.riobener.sonicsoul.data.music.spotify

import com.riobener.sonicsoul.data.music.TrackInfo
import com.riobener.sonicsoul.data.music.TrackSource

fun Item.toTrackInfo() = TrackInfo(
    externalId = track.id,
    title = track.name,
    artist = track.artists.joinToString(separator = ", ") { it.name },
    imageSource = track.album.images.lastOrNull()?.url,
    bigImageSource = track.album.images.firstOrNull()?.url,
    onlineSource = track.url,
    isPlaying = false,
    id = null,
    localPath = null,
    trackSource = TrackSource.SPOTIFY
)
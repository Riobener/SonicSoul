package com.riobener.sonicsoul.data.music

import com.riobener.sonicsoul.data.music.spotify.Item

fun Track.toTrackInfo() = TrackInfo(
    id = id,
    externalId = externalId,
    title = title,
    artist = artist,
    imageSource = imageSource,
    bigImageSource = imageSource,
    trackSource = null,
    isPlaying = false,
    localPath = localPath,
)
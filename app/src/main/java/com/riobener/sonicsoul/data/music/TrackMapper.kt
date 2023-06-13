package com.riobener.sonicsoul.data.music

fun Track.toTrackInfo() = TrackInfo(
    id = id,
    externalId = externalId,
    title = title,
    artist = artist,
    imageSource = imageSource,
    bigImageSource = imageSource,
    onlineSource = null,
    isPlaying = false,
    localPath = localPath,
    trackSource = source,
)
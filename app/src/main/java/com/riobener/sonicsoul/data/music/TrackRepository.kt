package com.riobener.sonicsoul.data.music

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackRepository
@Inject constructor(
    private val trackDao: TrackDao,
) {
    suspend fun save(track: Track) {
        trackDao.findByHash(track.hash)?.let {
            it.title = track.title
            it.artist = track.artist
            it.imageSource = track.imageSource
            it.bigImageSource = track.bigImageSource
            it.localPath = track.localPath
            it.hash = track.hash
            trackDao.save(it)
        } ?: trackDao.save(track)
    }

    suspend fun findAllBySource(source: TrackSource): List<TrackInfo> {
        return trackDao.findAllBySource(source.name).map{it.toTrackInfo()}
    }
}
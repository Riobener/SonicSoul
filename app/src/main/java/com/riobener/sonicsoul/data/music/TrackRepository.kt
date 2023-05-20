package com.riobener.sonicsoul.data.music

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackRepository
@Inject constructor(
    private val trackDao: TrackDao,
) {
    suspend fun save(track: Track) {
        trackDao.findByIdOrExternalId(id = track.id, externalId = track.externalId)?.let {
            it.title = track.title
            it.artist = track.artist
            it.imageSource = track.imageSource
            it.bigImageSource = track.bigImageSource
            it.localPath = track.localPath
            it.hash = track.hash
            trackDao.save(it)
        } ?: trackDao.save(track)
    }

    fun findAllBySource(source: TrackSource): Flow<List<Track>> {
        return trackDao.findAllBySource(source)
    }
}
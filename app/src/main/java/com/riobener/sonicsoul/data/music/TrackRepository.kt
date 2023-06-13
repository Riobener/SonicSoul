package com.riobener.sonicsoul.data.music

import com.riobener.sonicsoul.data.music.spotify.SpotifyApi
import com.riobener.sonicsoul.data.music.spotify.toTrackInfo
import okhttp3.OkHttpClient
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class TrackRepository
@Inject constructor(
    private val trackDao: TrackDao,
    private val spotifyApi: SpotifyApi,
) {
    suspend fun getOnlineTracks(): List<TrackInfo> {
        val offlineTracks = findLocalTrackBySource(TrackSource.SPOTIFY) as MutableList
        return try{
            val onlineTracks = spotifyApi.tracks("50", "0").items.map { it.toTrackInfo() } as MutableList
            offlineTracks.forEach { offlineValue ->
                val conditionValue = onlineTracks.firstOrNull{ it.externalId == offlineValue.externalId }
                if(conditionValue != null){
                    onlineTracks[onlineTracks.indexOf(conditionValue)] = offlineValue
                }
            }
            onlineTracks
        }catch (e: Exception){
            offlineTracks
        }
    }

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

    suspend fun deleteAllBySource(source: TrackSource){
        trackDao.deleteAllBySource(source = source.name)
    }

    suspend fun deleteById(id: UUID){
        trackDao.deleteById(id)
    }

    suspend fun findLocalTrackBySource(source: TrackSource): List<TrackInfo> {
        return trackDao.findAllBySource(source.name).map{it.toTrackInfo()}
    }
}
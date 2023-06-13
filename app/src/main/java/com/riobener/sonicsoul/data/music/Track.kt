package com.riobener.sonicsoul.data.music

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

data class TrackInfo(
    val id: UUID?,
    val externalId: String?,
    val title: String,
    val artist: String,
    val imageSource: String?,
    val bigImageSource: String?,
    val onlineSource: String?,
    val trackSource: TrackSource,
    var localPath: String?,
    var isPlaying: Boolean,
)

@Entity(tableName = "track")
data class Track(
    @PrimaryKey val id: UUID,
    @ColumnInfo(name = "external_id") val externalId: String?,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "artist") var artist: String,
    @ColumnInfo(name = "source") val source: TrackSource,
    @ColumnInfo(name = "image_source") var imageSource: String?,
    @ColumnInfo(name = "big_image_source") var bigImageSource: String?,
    @ColumnInfo(name = "local_path") var localPath: String,
    @ColumnInfo(name = "hash") var hash: String,
    @ColumnInfo(name = "created_at") var createdAt: Long,
    @ColumnInfo(name = "updated_at") var updatedAt: Long,
) {
    companion object {
        fun create(
            externalId: String?,
            title: String,
            artist: String,
            source: TrackSource,
            imageSource: String?,
            bigImageSource: String?,
            localPath: String,
            hash: String,
        ): Track {
            return Track(
                id = UUID.randomUUID(),
                externalId = externalId,
                title = title,
                artist = artist,
                source = source,
                imageSource = imageSource,
                bigImageSource = bigImageSource,
                localPath = localPath,
                hash = hash,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }
}

enum class TrackSource {
    LOCAL,
    SPOTIFY,
    //list of other track sources...
}

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(track: Track)

    @Query("DELETE FROM track WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM track WHERE source = :source")
    suspend fun deleteAllBySource(source: String)

    @Query("SELECT * FROM track WHERE source = :source")
    suspend fun findAllBySource(source: String): List<Track>

    @Query("SELECT * FROM track WHERE (id = :id or (external_id = :externalId and :externalId is not null)) ")
    suspend fun findByIdOrExternalId(id: UUID?, externalId: String?): Track?

    @Query("SELECT * FROM track WHERE hash = :hash ")
    suspend fun findByHash(hash: String): Track?
}
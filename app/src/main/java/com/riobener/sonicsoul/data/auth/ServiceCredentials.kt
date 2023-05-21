package com.riobener.sonicsoul.data.auth

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.*


@Entity(tableName = "service_credentials")
data class ServiceCredentials(
    @PrimaryKey val id: UUID,
    @ColumnInfo(name = "service_name") val serviceName: ServiceName,
    @ColumnInfo(name = "access_token") var accessToken: String,
    @ColumnInfo(name = "refresh_token") var refreshToken: String?,
    var createdAt: Long,
    var updatedAt: Long,
) {
    companion object {
        fun create(
            serviceName: ServiceName,
            accessToken: String,
            refreshToken: String?
        ): ServiceCredentials {
            return ServiceCredentials(
                id = UUID.randomUUID(),
                serviceName = serviceName,
                accessToken = accessToken,
                refreshToken = refreshToken,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }
}

enum class ServiceName {
    SPOTIFY
    //list of other services...
}

@Dao
interface ServiceCredentialsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(serviceCredentials: ServiceCredentials)

    @Query("DELETE FROM service_credentials WHERE service_name = :serviceName")
    suspend fun deleteByServiceName(serviceName: String)

    @Query("SELECT * FROM service_credentials WHERE service_name = :serviceName")
    suspend fun findByServiceName(serviceName: String): ServiceCredentials?

    @Query("SELECT * FROM service_credentials WHERE service_name = :serviceName")
    fun findByServiceNameFlow(serviceName: String): Flow<ServiceCredentials?>
}
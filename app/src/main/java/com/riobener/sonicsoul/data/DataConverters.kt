package com.riobener.sonicsoul.data

import androidx.room.TypeConverter
import com.riobener.sonicsoul.data.auth.ServiceName
import com.riobener.sonicsoul.data.music.TrackSource
import com.riobener.sonicsoul.data.settings.SettingsName
import java.util.*

object DataConverters {
    //UUID
    @TypeConverter
    fun fromUUID(uuid: UUID): String = uuid.toString()
    @TypeConverter
    fun uuidFromString(value: String?): UUID? = value?.let { UUID.fromString(it) }

    //ENUM ServiceName
    @TypeConverter
    fun toServiceName(value: String) = enumValueOf<ServiceName>(value)
    @TypeConverter
    fun fromServiceName(value: ServiceName) = value.name

    //ENUM SettingsName
    @TypeConverter
    fun toSettingsName(value: String) = enumValueOf<SettingsName>(value)
    @TypeConverter
    fun fromSettingsName(value: SettingsName) = value.name

    //ENUM TrackSource
    @TypeConverter
    fun toTrackSource(value: String) = enumValueOf<TrackSource>(value)
    @TypeConverter
    fun fromTrackSource(value: TrackSource) = value.name
}
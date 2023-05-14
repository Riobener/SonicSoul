package com.riobener.sonicsoul.data

import androidx.room.TypeConverter
import com.riobener.sonicsoul.data.auth.ServiceName
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
}
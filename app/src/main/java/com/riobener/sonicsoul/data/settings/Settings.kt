package com.riobener.sonicsoul.data.settings

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: UUID,
    @ColumnInfo(name = "settings_name") val name: SettingsName,
    @ColumnInfo(name = "settings_value") var value: String,
) {
    companion object {
        fun create(
            name: SettingsName,
            value: String,
        ): Settings {
            return Settings(
                id = UUID.randomUUID(),
                name = name,
                value = value,
            )
        }
    }
}

enum class SettingsName {
    LOCAL_DIRECTORY_PATH,
    IS_GAPLESS,
    THEME_APP,
    LANGUAGE,
    //list of other settings...
}

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: Settings)

    @Query("SELECT * FROM settings WHERE settings_name = :settingsName")
    suspend fun findBySettingsName(settingsName: String): Settings?

    @Query("SELECT * FROM settings")
    fun findAll(): Flow<List<Settings>>
}
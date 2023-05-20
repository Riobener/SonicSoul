package com.riobener.sonicsoul.data.settings

import com.riobener.sonicsoul.data.auth.ServiceCredentials
import com.riobener.sonicsoul.data.auth.ServiceCredentialsDao
import com.riobener.sonicsoul.data.auth.ServiceName
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepository
@Inject constructor(
    private val settingsDao: SettingsDao,
) {
    suspend fun save(settings: Settings) {
        settingsDao.findBySettingsName(settingsName = settings.name)?.let {
            it.value = settings.value
            settingsDao.save(it)
        } ?: settingsDao.save(settings)
    }

    fun findAllSettings(): Flow<List<Settings>> {
        return settingsDao.findAll()
    }
}
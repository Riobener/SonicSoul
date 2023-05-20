package com.riobener.sonicsoul.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.riobener.sonicsoul.data.auth.ServiceCredentials
import com.riobener.sonicsoul.data.auth.ServiceCredentialsDao
import com.riobener.sonicsoul.data.settings.Settings
import com.riobener.sonicsoul.data.settings.SettingsDao
import com.riobener.sonicsoul.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [ServiceCredentials::class, Settings::class], version = 1)
@TypeConverters(value = [DataConverters::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun serviceCredentialsDao(): ServiceCredentialsDao
    abstract fun settingsDao(): SettingsDao

    class Callback @Inject constructor(
        private val database: Provider<AppDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) : RoomDatabase.Callback()

}
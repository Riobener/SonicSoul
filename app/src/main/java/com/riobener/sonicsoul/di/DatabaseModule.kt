package com.riobener.sonicsoul.di

import android.app.Application
import androidx.room.Room
import com.riobener.sonicsoul.data.AppDatabase
import com.riobener.sonicsoul.data.auth.ServiceCredentialsDao
import com.riobener.sonicsoul.data.auth.ServiceCredentialsRepository
import com.riobener.sonicsoul.data.auth.spotify.SpotifyAuthRepository
import com.riobener.sonicsoul.data.settings.SettingsDao
import com.riobener.sonicsoul.data.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(app: Application, callback: AppDatabase.Callback): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "sonic_soul_database")
            .fallbackToDestructiveMigration().addCallback(callback).build()
    }

    @Provides
    fun provideServiceCredentialsDao(db: AppDatabase): ServiceCredentialsDao {
        return db.serviceCredentialsDao()
    }

    @Provides
    fun provideServiceCredentialsRepository(
        dao: ServiceCredentialsDao,
    ): ServiceCredentialsRepository {
        return ServiceCredentialsRepository(dao)
    }

    @Provides
    fun provideSpotifyAuthRepository(serviceCredentialsRepository: ServiceCredentialsRepository): SpotifyAuthRepository {
        return SpotifyAuthRepository(serviceCredentialsRepository)
    }

    @Provides
    fun provideSettingsDao(db: AppDatabase): SettingsDao {
        return db.settingsDao()
    }

    @Provides
    fun provideSettingsRepository(dao: SettingsDao): SettingsRepository {
        return SettingsRepository(dao)
    }
}
package com.riobener.sonicsoul.di

import android.app.Application
import androidx.room.Room
import com.riobener.sonicsoul.data.AppDatabase
import com.riobener.sonicsoul.data.entity.ServiceCredentialsDao
import com.riobener.sonicsoul.data.repository.ServiceCredentialsRepository
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
    fun provideServiceCredentialsRepository(dao: ServiceCredentialsDao): ServiceCredentialsRepository {
        return ServiceCredentialsRepository(dao)
    }
}
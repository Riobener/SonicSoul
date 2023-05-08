package com.riobener.sonicsoul.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.riobener.sonicsoul.data.entity.ServiceCredentials
import com.riobener.sonicsoul.data.entity.ServiceCredentialsDao
import com.riobener.sonicsoul.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [ServiceCredentials::class], version = 1)
@TypeConverters(value = [DataConverters::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun serviceCredentialsDao(): ServiceCredentialsDao

    class Callback @Inject constructor(
        private val database: Provider<AppDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) : RoomDatabase.Callback()

}
package com.riobener.sonicsoul.data.repository

import androidx.lifecycle.LiveData
import com.riobener.sonicsoul.data.entity.ServiceCredentials
import com.riobener.sonicsoul.data.entity.ServiceCredentialsDao
import com.riobener.sonicsoul.data.entity.ServiceName
import javax.inject.Inject

class ServiceCredentialsRepository
@Inject constructor(
    private val serviceCredentialsDao: ServiceCredentialsDao
) {
    suspend fun save(serviceCredentials: ServiceCredentials) {
        serviceCredentialsDao.save(serviceCredentials)
    }

    suspend fun findByServiceName(serviceName: ServiceName): ServiceCredentials? {
        return serviceCredentialsDao.findByServiceName(serviceName = serviceName.name)
    }
}
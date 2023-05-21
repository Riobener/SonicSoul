package com.riobener.sonicsoul.data.auth

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ServiceCredentialsRepository
@Inject constructor(
    private val serviceCredentialsDao: ServiceCredentialsDao,
) {
    suspend fun save(serviceCredentials: ServiceCredentials) {
        serviceCredentialsDao.findByServiceName(serviceCredentials.serviceName.name)?.let {
            it.accessToken = serviceCredentials.accessToken
            it.refreshToken = serviceCredentials.refreshToken
            it.updatedAt = serviceCredentials.updatedAt
            serviceCredentialsDao.save(it)
        } ?: serviceCredentialsDao.save(serviceCredentials)
    }

    suspend fun findByServiceName(serviceName: ServiceName): ServiceCredentials? {
        return serviceCredentialsDao.findByServiceName(serviceName = serviceName.name)
    }

    fun findByServiceNameFlow(serviceName: ServiceName): Flow<ServiceCredentials?> {
        return serviceCredentialsDao.findByServiceNameFlow(serviceName = serviceName.name)
    }

    suspend fun deleteByServiceName(serviceName: ServiceName) {
        serviceCredentialsDao.deleteByServiceName(serviceName = serviceName.name)
    }
}
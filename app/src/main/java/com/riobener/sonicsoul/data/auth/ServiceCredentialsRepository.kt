package com.riobener.sonicsoul.data.auth

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
}
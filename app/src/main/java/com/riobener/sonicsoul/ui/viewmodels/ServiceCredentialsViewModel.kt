package com.riobener.sonicsoul.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riobener.sonicsoul.data.entity.ServiceCredentials
import com.riobener.sonicsoul.data.entity.ServiceName
import com.riobener.sonicsoul.data.repository.ServiceCredentialsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceCredentialsViewModel @Inject constructor(private val repository: ServiceCredentialsRepository) : ViewModel() {

    private val serviceCredentialsLiveData: MutableLiveData<ServiceCredentials?> = MutableLiveData()
    val serviceCredentials: LiveData<ServiceCredentials?>
        get() = serviceCredentialsLiveData

    fun getServiceCredentials(serviceName: ServiceName) =
        viewModelScope.launch {
            val response = repository.findByServiceName(serviceName)
            serviceCredentialsLiveData.postValue(response)
        }

    fun saveServiceCredentials(serviceCredentials: ServiceCredentials) {
        viewModelScope.launch {
            repository.findByServiceName(serviceCredentials.serviceName)?.let{
                it.accessToken = serviceCredentials.accessToken
                it.refreshToken = serviceCredentials.refreshToken
                it.updatedAt = serviceCredentials.updatedAt
                repository.save(it)
            } ?: repository.save(serviceCredentials)
        }
    }

}
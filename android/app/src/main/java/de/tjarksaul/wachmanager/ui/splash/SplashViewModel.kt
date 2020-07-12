package de.tjarksaul.wachmanager.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tjarksaul.wachmanager.dtos.Station

class SplashViewModel : ViewModel() {
    fun updateData(data: MutableList<Station>) {
        _stations.value = data
    }

    private val _stations = MutableLiveData<MutableList<Station>>().apply {
        value = emptyList<Station>().toMutableList()
    }
    val stations: LiveData<MutableList<Station>> = _stations

}
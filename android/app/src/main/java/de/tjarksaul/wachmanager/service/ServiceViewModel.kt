package de.tjarksaul.wachmanager.service

import de.tjarksaul.wachmanager.iot.IotRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.amazonaws.regions.Regions
import de.tjarksaul.wachmanager.BuildConfig
import de.tjarksaul.wachmanager.iotClient.IotConfig

class ServiceViewModel(
    private val iotRepository: IotRepository
    ) {
    private val generator = MutableLiveData<Unit>()

    private fun something() {}

    init {
        generator.value = something()
    }

    val updates: LiveData<Unit> = Transformations.map(generator) {
        val config = IotConfig(
            iotEndpoint = BuildConfig.AWS_IOT_ENDPOINT,
            clientId = BuildConfig.AWS_IOT_CLIENT_ID,
            region = Regions.EU_CENTRAL_1
        )

        iotRepository.connect(config)
    }
}

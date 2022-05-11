package de.tjarksaul.wachmanager.service

import de.tjarksaul.wachmanager.iot.IotRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.amazonaws.regions.Regions
import de.tjarksaul.wachmanager.BuildConfig
import de.tjarksaul.wachmanager.iotClient.IotConfig
import de.tjarksaul.wachmanager.modules.auth.AuthRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class ServiceViewModel(
    private val iotRepository: IotRepository,
    authRepository: AuthRepository,
    ) {
    private val disposable = CompositeDisposable()
    private val generator = MutableLiveData<Unit>()

    init {
        disposable += authRepository.getState()
            .subscribe { state ->
                val certificate = state.certificate ?: return@subscribe iotRepository.disconnect()

                val config = IotConfig(
                    iotEndpoint = certificate.dataEndpoint,
                    clientId = certificate.thingName,
                    certificateId = certificate.certificateId,
                    region = Regions.EU_CENTRAL_1,
                    certificatePem = certificate.certificatePem,
                    privateKey = certificate.privateKey,
                )

                iotRepository.connect(config)
            }
    }

    val updates: LiveData<Unit> = generator
}

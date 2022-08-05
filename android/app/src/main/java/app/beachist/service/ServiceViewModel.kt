package app.beachist.service

import app.beachist.iot.repository.IotRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.beachist.iot_client.client.IotConfig
import app.beachist.auth.repository.AuthRepository
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
                    certificatePem = certificate.certificatePem,
                    privateKey = certificate.privateKey,
                )

                iotRepository.connect(config)
            }
    }

    val updates: LiveData<Unit> = generator
}

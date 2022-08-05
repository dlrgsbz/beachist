package app.beachist.provision.data

import app.beachist.auth.repository.AuthRepository
import app.beachist.auth.dto.Certificate
import app.beachist.provision.data.model.ProvisionResponse
import app.beachist.shared.async.Async
import io.reactivex.Observable

class ProvisionRepository(
    private val provisionUseCase: ProvisionUseCase,
    private val authRepository: AuthRepository,
) {
    fun provision(station: Int, password: String): Observable<Async<ProvisionResponse>> {
        // handle login
        return provisionUseCase(station, password)
            .doOnNext {
                when (it) {
                    is Async.Success -> {
                        val data = it.data
                        authRepository.updateCertificate(Certificate(
                            thingName = data.thingName,
                            certificateId = data.certificateId,
                            certificatePem = data.certificatePem,
                            publicKey = data.publicKey,
                            privateKey = data.privateKey,
                            dataEndpoint = data.dataEndpoint,
                            credentialsEndpoint = data.credentialsEndpoint,
                        ))
                    }
                    else -> {
                        // ignoring this on purpose
                    }
                }
            }
    }
}
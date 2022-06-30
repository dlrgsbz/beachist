package app.beachist.provision.data

import android.util.Base64
import app.beachist.provision.data.model.ProvisionRequest
import app.beachist.provision.data.model.ProvisionResponse
import app.beachist.shared.async.Async
import app.beachist.shared.async.toAsync
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

interface ProvisionUseCase : (Int, String) -> Observable<Async<ProvisionResponse>>

internal class ProvisionUseCaseImpl(private val provisionApi: ProvisionApi) : ProvisionUseCase {
    override fun invoke(station: Int, password: String): Observable<Async<ProvisionResponse>> {
        val basicAuth = generateAuthorizationHeader(password)
        val provisionRequest = ProvisionRequest(station)

        return provisionApi
            .provisionDevice(basicAuth, provisionRequest)
            .subscribeOn(Schedulers.io())
            .toAsync()
    }
}

private fun generateAuthorizationHeader(password: String): String {
    val credentials = "%s:%s".format("beachist", password)
    val encodedCredentials = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
    return "Basic $encodedCredentials"
}

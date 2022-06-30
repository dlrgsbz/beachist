package app.beachist.provision.data

import app.beachist.provision.data.model.ProvisionRequest
import app.beachist.provision.data.model.ProvisionResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ProvisionApi {
    @Headers("Content-Type: application/json")
    @POST("/")
    fun provisionDevice(
        @Header("Authorization") authorization: String,
        @Body body: ProvisionRequest
    ): Observable<ProvisionResponse>
}

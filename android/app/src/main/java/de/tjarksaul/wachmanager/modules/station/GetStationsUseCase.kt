package de.tjarksaul.wachmanager.modules.station


import de.tjarksaul.wachmanager.api.Async
import de.tjarksaul.wachmanager.api.WachmanagerService
import de.tjarksaul.wachmanager.api.toAsync
import de.tjarksaul.wachmanager.dtos.Station
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


interface GetStationsUseCase : () -> Observable<Async<List<Station>>>

internal class GetStationsUseCaseImpl(
    private val retrofit: Retrofit
) : GetStationsUseCase {
    override fun invoke(): Observable<Async<List<Station>>> {
        val api = retrofit.create(WachmanagerService::class.java)

        return api.getStationsRx()
            .subscribeOn(Schedulers.io())
            .retryWhen { errorStream ->
                errorStream.switchMap {
                    Observable.timer(RETRY_AFTER_MS, TimeUnit.MILLISECONDS)
                }
            }
            .toAsync()
    }

    companion object {
        private const val RETRY_AFTER_MS = 3000L
    }
}

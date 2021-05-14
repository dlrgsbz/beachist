package de.tjarksaul.wachmanager.ui.events

import de.tjarksaul.wachmanager.api.Async
import de.tjarksaul.wachmanager.api.WachmanagerService
import de.tjarksaul.wachmanager.api.mapData
import de.tjarksaul.wachmanager.api.toAsync
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


interface CreateEventUseCase : (String, PostEvent) -> Observable<Async<PostEvent>>

internal class CreateEventUsecaseImpl(
    private val retrofit: Retrofit
) : CreateEventUseCase {
    override fun invoke(stationId: String, info: PostEvent): Observable<Async<PostEvent>> {
        val api = retrofit.create(WachmanagerService::class.java)

        return api.createEvent(stationId, info)
            .subscribeOn(Schedulers.io())
            .retryWhen { errorStream ->
                errorStream.switchMap {
                    Observable.timer(RETRY_AFTER_MS, TimeUnit.MILLISECONDS)
                }
            }
            .toAsync()
            .mapData { info }
    }

    companion object {
        private const val RETRY_AFTER_MS = 3000L
    }
}
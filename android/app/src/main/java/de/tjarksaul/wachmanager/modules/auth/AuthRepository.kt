package de.tjarksaul.wachmanager.modules.auth

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class AuthRepository internal constructor(
    private val dao: AuthDao,
): CoroutineScope {
    private val disposable = CompositeDisposable()
    private val state: BehaviorSubject<State> = BehaviorSubject.create()

    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    init {
        state.onNext(State(certificate = null))

        disposable += dao.queryOne().subscribe {
            Timber.tag("AuthRepository").i("Thing name: ${it.thingName}")
            state.onNext(State(certificate = it))
        }
    }

    fun getState(): Observable<State> = state

    fun updateCertificate(certificate: Certificate) {
        launch(coroutineContext) {
            withContext(Dispatchers.IO) {
                dao.deleteAllExcept(certificate.thingName)
                dao.upsert(certificate)
            }
        }
    }
}

data class State(
    val certificate: Certificate? = null,
)
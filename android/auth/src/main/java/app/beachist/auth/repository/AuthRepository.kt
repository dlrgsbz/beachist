package app.beachist.auth.repository

import app.beachist.auth.database.AuthDao
import app.beachist.auth.database.DbCertificate
import app.beachist.auth.database.toDbCertificate
import app.beachist.auth.dto.Certificate
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
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
            state.onNext(State(certificate = it.toCertificate()))
        }
    }

    fun getState(): Observable<State> = state

    fun updateCertificate(certificate: Certificate) {
        launch(coroutineContext) {
            withContext(Dispatchers.IO) {
                dao.deleteAllExcept(certificate.thingName)
                dao.upsert(certificate.toDbCertificate())
            }
        }
    }
}

data class State(
    val certificate: Certificate? = null,
)
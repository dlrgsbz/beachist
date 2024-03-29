package app.beachist.auth.station

import app.beachist.auth.repository.AuthRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class StationNameProvider(
    authRepository: AuthRepository
): CoroutineScope {
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    private val disposable = CompositeDisposable()

    private val mutableStationNameFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    val stationNameFlow: Flow<String?> = mutableStationNameFlow

    init {
        disposable += authRepository.getState()
            .subscribe {
                launch {
                    mutableStationNameFlow.emit(it?.certificate?.thingName)
                }
            }
    }

    fun currentStationName(): String? = mutableStationNameFlow.value
}

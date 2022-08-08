package app.beachist.iot.ui

import app.beachist.iot.repository.ConnectionState
import app.beachist.iot.repository.IotRepository
import app.beachist.shared.base.BaseViewModel
import app.beachist.shared.base.ViewModelAction
import app.beachist.shared.base.ViewModelEffect
import app.beachist.shared.base.ViewModelState
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

internal class IotConnectionStateViewModel(private val iotRepository: IotRepository) :
    BaseViewModel<IotConnectionStateAction, IotConnectionStateState, IotConnectionStateEffect>(
        emptyState
    ) {
    companion object {
        private val emptyState = IotConnectionStateState()
    }

    override fun handleActions() {
        disposables += iotRepository.observeConnection().subscribe {
            Timber.tag("IotConnectionStateViewModel").d("state: $it")
            state.set {
                copy(connected = it == ConnectionState.Connected)
            }
        }
    }
}

internal sealed class IotConnectionStateAction : ViewModelAction()

internal sealed class IotConnectionStateEffect : ViewModelEffect()

internal data class IotConnectionStateState(val connected: Boolean = false) : ViewModelState

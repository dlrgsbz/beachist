package de.tjarksaul.wachmanager.iot

import de.tjarksaul.wachmanager.modules.base.BaseViewModel
import de.tjarksaul.wachmanager.modules.base.ViewModelAction
import de.tjarksaul.wachmanager.modules.base.ViewModelEffect
import de.tjarksaul.wachmanager.modules.base.ViewModelState
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

internal sealed class IotConnectionStateAction : ViewModelAction

internal sealed class IotConnectionStateEffect : ViewModelEffect

internal data class IotConnectionStateState(val connected: Boolean = false) : ViewModelState

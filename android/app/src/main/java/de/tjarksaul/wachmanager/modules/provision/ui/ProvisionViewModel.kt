package de.tjarksaul.wachmanager.modules.provision.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.api.Async
import de.tjarksaul.wachmanager.modules.provision.data.ProvisionRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class ProvisionViewModel(private val provisionRepository: ProvisionRepository) : ViewModel() {
    private val disposable = CompositeDisposable()

    private val _loginForm = MutableLiveData<ProvisionFormState>()
    val provisionFormState: LiveData<ProvisionFormState> = _loginForm

    private val _loginResult = MutableLiveData<ProvisionResult>()
    val provisionResult: LiveData<ProvisionResult> = _loginResult

    fun provision(stationNumber: String, password: String) {
        val number = stationNumber.toInt()

        disposable += provisionRepository.provision(number, password)
            .subscribe {
                when (it) {
                    is Async.Success -> {
                        Handler(Looper.getMainLooper()).post {
                            _loginResult.value = ProvisionResult(success = ProvisionDataView(it.data.thingName))
                        }
                    }
                    is Async.Failure -> {
                        Timber.tag("ProvisionViewModel").e(it.error)
                        Handler(Looper.getMainLooper()).post {
                            _loginResult.value = ProvisionResult(error = R.string.provision_failed)
                        }
                    }
                    is Async.Running -> {
                        // this is explicitly ignored
                    }
                }
            }
    }

    fun dataChanged(stationNumber: String, password: String) {
        if (!isStationValid(stationNumber)) {
            _loginForm.value = ProvisionFormState(usernameError = R.string.invalid_station_no)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = ProvisionFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = ProvisionFormState(isDataValid = true)
        }
    }

    private fun isStationValid(stationNumber: String): Boolean {
        val number = stationNumber.toIntOrNull() ?: return false
        return number > 0
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }
}
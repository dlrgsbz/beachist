package de.tjarksaul.wachmanager.iot

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.tjarksaul.wachmanager.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_iot_connection_state.*
import org.koin.core.component.KoinComponent
import timber.log.Timber

class IotConnectionStateFragment : Fragment(), KoinComponent {
    private val disposable = CompositeDisposable()

    private val viewModel: IotConnectionStateViewModel by viewModel()

    private val actions: PublishSubject<IotConnectionStateAction> = PublishSubject.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_iot_connection_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onConnectedChanged(false)

        setupBindings()
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposable += viewModel.stateOf { connected }
            .subscribe { onConnectedChanged(it) }
    }

    private fun onConnectedChanged(connected: Boolean) {
        Timber.tag("IotConnectionStateFragment").d("onConnectionChanged: $connected")
        disconnectedImage.visibility = if (connected) View.INVISIBLE else View.VISIBLE
    }
}

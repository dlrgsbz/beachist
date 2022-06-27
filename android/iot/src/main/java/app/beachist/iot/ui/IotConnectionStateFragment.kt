package app.beachist.iot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.beachist.iot.databinding.FragmentIotConnectionStateBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import timber.log.Timber

class IotConnectionStateFragment : Fragment(), KoinComponent {
    private val disposable = CompositeDisposable()

    private val viewModel: IotConnectionStateViewModel by viewModel()

    private val actions: PublishSubject<IotConnectionStateAction> = PublishSubject.create()

    private var _binding: FragmentIotConnectionStateBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentIotConnectionStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.disconnectedImage.visibility = if (connected) View.INVISIBLE else View.VISIBLE
    }
}

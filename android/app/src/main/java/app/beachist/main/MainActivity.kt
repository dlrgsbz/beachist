package app.beachist.main

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import app.beachist.R
import app.beachist.crew.ui.CrewNameFragment
import app.beachist.databinding.ActivityMainBinding
import app.beachist.debug.CrashRecorder
import app.beachist.provision.ui.ProvisionFragment
import app.beachist.service.BeachistService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.system.exitProcess

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), ServiceConnection {

    private val viewModel: MainViewModel by viewModel()
    private val disposable = CompositeDisposable()
    private val actions: PublishSubject<MainViewAction> = PublishSubject.create()
    private lateinit var binding: ActivityMainBinding

    private val crashRecorder: CrashRecorder by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, BeachistService::class.java)
        startService(intent)
        bindService(intent, this, Service.BIND_AUTO_CREATE)

        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setupView()
    }

    override fun onResume() {
        super.onResume()

        setupBindings()
    }

    private fun setupView() {
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_special_events
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.navView.visibility = View.VISIBLE
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposable += viewModel.stateOf { currentView }
            .doOnNext { Timber.tag("MainActivity").d("currentView: $it") }
            .subscribe { onViewChange(it) }
    }

    private fun onViewChange(view: MainViewCurrentView) {
        binding.navView.visibility = View.VISIBLE
        val fragment = (supportFragmentManager.findFragmentByTag(FragmentTag.crewInfo) as? DialogFragment)

        if (fragment == null ) {
            Timber.tag("MainActivity").w("No crew info dialog fragment found ð")
        }
        fragment?.dismiss()
        (supportFragmentManager.findFragmentByTag(FragmentTag.crewInfo) as? DialogFragment)?.dismiss()
        (supportFragmentManager.findFragmentByTag(FragmentTag.provision) as? DialogFragment)?.dismiss()

        when (view) {
            MainViewCurrentView.CrewInput -> setupCrewView()
            MainViewCurrentView.Provision -> setupProvisioningView()
            MainViewCurrentView.TabbedView -> goToStationView()
        }
    }

    private fun setupCrewView() {
        Timber.tag("MainActivity").d("setupCrewView")
        ensureTransition()
        binding.navView.visibility = View.GONE
        CrewNameFragment().show(supportFragmentManager, FragmentTag.crewInfo)
    }

    private fun setupProvisioningView() {
        Timber.tag("MainActivity").d("setupProvisioningView")
        ensureTransition()
        binding.navView.visibility = View.GONE
        ProvisionFragment().show(supportFragmentManager, FragmentTag.provision)
    }

    private fun ensureTransition() {
        if (supportFragmentManager.isDestroyed || supportFragmentManager.isStateSaved || isFinishing) {
            crashRecorder.log("Can't transition to other view because transition is impossible")
            crashRecorder.recordException(CantTransitionException())
            Timber.tag("MainActivity").i("Can't transition to other view because transition is impossible")
            exitProcess(0)
        }
    }

    private fun goToStationView() {
        Timber.tag("MainActivity").d("goToStationView")
        binding.navView.visibility = View.VISIBLE
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        // ignoring this on purpose
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        // ignoring this on purpose
    }
}

sealed class FragmentTag {
    companion object {
        const val provision = "provision"
        const val crewInfo = "crewInfo"
    }
}

class CantTransitionException(): Exception()

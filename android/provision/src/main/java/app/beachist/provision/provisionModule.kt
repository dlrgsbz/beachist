package app.beachist.provision

import com.google.gson.Gson
import app.beachist.provision.BuildConfig
import app.beachist.provision.data.ProvisionApiFactory
import app.beachist.provision.data.ProvisionRepository
import app.beachist.provision.data.ProvisionUseCase
import app.beachist.provision.data.ProvisionUseCaseImpl
import app.beachist.provision.ui.ProvisionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val provisionModule = module {
    viewModel {
        ProvisionViewModel(
            provisionRepository = get()
        )
    }
    factory { ProvisionRepository(get(), get()) }
    factory { ProvisionUseCaseImpl(get()) } bind ProvisionUseCase::class

    // creating a new Gson instance here on purpose
    factory { ProvisionApiFactory(Gson()).api(BuildConfig.PROVISIONING_URL) }
}

package de.tjarksaul.wachmanager.modules.provision

import com.google.gson.Gson
import de.tjarksaul.wachmanager.BuildConfig
import de.tjarksaul.wachmanager.modules.provision.data.ProvisionApiFactory
import de.tjarksaul.wachmanager.modules.provision.data.ProvisionRepository
import de.tjarksaul.wachmanager.modules.provision.data.ProvisionUseCase
import de.tjarksaul.wachmanager.modules.provision.data.ProvisionUseCaseImpl
import de.tjarksaul.wachmanager.modules.provision.ui.ProvisionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val provisionModule = module {
    viewModel {
        ProvisionViewModel(
            provisionRepository = get()
        )
    }
    factory { ProvisionRepository(get(), get()) }
    factory { ProvisionUseCaseImpl(get()) as ProvisionUseCase }
    factory { ProvisionApiFactory(Gson()).api(BuildConfig.PROVISIONING_URL) }
}

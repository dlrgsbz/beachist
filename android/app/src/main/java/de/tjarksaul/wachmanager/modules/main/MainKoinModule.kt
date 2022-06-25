package de.tjarksaul.wachmanager.modules.main

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@ExperimentalCoroutinesApi
val mainKoinModule = module {
    viewModel {
        MainViewModel(get(), get())
    }
}

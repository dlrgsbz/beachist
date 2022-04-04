package de.tjarksaul.wachmanager.modules.shared

import org.koin.dsl.module


val sharedModule = module {
    factory { AppVersionRepository(get()) }
}

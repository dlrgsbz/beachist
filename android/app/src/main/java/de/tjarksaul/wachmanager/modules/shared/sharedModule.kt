package de.tjarksaul.wachmanager.modules.shared

import org.koin.dsl.module.module

val sharedModule = module {
    factory { AppVersionRepository(get()) }
}

package de.tjarksaul.wachmanager.util

import org.koin.dsl.module

val utilModule = module {
    factory {
        DateFormatProvider()
    }
}
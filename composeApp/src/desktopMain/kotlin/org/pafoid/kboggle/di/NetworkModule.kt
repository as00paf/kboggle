package org.pafoid.kboggle.di

import org.koin.dsl.module
import org.pafoid.kboggle.services.SocketService

fun networkModule() = module {
    single { SocketService() }
}
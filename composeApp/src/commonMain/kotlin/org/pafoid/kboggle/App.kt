package org.pafoid.kboggle

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.pafoid.kboggle.services.GameService
import org.pafoid.kboggle.services.SocketService
import org.pafoid.kboggle.ui.Navigation
import org.pafoid.kboggle.viewmodels.GameScreenViewModel
import org.pafoid.kboggle.viewmodels.LoginScreenViewModel

@Composable
@Preview
fun App() {
    val navController = rememberNavController()

    val networkModule = module {
        singleOf(::SocketService)
    }

    val appModule = module {
        single { GameService(get()) }
        single { navController }
    }

    val vmModule = module {
        viewModelOf(::LoginScreenViewModel)
        viewModelOf(::GameScreenViewModel)
    }

    KoinApplication(application = { modules(networkModule, appModule, vmModule) }) {
        MaterialTheme {
            Navigation()
        }
    }
}
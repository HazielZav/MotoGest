package com.TallerJC.motogest.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.TallerJC.motogest.MotoGestApplication
import com.TallerJC.motogest.data.di.AppContainer
import com.TallerJC.motogest.ui.entregas.EntregasViewModel
import com.TallerJC.motogest.ui.entregas.PantallaEntregas
import com.TallerJC.motogest.ui.gerencia.GerenciaViewModel
import com.TallerJC.motogest.ui.gerencia.PantallaGerencia
import com.TallerJC.motogest.ui.navigation.Ruta
import com.TallerJC.motogest.ui.recepcion.PantallaRecepcion
import com.TallerJC.motogest.ui.recepcion.RecepcionViewModel
import com.TallerJC.motogest.ui.taller.PantallaTaller
import com.TallerJC.motogest.ui.taller.TallerViewModel

@Composable
fun MotoGestApp(
    appContainer: AppContainer = (LocalContext.current.applicationContext as MotoGestApplication).container,
    navController: NavHostController = rememberNavController()
) {
    val operacionesRepository = appContainer.operacionesRepository
    val gerenciaRepository = appContainer.gerenciaRepository

    // Lista de pantallas para el menú inferior
    val itemsNavegacion = listOf(
        Ruta.Recepcion,
        Ruta.Taller,
        Ruta.Entregas,
        Ruta.Gerencia
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val rutaActual = navBackStackEntry?.destination?.route

                itemsNavegacion.forEach { destino ->
                    NavigationBarItem(
                        icon = { Icon(destino.icono, contentDescription = stringResource(destino.tituloRes)) },
                        label = { Text(stringResource(destino.tituloRes)) },
                        selected = rutaActual == destino.ruta,
                        onClick = {
                            navController.navigate(destino.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Ruta.Recepcion.ruta,
            modifier = Modifier.padding(paddingValues)
        ) {

            // --- RECEPCIÓN ---
            composable(Ruta.Recepcion.ruta) {
                val recepcionViewModel: RecepcionViewModel = viewModel(
                    factory = RecepcionViewModel.provideFactory(operacionesRepository)
                )
                PantallaRecepcion(viewModel = recepcionViewModel)
            }

            // --- TALLER ---
            composable(Ruta.Taller.ruta) {
                val tallerViewModel: TallerViewModel = viewModel(
                    factory = TallerViewModel.provideFactory(operacionesRepository)
                )

                PantallaTaller(viewModel = tallerViewModel)
            }

            // --- ENTREGAS ---
            composable(Ruta.Entregas.ruta) {
                val entregasViewModel: EntregasViewModel = viewModel(
                    factory = EntregasViewModel.provideFactory(operacionesRepository)
                )
                PantallaEntregas(viewModel = entregasViewModel)
            }

            // --- GERENCIA ---
            composable(Ruta.Gerencia.ruta) {
                val gerenciaViewModel: GerenciaViewModel = viewModel(
                    factory = GerenciaViewModel.provideFactory(gerenciaRepository)
                )
                PantallaGerencia(viewModel = gerenciaViewModel)
            }
        }
    }
}

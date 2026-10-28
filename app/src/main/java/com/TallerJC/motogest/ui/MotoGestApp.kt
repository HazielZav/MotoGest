package com.TallerJC.motogest.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.TallerJC.motogest.data.local.AppDatabase
import com.TallerJC.motogest.data.repository.OperacionesRepository
import com.TallerJC.motogest.ui.gerencia.GerenciaViewModel
import com.TallerJC.motogest.ui.gerencia.PantallaGerencia
import com.TallerJC.motogest.ui.navigation.Ruta
import com.TallerJC.motogest.ui.recepcion.PantallaRecepcion
import com.TallerJC.motogest.ui.recepcion.RecepcionViewModel
import com.TallerJC.motogest.ui.taller.PantallaTaller
import com.TallerJC.motogest.ui.taller.TallerViewModel

@Composable
fun MotoGestApp(
    navController: NavHostController = rememberNavController()
) {
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
                // La ruta acutal es pintada de naranja
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val rutaActual = navBackStackEntry?.destination?.route

                itemsNavegacion.forEach { destino ->
                    NavigationBarItem(
                        icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                        label = { Text(destino.titulo) },
                        selected = rutaActual == destino.ruta,
                        onClick = {
                            navController.navigate(destino.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                // Restaura el estado de la pantalla si ya la habías visitado
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary, // Icono Blanco
                            selectedTextColor = MaterialTheme.colorScheme.primary, // Texto Naranja
                            indicatorColor = MaterialTheme.colorScheme.primary // Píldora Naranja de selección
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // Aquí conectamos el menú con las pantallas reales
        NavHost(
            navController = navController,
            startDestination = Ruta.Recepcion.ruta,
            modifier = Modifier.padding(paddingValues)
        ) {

            // --- RECEPCIÓN ---
            composable(Ruta.Recepcion.ruta) {
                val viewModel: RecepcionViewModel = viewModel()
                val estadoUi by viewModel.estadoUi.collectAsState()

                PantallaRecepcion(
                    estadoUi = estadoUi,
                    onNombreCambio = { viewModel.actualizarNombreCliente(it) },
                    onTelefonoCambio = { viewModel.actualizarTelefonoCliente(it) },
                    onMarcaCambio = { viewModel.actualizarMarcaMoto(it) },
                    onModeloCambio = { viewModel.actualizarModeloColorMoto(it) },
                    onServicioSeleccionado = { viewModel.seleccionarServicio(it) },
                    onNotasCambio = { viewModel.actualizarNotasFalla(it) },
                    onModalidadCambio = { viewModel.cambiarModalidadEntrega(it) },
                    onGuardarClic = { viewModel.guardarOrden() }
                )
            }

            // --- TALLER ---
            composable(Ruta.Taller.ruta) {
                //Text(text = "Pantalla del Taller", color = MaterialTheme.colorScheme.onBackground)
                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val operacionesRepository = remember { OperacionesRepository(database.workOrderDao()) }

                val tallerViewModel: TallerViewModel = viewModel(
                    factory = TallerViewModel.provideFactory(operacionesRepository)
                )

                PantallaTaller(viewModel = tallerViewModel)
            }

            // --- ENTREGAS ---
            composable(Ruta.Entregas.ruta) {
                Text(text = "Pantalla de Entregas", color = MaterialTheme.colorScheme.onBackground)
            }

            // --- GERENCIA ---
            composable(Ruta.Gerencia.ruta) {
                val viewModel: GerenciaViewModel = viewModel()
                val estadoUi by viewModel.estadoUi.collectAsStateWithLifecycle()

                PantallaGerencia(
                    estadoUi = estadoUi,
                    onPeriodoSeleccionado = { viewModel.cambiarPeriodo(it) },
                    onExcluirRefaccionesToggle = { viewModel.toggleExcluirRefacciones() },
                    onBuscarCambio = { viewModel.actualizarBusqueda(it) },
                    onRangoPersonalizadoSeleccionado = { inicio, fin ->
                        viewModel.establecerRangoPersonalizado(inicio, fin)
                    },
                    onCerrarDialogoFecha = { viewModel.cerrarDialogoFecha() },
                    onReintentar = { viewModel.cargarDatos() }
                )
            }
        }
    }
}

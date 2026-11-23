package com.TallerJC.motogest.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.TallerJC.motogest.MotoGestApplication
import com.TallerJC.motogest.R
import com.TallerJC.motogest.data.di.AppContainer
import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.ui.auth.AuthViewModel
import com.TallerJC.motogest.ui.auth.EstadoSesion
import com.TallerJC.motogest.ui.auth.PantallaLogin
import com.TallerJC.motogest.ui.auth.PantallaSplash
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
    appContainer: AppContainer = (LocalContext.current.applicationContext as MotoGestApplication).container
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(appContainer.authRepository)
    )
    val authState by authViewModel.estadoUi.collectAsStateWithLifecycle()

    when (val sesion = authState.estadoSesion) {
        is EstadoSesion.CargandoSplash -> PantallaSplash()
        is EstadoSesion.SinSesion -> PantallaLogin(
            estadoUi = authState,
            onIdentificadorCambio = authViewModel::actualizarIdentificador,
            onContrasenaCambio = authViewModel::actualizarContrasena,
            onIniciarSesion = authViewModel::iniciarSesion
        )
        is EstadoSesion.Autenticado -> key(sesion.usuario.id) {
            MotoGestContenidoAutenticado(
                usuario = sesion.usuario,
                appContainer = appContainer,
                onCerrarSesion = authViewModel::cerrarSesion
            )
        }
    }
}

@Composable
private fun MotoGestContenidoAutenticado(
    usuario: UserEntity,
    appContainer: AppContainer,
    onCerrarSesion: () -> Unit
) {
    val rutasPermitidas = remember(usuario.roles) {
        Ruta.obtenerRutasPermitidas(usuario.roles)
    }
    var mostrarDialogoSalir by remember { mutableStateOf(false) }

    if (rutasPermitidas.isEmpty()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockPerson,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.auth_sesion_usuario, usuario.fullName),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.auth_sin_permisos_mensaje),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCerrarSesion) {
                    Text(stringResource(R.string.auth_btn_cerrar_sesion))
                }
            }
        }
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    // Redirige si los permisos del usuario cambiaron mientras estaba en otro módulo
    LaunchedEffect(rutasPermitidas, rutaActual) {
        val rutasSet = rutasPermitidas.map { it.ruta }.toSet()
        if (rutaActual != null && rutaActual !in rutasSet) {
            navController.navigate(rutasPermitidas.first().ruta) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            BarraSesionUsuario(
                usuario = usuario,
                onSolicitarCerrarSesion = { mostrarDialogoSalir = true }
            )
        },
        bottomBar = {
            if (rutasPermitidas.size > 1) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    rutasPermitidas.forEach { destino ->
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
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = rutasPermitidas.first().ruta,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            if (Ruta.Recepcion in rutasPermitidas) {
                composable(Ruta.Recepcion.ruta) {
                    val recepcionViewModel: RecepcionViewModel = viewModel(
                        factory = RecepcionViewModel.provideFactory(
                            repository = appContainer.operacionesRepository,
                            authRepository = appContainer.authRepository
                        )
                    )
                    PantallaRecepcion(viewModel = recepcionViewModel)
                }
            }

            if (Ruta.Taller in rutasPermitidas) {
                composable(Ruta.Taller.ruta) {
                    val tallerViewModel: TallerViewModel = viewModel(
                        factory = TallerViewModel.provideFactory(
                            repository = appContainer.operacionesRepository,
                            authRepository = appContainer.authRepository
                        )
                    )
                    PantallaTaller(viewModel = tallerViewModel)
                }
            }

            if (Ruta.Entregas in rutasPermitidas) {
                composable(Ruta.Entregas.ruta) {
                    val entregasViewModel: EntregasViewModel = viewModel(
                        factory = EntregasViewModel.provideFactory(appContainer.operacionesRepository)
                    )
                    PantallaEntregas(viewModel = entregasViewModel)
                }
            }

            if (Ruta.Gerencia in rutasPermitidas) {
                composable(Ruta.Gerencia.ruta) {
                    val gerenciaViewModel: GerenciaViewModel = viewModel(
                        factory = GerenciaViewModel.provideFactory(appContainer.gerenciaRepository)
                    )
                    PantallaGerencia(viewModel = gerenciaViewModel)
                }
            }
        }
    }

    if (mostrarDialogoSalir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalir = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.auth_dialog_salir_titulo), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(stringResource(R.string.auth_dialog_salir_mensaje))
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoSalir = false
                        onCerrarSesion()
                    }
                ) {
                    Text(stringResource(R.string.auth_btn_cerrar_sesion), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSalir = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }
}

@Composable
private fun BarraSesionUsuario(
    usuario: UserEntity,
    onSolicitarCerrarSesion: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = stringResource(R.string.auth_sesion_usuario, usuario.fullName),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = usuario.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            TextButton(onClick = onSolicitarCerrarSesion) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.auth_btn_cerrar_sesion))
            }
        }
    }
}

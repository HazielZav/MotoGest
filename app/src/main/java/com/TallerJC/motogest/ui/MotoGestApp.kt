package com.TallerJC.motogest.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.TallerJC.motogest.MotoGestApplication
import com.TallerJC.motogest.R
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
import com.TallerJC.motogest.ui.util.LocalAdaptiveState
import com.TallerJC.motogest.ui.util.MotoGestAdaptiveState
import com.TallerJC.motogest.ui.util.MotoGestNavigationType

@Composable
fun MotoGestApp(
    appContainer: AppContainer = (LocalContext.current.applicationContext as MotoGestApplication).container,
    navController: NavHostController = rememberNavController(),
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    heightSizeClass: WindowHeightSizeClass = WindowHeightSizeClass.Medium
) {
    val adaptiveState = remember(widthSizeClass, heightSizeClass) {
        MotoGestAdaptiveState.fromWidthSizeClass(widthSizeClass, heightSizeClass)
    }

    val itemsNavegacion = remember {
        listOf(
            Ruta.Recepcion,
            Ruta.Taller,
            Ruta.Entregas,
            Ruta.Gerencia
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val navegarA: (Ruta) -> Unit = { destino ->
        navController.navigate(destino.ruta) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalAdaptiveState provides adaptiveState) {
        // Fondo único bajo la barra de estado y un solo statusBarsPadding() en la raíz:
        // evita sobreposición con notificaciones/reloj y evita doble margen en TopAppBar / NavigationRail
        // tanto en Android antiguo (API 26) como en dispositivos recientes edge-to-edge (API 35/36).
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                when (adaptiveState.navigationType) {
                    MotoGestNavigationType.BOTTOM_NAVIGATION -> {
                        Scaffold(
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            bottomBar = {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ) {
                                    itemsNavegacion.forEach { destino ->
                                        NavigationBarItem(
                                            icon = { Icon(destino.icono, contentDescription = stringResource(destino.tituloRes)) },
                                            label = {
                                                Text(
                                                    text = stringResource(destino.tituloRes),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            selected = rutaActual == destino.ruta,
                                            onClick = { navegarA(destino) },
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
                            MotoGestNavHost(
                                appContainer = appContainer,
                                navController = navController,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            )
                        }
                    }

                    MotoGestNavigationType.NAVIGATION_RAIL -> {
                        // Solo respetamos navigationBars horizontal (barra de 3 botones en landscape);
                        // no aplicamos displayCutout al contenedor completo para evitar franjas blancas
                        // laterales al girar teléfonos con cámara perforada.
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                        ) {
                            NavigationRail(
                                modifier = Modifier.widthIn(min = 88.dp),
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                windowInsets = WindowInsets.displayCutout.only(WindowInsetsSides.Start)
                                    .union(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
                                header = {
                                    Icon(
                                        imageVector = Icons.Default.TwoWheeler,
                                        contentDescription = stringResource(R.string.app_name),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(vertical = if (adaptiveState.isCompactHeight) 4.dp else 12.dp)
                                            .size(if (adaptiveState.isCompactHeight) 24.dp else 30.dp)
                                    )
                                }
                            ) {
                                if (!adaptiveState.isCompactHeight) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                itemsNavegacion.forEach { destino ->
                                    NavigationRailItem(
                                        selected = rutaActual == destino.ruta,
                                        onClick = { navegarA(destino) },
                                        icon = { Icon(destino.icono, contentDescription = stringResource(destino.tituloRes)) },
                                        label = {
                                            Text(
                                                text = stringResource(destino.tituloRes),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                            ) {
                                MotoGestNavHost(
                                    appContainer = appContainer,
                                    navController = navController,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    MotoGestNavigationType.PERMANENT_NAVIGATION_DRAWER -> {
                        PermanentNavigationDrawer(
                            drawerContent = {
                                PermanentDrawerSheet(
                                    modifier = Modifier.width(240.dp),
                                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                                    windowInsets = WindowInsets.displayCutout.only(WindowInsetsSides.Start)
                                        .union(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(horizontal = 12.dp, vertical = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TwoWheeler,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(30.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = stringResource(R.string.app_name),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = stringResource(R.string.nav_drawer_subtitulo),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        itemsNavegacion.forEach { destino ->
                                            NavigationDrawerItem(
                                                selected = rutaActual == destino.ruta,
                                                onClick = { navegarA(destino) },
                                                icon = { Icon(destino.icono, contentDescription = null) },
                                                label = { Text(stringResource(destino.tituloRes), fontWeight = FontWeight.SemiBold) },
                                                colors = NavigationDrawerItemDefaults.colors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                                    selectedTextColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .windowInsetsPadding(
                                        WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                                    )
                            ) {
                                MotoGestNavHost(
                                    appContainer = appContainer,
                                    navController = navController,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MotoGestNavHost(
    appContainer: AppContainer,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val operacionesRepository = appContainer.operacionesRepository
    val gerenciaRepository = appContainer.gerenciaRepository

    NavHost(
        navController = navController,
        startDestination = Ruta.Recepcion.ruta,
        modifier = modifier
    ) {
        // --- RECEPCIÓN ---
        composable(Ruta.Recepcion.ruta) {
            val recepcionViewModel: RecepcionViewModel = viewModel(
                factory = RecepcionViewModel.provideFactory(
                    repository = operacionesRepository,
                    mediaRepository = appContainer.mediaRepository,
                    imageStorage = appContainer.localImageStorage
                )
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

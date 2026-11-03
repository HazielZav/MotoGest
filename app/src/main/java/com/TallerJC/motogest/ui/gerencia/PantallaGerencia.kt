package com.TallerJC.motogest.ui.gerencia

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.TallerJC.motogest.R
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.ui.theme.MotoGestTheme
import com.TallerJC.motogest.ui.util.getLabelRes
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantallaGerencia(
    viewModel: GerenciaViewModel,
    modifier: Modifier = Modifier
) {
    val estadoUi by viewModel.estadoUi.collectAsStateWithLifecycle()
    PantallaGerencia(
        estadoUi = estadoUi,
        onPestanaCambio = viewModel::cambiarPestana,
        onPeriodoSeleccionado = viewModel::cambiarPeriodo,
        onExcluirRefaccionesToggle = viewModel::toggleExcluirRefacciones,
        onBuscarCambio = viewModel::actualizarBusqueda,
        onBuscarDirectorioCambio = viewModel::actualizarBusquedaDirectorio,
        onRangoPersonalizadoSeleccionado = viewModel::establecerRangoPersonalizado,
        onCerrarDialogoFecha = viewModel::cerrarDialogoFecha,
        onReintentar = viewModel::cargarDatos,
        onCargarDemo = viewModel::cargarDatosDemo,
        onAbrirDetalleOrden = viewModel::abrirDetalleOrden,
        onCerrarDetalleOrden = viewModel::cerrarDetalleOrden,
        onVerHistorialMoto = viewModel::verHistorialMoto,
        onCerrarHistorialMoto = viewModel::cerrarModalHistorialMoto,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGerencia(
    estadoUi: GerenciaUiState,
    onPeriodoSeleccionado: (PeriodoFiltro) -> Unit,
    onExcluirRefaccionesToggle: () -> Unit,
    onBuscarCambio: (String) -> Unit,
    onRangoPersonalizadoSeleccionado: (Long, Long) -> Unit,
    onCerrarDialogoFecha: () -> Unit,
    onReintentar: () -> Unit,
    onCargarDemo: () -> Unit = {},
    onPestanaCambio: (Int) -> Unit = {},
    onBuscarDirectorioCambio: (String) -> Unit = {},
    onAbrirDetalleOrden: (WorkOrderWithDetails) -> Unit = {},
    onCerrarDetalleOrden: () -> Unit = {},
    onVerHistorialMoto: (String) -> Unit = {},
    onCerrarHistorialMoto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }
    val shortDateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.gerencia_titulo),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = stringResource(R.string.gerencia_subtitulo),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (estadoUi.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (estadoUi.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = estadoUi.error,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        Button(onClick = onReintentar) {
                            Text(stringResource(R.string.btn_reintentar))
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // PESTAÑAS: 0 = Métricas y Finanzas, 1 = Directorio e Historial
                PrimaryTabRow(
                    selectedTabIndex = estadoUi.pestanaSeleccionada,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = estadoUi.pestanaSeleccionada == 0,
                        onClick = { onPestanaCambio(0) },
                        text = { Text(stringResource(R.string.gerencia_tab_metricas), fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) }
                    )
                    Tab(
                        selected = estadoUi.pestanaSeleccionada == 1,
                        onClick = { onPestanaCambio(1) },
                        text = { Text(stringResource(R.string.gerencia_tab_directorio), fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.TwoWheeler, contentDescription = null) }
                    )
                }

                if (estadoUi.pestanaSeleccionada == 0) {
                    // TAB 0: MÉTRICAS Y FINANZAS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // FILTROS TEMPORALES
                        item {
                            Text(
                                text = stringResource(R.string.gerencia_periodo_titulo),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(PeriodoFiltro.values()) { periodo ->
                                    FilterChip(
                                        selected = estadoUi.periodoSeleccionado == periodo,
                                        onClick = { onPeriodoSeleccionado(periodo) },
                                        label = { Text(stringResource(periodo.getLabelRes()), fontWeight = FontWeight.SemiBold) },
                                        leadingIcon = if (periodo == PeriodoFiltro.PERSONALIZADO) {
                                            {
                                                Icon(
                                                    Icons.Filled.CalendarMonth,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.height(48.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }

                            if (estadoUi.periodoSeleccionado == PeriodoFiltro.PERSONALIZADO &&
                                estadoUi.fechaInicioPersonalizada != null &&
                                estadoUi.fechaFinPersonalizada != null
                            ) {
                                val inicioStr = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(estadoUi.fechaInicioPersonalizada),
                                    ZoneId.systemDefault()
                                ).format(shortDateFormatter)

                                val finStr = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(estadoUi.fechaFinPersonalizada),
                                    ZoneId.systemDefault()
                                ).format(shortDateFormatter)

                                Spacer(modifier = Modifier.height(8.dp))
                                SuggestionChip(
                                    onClick = { onPeriodoSeleccionado(PeriodoFiltro.PERSONALIZADO) },
                                    label = { Text(stringResource(R.string.gerencia_rango_activo, inicioStr, finStr)) }
                                )
                            }
                        }

                        // UTILIDAD NETA (EXCLUIR REFACCIONES)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        Text(
                                            text = stringResource(R.string.gerencia_toggle_excluir_refacciones),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (estadoUi.excluirRefacciones)
                                                stringResource(R.string.gerencia_toggle_desc_ganancia_real)
                                            else
                                                stringResource(R.string.gerencia_toggle_desc_ingreso_bruto),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = estadoUi.excluirRefacciones,
                                        onCheckedChange = { onExcluirRefaccionesToggle() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }

                        // TARJETAS DE RESUMEN FINANCIERO
                        item {
                            Text(
                                text = stringResource(R.string.gerencia_metricas_titulo),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = if (estadoUi.excluirRefacciones)
                                            stringResource(R.string.gerencia_kpi_ganancia_neta)
                                        else
                                            stringResource(R.string.gerencia_kpi_ingresos_brutos),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currencyFormatter.format(estadoUi.ingresosTotales),
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    if (estadoUi.excluirRefacciones && estadoUi.totalRefaccionesCosto > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.gerencia_kpi_refacciones_descontadas,
                                                currencyFormatter.format(estadoUi.totalRefaccionesCosto)
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = stringResource(R.string.gerencia_kpi_motos_atendidas_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${estadoUi.motosAtendidas}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = stringResource(R.string.gerencia_kpi_motos_subtitulo),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = stringResource(R.string.gerencia_kpi_ticket_promedio_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = currencyFormatter.format(estadoUi.ticketPromedio),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = stringResource(R.string.gerencia_kpi_ticket_subtitulo),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // DISTRIBUCIÓN OPERATIVA DE SERVICIOS
                        item {
                            Text(
                                text = stringResource(R.string.gerencia_distribucion_titulo),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = stringResource(R.string.gerencia_distribucion_subtitulo),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val serviciosConDatos = estadoUi.distribucionServicios.filter { it.cantidad > 0 }
                                    if (serviciosConDatos.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.gerencia_distribucion_sin_datos),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        serviciosConDatos.forEach { itemServicio ->
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(itemServicio.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text("${itemServicio.cantidad} (${(itemServicio.porcentaje * 100).toInt()}%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                }
                                                LinearProgressIndicator(
                                                    progress = { itemServicio.porcentaje },
                                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // HISTORIAL DE ÓRDENES FINALIZADAS
                        item {
                            Text(
                                text = stringResource(R.string.gerencia_historial_titulo),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = estadoUi.textoBusqueda,
                                onValueChange = onBuscarCambio,
                                placeholder = { Text(stringResource(R.string.gerencia_buscar_placeholder)) },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = {
                                    if (estadoUi.textoBusqueda.isNotEmpty()) {
                                        IconButton(onClick = { onBuscarCambio("") }) {
                                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.btn_limpiar))
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // ESTADO VACÍO O LISTA DE ÓRDENES
                        if (estadoUi.ordenesFiltradas.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Filled.AssignmentTurnedIn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(52.dp))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (estadoUi.textoBusqueda.isNotEmpty())
                                                stringResource(R.string.gerencia_vacio_busqueda, estadoUi.textoBusqueda)
                                            else
                                                stringResource(R.string.gerencia_vacio_titulo),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.gerencia_vacio_sugerencia),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (estadoUi.textoBusqueda.isEmpty()) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = onCargarDemo,
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Filled.CloudDownload, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(stringResource(R.string.gerencia_btn_cargar_demo), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            items(estadoUi.ordenesFiltradas, key = { it.order.id }) { item ->
                                val fechaStr = remember(item.order.deliveredAt) {
                                    val fechaEpoch = item.order.deliveredAt ?: item.order.completedAt ?: item.order.createdAt
                                    LocalDateTime.ofInstant(Instant.ofEpochMilli(fechaEpoch), ZoneId.systemDefault()).format(dateFormatter)
                                }

                                Card(
                                    onClick = { onAbrirDetalleOrden(item) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(item.order.folio, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                            Text(fechaStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                            Text(item.client.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Filled.TwoWheeler, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                            val placa = item.motorcycle.plate?.let { " ($it)" } ?: ""
                                            Text("${item.motorcycle.brand} ${item.motorcycle.model}$placa", fontSize = 13.sp)
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val servEnum = runCatching { ServiceType.valueOf(item.order.serviceType) }.getOrNull()
                                            val servLabel = servEnum?.let { stringResource(it.getLabelRes()) } ?: item.order.serviceType
                                            Text(servLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = currencyFormatter.format(item.order.totalAmount),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: DIRECTORIO E HISTORIAL COMPLETO
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = estadoUi.textoBusquedaDirectorio,
                            onValueChange = onBuscarDirectorioCambio,
                            placeholder = { Text(stringResource(R.string.gerencia_directorio_buscar)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (estadoUi.ordenesDirectorioFiltradas.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.gerencia_directorio_vacio),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(estadoUi.ordenesDirectorioFiltradas, key = { it.order.id }) { ordenDetalle ->
                                    Card(
                                        onClick = { onAbrirDetalleOrden(ordenDetalle) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(ordenDetalle.order.folio, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Badge { Text(ordenDetalle.order.status) }
                                            }
                                            Text(
                                                text = "${ordenDetalle.client.name} • Tel: ${ordenDetalle.client.phone}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Moto: ${ordenDetalle.motorcycle.brand} ${ordenDetalle.motorcycle.model} (${ordenDetalle.motorcycle.color})",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Cobro: ${currencyFormatter.format(ordenDetalle.order.totalAmount)}",
                                                    fontWeight = FontWeight.Bold
                                                )
                                                TextButton(onClick = { onVerHistorialMoto(ordenDetalle.motorcycle.id) }) {
                                                    Text(stringResource(R.string.recepcion_btn_ver_historial), fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // MODAL DE DETALLE DE ORDEN
        if (estadoUi.mostrarModalDetalleOrden && estadoUi.ordenSeleccionadaDetalle != null) {
            val orden = estadoUi.ordenSeleccionadaDetalle.order
            val cliente = estadoUi.ordenSeleccionadaDetalle.client
            val moto = estadoUi.ordenSeleccionadaDetalle.motorcycle
            val workItems = estadoUi.ordenSeleccionadaDetalle.workItems
            val spareParts = estadoUi.ordenSeleccionadaDetalle.spareParts

            AlertDialog(
                onDismissRequest = onCerrarDetalleOrden,
                icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
                title = { Text(stringResource(R.string.gerencia_dialog_detalle_titulo, orden.folio), fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cliente: ${cliente.name}", fontWeight = FontWeight.Bold)
                        Text("Teléfono: ${cliente.phone} • ${cliente.address ?: stringResource(R.string.entregas_sin_direccion)}")
                        HorizontalDivider()
                        Text("Vehículo: ${moto.brand} ${moto.model} (${moto.color})", fontWeight = FontWeight.Bold)
                        moto.plate?.let { Text("Placa: $it") }
                        moto.vin?.let { Text("VIN: $it") }
                        HorizontalDivider()
                        Text("Mano de Obra:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (workItems.isEmpty()) {
                            Text("Total M.O.: ${currencyFormatter.format(orden.totalLaborCost)}", style = MaterialTheme.typography.bodySmall)
                        } else {
                            workItems.forEach { item ->
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("• ${item.description}", style = MaterialTheme.typography.bodySmall)
                                    Text(currencyFormatter.format(item.cost), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Text("Refacciones:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (spareParts.isEmpty()) {
                            Text("Total Refacciones: ${currencyFormatter.format(orden.totalPartsCost)}", style = MaterialTheme.typography.bodySmall)
                        } else {
                            spareParts.forEach { part ->
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("• ${part.partName}", style = MaterialTheme.typography.bodySmall)
                                    Text(currencyFormatter.format(part.cost), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        if (orden.deliveryCost > 0.0) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Envío a Domicilio:", fontWeight = FontWeight.Bold)
                                Text(currencyFormatter.format(orden.deliveryCost), fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("TOTAL FACTURADO:", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text(currencyFormatter.format(orden.totalAmount), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        orden.recommendations?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("Recomendaciones Mecánicas:", fontWeight = FontWeight.Bold)
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = onCerrarDetalleOrden) {
                        Text(stringResource(R.string.btn_cerrar))
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        onCerrarDetalleOrden()
                        onVerHistorialMoto(moto.id)
                    }) {
                        Text(stringResource(R.string.recepcion_btn_ver_historial))
                    }
                }
            )
        }

        // MODAL DE HISTORIAL COMPLETO DE MOTO
        if (estadoUi.mostrarModalHistorialMoto) {
            AlertDialog(
                onDismissRequest = onCerrarHistorialMoto,
                icon = { Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
                title = { Text("Historial de la Unidad", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (estadoUi.historialMotoSeleccionada.isEmpty()) {
                            Text(stringResource(R.string.recepcion_historial_sin_ordenes), style = MaterialTheme.typography.bodyMedium)
                        } else {
                            estadoUi.historialMotoSeleccionada.forEach { detalle ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text(detalle.order.folio, fontWeight = FontWeight.Bold)
                                            Badge { Text(detalle.order.status) }
                                        }
                                        Text("Motivo: ${detalle.order.reportedFailure}", style = MaterialTheme.typography.bodySmall)
                                        Text("Total: ${currencyFormatter.format(detalle.order.totalAmount)}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                        detalle.order.recommendations?.let {
                                            Text("Recs: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = onCerrarHistorialMoto) {
                        Text(stringResource(R.string.btn_cerrar))
                    }
                }
            )
        }
    }
}

@Preview(
    name = "Gerencia - Modo Oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PantallaGerenciaPreview() {
    val clienteEjemplo = ClientEntity(
        id = "c1",
        name = "Juan Carlos Pérez",
        phone = "4451234567"
    )
    val motoEjemplo = MotorcycleEntity(
        id = "m1",
        clientId = "c1",
        brand = "Italika",
        model = "FT150",
        color = "Rojo",
        photoUrl = "",
        plate = "ABC-12"
    )
    val ordenEjemplo = WorkOrderEntity(
        id = "o1",
        folio = "ORD-001",
        clientId = "c1",
        motorcycleId = "m1",
        receivedByUserId = "u1",
        status = OrderStatus.ENTREGADA.name,
        serviceType = ServiceType.SERVICIO_GENERAL.name,
        reportedFailure = "Mantenimiento preventivo",
        deliveryType = DeliveryType.TALLER.name,
        totalLaborCost = 350.0,
        totalPartsCost = 450.0,
        totalAmount = 800.0
    )

    val estadoEjemplo = GerenciaUiState(
        periodoSeleccionado = PeriodoFiltro.ESTE_MES,
        excluirRefacciones = true,
        ingresosTotales = 7850.0,
        totalRefaccionesCosto = 3450.0,
        motosAtendidas = 8,
        ticketPromedio = 981.25,
        distribucionServicios = listOf(
            DistribucionServicio(ServiceType.SERVICIO_GENERAL, "Servicio General / Afinación", 4, 0.5f),
            DistribucionServicio(ServiceType.FRENOS, "Sistema de Frenos", 2, 0.25f),
            DistribucionServicio(ServiceType.REPARACION, "Reparación General", 1, 0.125f),
            DistribucionServicio(ServiceType.AJUSTE_MOTOR, "Ajuste de Motor", 1, 0.125f),
            DistribucionServicio(ServiceType.OTRO, "Otros Servicios", 0, 0f)
        ),
        ordenesFiltradas = listOf(
            WorkOrderWithDetails(
                order = ordenEjemplo,
                client = clienteEjemplo,
                motorcycle = motoEjemplo,
                workItems = emptyList(),
                spareParts = emptyList()
            )
        )
    )

    MotoGestTheme {
        PantallaGerencia(
            estadoUi = estadoEjemplo,
            onPeriodoSeleccionado = {},
            onExcluirRefaccionesToggle = {},
            onBuscarCambio = {},
            onRangoPersonalizadoSeleccionado = { _, _ -> },
            onCerrarDialogoFecha = {},
            onReintentar = {},
            onCargarDemo = {}
        )
    }
}

package com.TallerJC.motogest.ui.gerencia

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.ui.theme.MotoGestTheme
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                            text = "Panel de Gerencia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Control operativo y financiero del taller",
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
                            Text("Reintentar")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // FILTROS TEMPORALES
                item {
                    Text(
                        text = "Periodo de Consulta",
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
                                label = { Text(periodo.titulo, fontWeight = FontWeight.SemiBold) },
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

                    // Indicador de rango si es personalizado
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
                            label = { Text("Rango activo: $inicioStr al $finStr (Tocar para cambiar)") }
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
                                    text = "Excluir costo de refacciones",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (estadoUi.excluirRefacciones)
                                        "Mostrando ganancia real (Mano de obra + envíos)"
                                    else
                                        "Mostrando ingreso bruto (Incluye refacciones)",
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
                        text = "Métricas Financieras",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tarjeta Principal: Ingresos Totales / Ganancia Neta
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MonetizationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = if (estadoUi.excluirRefacciones)
                                        "Ganancia Neta del Taller"
                                    else
                                        "Ingresos Totales Brutos",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currencyFormatter.format(estadoUi.ingresosTotales),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (estadoUi.excluirRefacciones) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Refacciones descontadas: ${currencyFormatter.format(estadoUi.totalRefaccionesCosto)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // tarjetas: Motos Atendidas y Ticket Promedio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Motos Atendidas
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.TwoWheeler,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Entregadas",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${estadoUi.motosAtendidas}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "motos concluidas",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Ticket Promedio
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Ticket Promedio",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currencyFormatter.format(estadoUi.ticketPromedio),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "por servicio",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // DISTRIBUCIÓN OPERATIVA DE SERVICIOS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Distribución de Servicios",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Servicios realizados en el periodo actual",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            if (estadoUi.motosAtendidas == 0) {
                                Text(
                                    text = "Sin datos de distribución para este periodo.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                estadoUi.distribucionServicios.forEach { servicio ->
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = servicio.nombre,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Medium
                                            )
                                            val porcentajeTexto = (servicio.porcentaje * 100).toInt()
                                            Text(
                                                text = "${servicio.cantidad} ($porcentajeTexto%)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { servicio.porcentaje },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // CONSULTA HISTÓRICA RÁPIDA
                item {
                    Text(
                        text = "Historial de Órdenes Finalizadas",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = estadoUi.textoBusqueda,
                        onValueChange = onBuscarCambio,
                        label = { Text("Buscar por cliente, moto o placas...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (estadoUi.textoBusqueda.isNotEmpty()) {
                                IconButton(onClick = { onBuscarCambio("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Limpiar búsqueda"
                                    )
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(52.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (estadoUi.textoBusqueda.isNotEmpty())
                                        "No se encontraron órdenes con \"${estadoUi.textoBusqueda}\""
                                    else
                                        "No hay órdenes registradas en este periodo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Intenta seleccionar otro periodo o cambiar el criterio de búsqueda.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    items(estadoUi.ordenesFiltradas, key = { it.order.id }) { item ->
                        val fechaStr = remember(item.order.deliveredAt) {
                            val fechaEpoch = item.order.deliveredAt
                                ?: item.order.completedAt
                                ?: item.order.createdAt
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(fechaEpoch),
                                ZoneId.systemDefault()
                            ).format(dateFormatter)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = item.order.folio,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = fechaStr,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Cliente
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = item.client.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Motocicleta
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.TwoWheeler,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    val placaTexto = item.motorcycle.plate?.let { " • Placa: $it" } ?: ""
                                    Text(
                                        text = "${item.motorcycle.brand} ${item.motorcycle.model}$placaTexto",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                )

                                // Desglose por orden
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.order.serviceType.replace("_", " "),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Column(horizontalAlignment = Alignment.End) {
                                        val montoMostrar = if (estadoUi.excluirRefacciones) {
                                            maxOf(0.0, item.order.totalAmount - item.order.totalPartsCost)
                                        } else {
                                            item.order.totalAmount
                                        }

                                        Text(
                                            text = currencyFormatter.format(montoMostrar),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        if (estadoUi.excluirRefacciones && item.order.totalPartsCost > 0) {
                                            Text(
                                                text = "-${currencyFormatter.format(item.order.totalPartsCost)} refacc.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
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

    // SELECTOR DE FECHA
    if (estadoUi.mostrarDialogoFecha) {
        val dateRangePickerState = rememberDateRangePickerState()

        DatePickerDialog(
            onDismissRequest = onCerrarDialogoFecha,
            confirmButton = {
                TextButton(
                    onClick = {
                        val inicio = dateRangePickerState.selectedStartDateMillis
                        val fin = dateRangePickerState.selectedEndDateMillis ?: inicio
                        if (inicio != null && fin != null) {
                            onRangoPersonalizadoSeleccionado(inicio, fin)
                        } else {
                            onCerrarDialogoFecha()
                        }
                    }
                ) {
                    Text("Aplicar Rango")
                }
            },
            dismissButton = {
                TextButton(onClick = onCerrarDialogoFecha) {
                    Text("Cancelar")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = "Seleccionar Rango de Consulta",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(
    name = "Pantalla Gerencia - Modo Oscuro",
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
            onReintentar = {}
        )
    }
}

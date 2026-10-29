package com.TallerJC.motogest.ui.recepcion

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.ServiceType

@Composable
fun PantallaRecepcion(
    viewModel: RecepcionViewModel
) {
    val estadoUi by viewModel.estadoUi.collectAsStateWithLifecycle()

    PantallaRecepcionContenido(
        estadoUi = estadoUi,
        onNombreCambio = viewModel::actualizarNombreCliente,
        onTelefonoCambio = viewModel::actualizarTelefonoCliente,
        onDireccionCambio = viewModel::actualizarDireccionCliente,
        onMarcaCambio = viewModel::actualizarMarcaMoto,
        onModeloCambio = viewModel::actualizarModeloMoto,
        onColorCambio = viewModel::actualizarColorMoto,
        onPlacaCambio = viewModel::actualizarPlaca,
        onAnioCambio = viewModel::actualizarAnio,
        onVinCambio = viewModel::actualizarVin,
        onServicioSeleccionado = viewModel::seleccionarServicio,
        onFallaCambio = viewModel::actualizarFallaReportada,
        onAgregarFoto = viewModel::agregarFotoEvidenciaSimulada,
        onNotasDanosCambio = viewModel::actualizarNotasDanos,
        onModalidadCambio = viewModel::cambiarModalidadEntrega,
        onDescartarAlertaDomicilio = viewModel::descartarAlertaDomicilio,
        onGuardarClic = viewModel::guardarOrden
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecepcionContenido(
    estadoUi: RecepcionUiState,
    onNombreCambio: (String) -> Unit,
    onTelefonoCambio: (String) -> Unit,
    onDireccionCambio: (String) -> Unit,
    onMarcaCambio: (String) -> Unit,
    onModeloCambio: (String) -> Unit,
    onColorCambio: (String) -> Unit,
    onPlacaCambio: (String) -> Unit,
    onAnioCambio: (String) -> Unit,
    onVinCambio: (String) -> Unit,
    onServicioSeleccionado: (ServiceType) -> Unit,
    onFallaCambio: (String) -> Unit,
    onAgregarFoto: () -> Unit,
    onNotasDanosCambio: (String) -> Unit,
    onModalidadCambio: (DeliveryType) -> Unit,
    onDescartarAlertaDomicilio: () -> Unit,
    onGuardarClic: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recepción de Unidad", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mensajes de feedback
            estadoUi.mensajeError?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            if (estadoUi.ordenGuardadaConExito) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        text = "✓ Orden registrada y enviada al Tablero del Taller.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. CLIENTE (US-02)
            Text("1. Datos del Cliente", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = estadoUi.nombreCliente,
                onValueChange = onNombreCambio,
                label = { Text("Nombre completo*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = estadoUi.telefonoCliente,
                    onValueChange = onTelefonoCambio,
                    label = { Text("Teléfono (10 dígitos)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = estadoUi.direccionCliente,
                    onValueChange = onDireccionCambio,
                    label = { Text("Dirección (Opcional)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // 2. MOTOCICLETA (US-02)
            Text("2. Identificación de Motocicleta", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = estadoUi.marcaMoto,
                    onValueChange = onMarcaCambio,
                    label = { Text("Marca*") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = estadoUi.modeloMoto,
                    onValueChange = onModeloCambio,
                    label = { Text("Modelo*") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = estadoUi.colorMoto,
                    onValueChange = onColorCambio,
                    label = { Text("Color*") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = estadoUi.placaOpcional,
                    onValueChange = onPlacaCambio,
                    label = { Text("Placa (Opc)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = estadoUi.anioOpcional,
                    onValueChange = onAnioCambio,
                    label = { Text("Año (Opc)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = estadoUi.vinOpcional,
                    onValueChange = onVinCambio,
                    label = { Text("VIN / Serie") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // 3. MOTIVO DE INGRESO Y DICTADO POR VOZ (US-03 y RF-05)
            Text("3. Orden de Servicio", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ServiceType.values()) { tipo ->
                    FilterChip(
                        selected = estadoUi.servicioSeleccionado == tipo,
                        onClick = { onServicioSeleccionado(tipo) },
                        label = { Text(tipo.name.replace("_", " ")) },
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            OutlinedTextField(
                value = estadoUi.fallaReportada,
                onValueChange = onFallaCambio,
                label = { Text("Problema reportado por el cliente*") },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                trailingIcon = {
                    IconButton(onClick = { /* Lanzar SpeechRecognizer */ }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = "Dictar por voz", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            // 4. EVIDENCIA FOTOGRÁFICA DE DAÑOS (RF-07: Máximo 5 fotos)
            Text(
                text = "Evidencia de Daños Previos (${estadoUi.fotosEvidencia.size}/5)",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (estadoUi.fotosEvidencia.size < 5) {
                    item {
                        Card(
                            onClick = onAgregarFoto,
                            modifier = Modifier.size(90.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Capturar Foto")
                            }
                        }
                    }
                }
                items(estadoUi.fotosEvidencia.size) { index ->
                    Card(
                        modifier = Modifier.size(90.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Foto #${index + 1}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // RF-07: Solo se habilita si se tomó al menos 1 foto
            if (estadoUi.fotosEvidencia.isNotEmpty()) {
                OutlinedTextField(
                    value = estadoUi.notasDanosPrevios,
                    onValueChange = onNotasDanosCambio,
                    label = { Text("Descripción de golpes o rayones previos (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 5. MODALIDAD DE ENTREGA (RF-08)
            Text("Modalidad de Entrega", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = estadoUi.modalidadEntrega == DeliveryType.TALLER,
                    onClick = { onModalidadCambio(DeliveryType.TALLER) }
                )
                Text("Recoger en Taller")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = estadoUi.modalidadEntrega == DeliveryType.DOMICILIO,
                    onClick = { onModalidadCambio(DeliveryType.DOMICILIO) }
                )
                Text("Entrega a Domicilio")
            }

            Button(
                onClick = onGuardarClic,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("ABRIR ORDEN DE TRABAJO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ALERTA MODAL RF-08: Entrega a domicilio sin dirección
        if (estadoUi.mostrarAlertaSinDomicilio) {
            var direccionTemporal by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onDescartarAlertaDomicilio,
                title = { Text("Cliente sin domicilio registrado") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Seleccionaste Entrega a Domicilio, pero el cliente no tiene dirección. ¿Deseas capturarla ahora?")
                        OutlinedTextField(
                            value = direccionTemporal,
                            onValueChange = { direccionTemporal = it },
                            label = { Text("Calle, número y colonia") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        onDireccionCambio(direccionTemporal)
                        onDescartarAlertaDomicilio()
                    }) {
                        Text("Guardar Dirección")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDescartarAlertaDomicilio) {
                        Text("Continuar sin dirección")
                    }
                }
            )
        }
    }
}

@Preview(
    name = "Recepción Completa - Modo Oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PantallaRecepcionPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        PantallaRecepcionContenido(
            estadoUi = RecepcionUiState(
                nombreCliente = "Kevin Haziel Elías",
                telefonoCliente = "4451234567",
                marcaMoto = "Hero",
                modeloMoto = "Ignitor 125",
                colorMoto = "Rojo",
                fotosEvidencia = listOf("foto1.jpg")
            ),
            onNombreCambio = {},
            onTelefonoCambio = {},
            onDireccionCambio = {},
            onMarcaCambio = {},
            onModeloCambio = {},
            onColorCambio = {},
            onPlacaCambio = {},
            onAnioCambio = {},
            onVinCambio = {},
            onServicioSeleccionado = {},
            onFallaCambio = {},
            onAgregarFoto = {},
            onNotasDanosCambio = {},
            onModalidadCambio = {},
            onDescartarAlertaDomicilio = {},
            onGuardarClic = {}
        )
    }
}
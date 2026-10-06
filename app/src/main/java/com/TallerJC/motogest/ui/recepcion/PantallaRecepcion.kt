package com.TallerJC.motogest.ui.recepcion

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TallerJC.motogest.ui.theme.MotoGestTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecepcion(
    estadoUi: RecepcionUiState,
    onNombreCambio: (String) -> Unit,
    onTelefonoCambio: (String) -> Unit,
    onMarcaCambio: (String) -> Unit,
    onModeloCambio: (String) -> Unit,
    onServicioSeleccionado: (String) -> Unit,
    onNotasCambio: (String) -> Unit,
    onModalidadCambio: (String) -> Unit,
    onGuardarClic: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recepción de Motocicleta", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // DATOS DEL CLIENTE
            Text("Datos del Cliente", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = estadoUi.nombreCliente,
                onValueChange = onNombreCambio,
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = estadoUi.telefonoCliente,
                onValueChange = onTelefonoCambio,
                label = { Text("Teléfono (WhatsApp)*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // 2. DATOS DE LA MOTO Y SERVICIO[cite: 10, 11]
            Text("Motocicleta y Servicio", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = estadoUi.marcaMoto,
                    onValueChange = onMarcaCambio,
                    label = { Text("Marca") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = estadoUi.modeloColorMoto,
                    onValueChange = onModeloCambio,
                    label = { Text("Modelo/Color") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Chips dinámicos desde el estado[cite: 11, 24]
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(estadoUi.opcionesServicio) { opcion ->
                    FilterChip(
                        selected = estadoUi.servicioSeleccionado == opcion,
                        onClick = { onServicioSeleccionado(opcion) },
                        label = { Text(opcion) },
                        modifier = Modifier.height(48.dp) // RNF-01: Accesibilidad 48dp[cite: 8, 34]
                    )
                }
            }

            // Dictado por voz (RF-05)[cite: 6, 33]
            OutlinedTextField(
                value = estadoUi.notasDañosYFalla,
                onValueChange = onNotasCambio,
                label = { Text("Notas de falla o daños previos...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                trailingIcon = {
                    IconButton(
                        onClick = { /* Pendiente: Lanzar intent nativo de voz */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Dictar por voz",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            // 3. EVIDENCIA FOTOGRÁFICA[cite: 6, 11, 33]
            Text("Evidencia Inicial (Max 5)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(
                        onClick = { /* Pendiente: FileProvider Camera */ },
                        modifier = Modifier.size(100.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.AddAPhoto, contentDescription = "Tomar Foto", tint = Color.Gray)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // 4. MODALIDAD DE ENTREGA[cite: 6, 12, 24]
            Text("Modalidad de Entrega", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = estadoUi.modalidadEntrega == "Taller",
                    onClick = { onModalidadCambio("Taller") }
                )
                Text("Recoger en taller")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = estadoUi.modalidadEntrega == "Domicilio",
                    onClick = { onModalidadCambio("Domicilio") }
                )
                Text("A domicilio")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. BOTON GUARDAR
            Button(
                onClick = onGuardarClic,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("GENERAR ORDEN DE SERVICIO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(
    name = "Pantalla Recepción - Modo Oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PantallaRecepcionPreview() {
    MotoGestTheme {
        PantallaRecepcion(
            estadoUi = RecepcionUiState(
                nombreCliente = "Kevin Haziel Elías",
                telefonoCliente = "445 123 4567",
                marcaMoto = "Hero",
                modeloColorMoto = "Ignitor 125 / Rojo",
                servicioSeleccionado = "Servicio"
            ),
            // Pasamos funciones vacías (lambdas) porque en el Preview no hay lógica
            onNombreCambio = {},
            onTelefonoCambio = {},
            onMarcaCambio = {},
            onModeloCambio = {},
            onServicioSeleccionado = {},
            onNotasCambio = {},
            onModalidadCambio = {},
            onGuardarClic = {}
        )
    }
}

package com.TallerJC.motogest.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Ruta(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
) {
    object Recepcion : Ruta(
        "recepcion",
        "Recepción",
        Icons.Filled.AppRegistration
    )

    object Taller : Ruta(
        "taller",
        "Taller",
        Icons.Filled.Build
    )

    object Entregas : Ruta(
        "entregas",
        "Entregas",
        Icons.Filled.TwoWheeler
    )

    object Gerencia : Ruta(
        "gerencia",
        "Gerencia",
        Icons.Filled.BarChart
    )
}
package com.TallerJC.motogest.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector
import com.TallerJC.motogest.R

sealed class Ruta(
    val ruta: String,
    val titulo: String,
    @param:StringRes val tituloRes: Int,
    val icono: ImageVector
) {
    object Recepcion : Ruta(
        "recepcion",
        "Recepción",
        R.string.nav_recepcion,
        Icons.Filled.AppRegistration
    )

    object Taller : Ruta(
        "taller",
        "Taller",
        R.string.nav_taller,
        Icons.Filled.Build
    )

    object Entregas : Ruta(
        "entregas",
        "Entregas",
        R.string.nav_entregas,
        Icons.Filled.TwoWheeler
    )

    object Gerencia : Ruta(
        "gerencia",
        "Gerencia",
        R.string.nav_gerencia,
        Icons.Filled.BarChart
    )
}

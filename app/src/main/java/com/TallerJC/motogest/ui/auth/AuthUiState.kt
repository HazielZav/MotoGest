package com.TallerJC.motogest.ui.auth

import com.TallerJC.motogest.data.local.entity.UserEntity

sealed interface EstadoSesion {
    data object CargandoSplash : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Autenticado(val usuario: UserEntity) : EstadoSesion
}

data class AuthUiState(
    val estadoSesion: EstadoSesion = EstadoSesion.CargandoSplash,
    val identificador: String = "",
    val contrasena: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

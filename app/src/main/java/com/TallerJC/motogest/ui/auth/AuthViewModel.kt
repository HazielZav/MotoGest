package com.TallerJC.motogest.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.TallerJC.motogest.data.repository.AuthRepository
import com.TallerJC.motogest.data.repository.AuthResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val splashDurationMs: Long = 1_000L,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(AuthUiState())
    val estadoUi: StateFlow<AuthUiState> = _estadoUi.asStateFlow()

    init {
        inicializar()
    }

    private fun inicializar() {
        viewModelScope.launch(dispatcher) {
            val initDeferred = async { authRepository.inicializarSesionYAdmin() }
            if (splashDurationMs > 0L) {
                delay(splashDurationMs)
            }
            initDeferred.await()

            authRepository.usuarioActual.collect { usuario ->
                _estadoUi.update { actual ->
                    if (usuario != null) {
                        actual.copy(
                            estadoSesion = EstadoSesion.Autenticado(usuario),
                            contrasena = "",
                            isLoading = false,
                            error = null
                        )
                    } else {
                        actual.copy(
                            estadoSesion = EstadoSesion.SinSesion,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun actualizarIdentificador(valor: String) {
        _estadoUi.update { it.copy(identificador = valor, error = null) }
    }

    fun actualizarContrasena(valor: String) {
        _estadoUi.update { it.copy(contrasena = valor, error = null) }
    }

    fun iniciarSesion() {
        val actual = _estadoUi.value
        if (actual.isLoading) return

        viewModelScope.launch(dispatcher) {
            _estadoUi.update { it.copy(isLoading = true, error = null) }
            when (val resultado = authRepository.iniciarSesion(actual.identificador, actual.contrasena)) {
                is AuthResult.Exito -> {
                    _estadoUi.update {
                        it.copy(
                            estadoSesion = EstadoSesion.Autenticado(resultado.usuario),
                            identificador = "",
                            contrasena = "",
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is AuthResult.Error -> {
                    _estadoUi.update {
                        it.copy(
                            isLoading = false,
                            error = resultado.mensaje
                        )
                    }
                }
            }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _estadoUi.update {
            AuthUiState(estadoSesion = EstadoSesion.SinSesion)
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            splashDurationMs: Long = 1_000L,
            dispatcher: CoroutineDispatcher = Dispatchers.Main
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository, splashDurationMs, dispatcher) as T
                }
            }
    }
}

package com.TallerJC.motogest.ui.auth

import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.data.repository.AuthRepository
import com.TallerJC.motogest.data.repository.AuthResult
import com.TallerJC.motogest.domain.model.enums.UserRole
import com.TallerJC.motogest.ui.navigation.Ruta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthViewModelTest {

    private class FakeAuthRepository(
        private val usuarios: List<UserEntity>,
        initialUser: UserEntity? = null
    ) : AuthRepository {
        private val _usuarioActual = MutableStateFlow(initialUser)
        override val usuarioActual: Flow<UserEntity?> = _usuarioActual

        override suspend fun inicializarSesionYAdmin(): UserEntity? = _usuarioActual.value

        override suspend fun iniciarSesion(identificador: String, contrasena: String): AuthResult {
            val user = usuarios.find {
                (it.email.equals(identificador.trim(), ignoreCase = true) ||
                    it.fullName.equals(identificador.trim(), ignoreCase = true)) &&
                    contrasena == it.password
            } ?: return AuthResult.Error("Credenciales inválidas")

            _usuarioActual.value = user
            return AuthResult.Exito(user)
        }

        override fun cerrarSesion() {
            _usuarioActual.value = null
        }

        override fun obtenerIdUsuarioActual(): String? = _usuarioActual.value?.id
    }

    @Test
    fun testFlujoLoginYCerrarSesion() = runBlocking {
        val admin = UserEntity(
            id = "admin-motogest",
            fullName = "admin",
            email = "admin@gmail.com",
            roles = UserRole.entries.map { it.name },
            isActive = true,
            password = "admin123"
        )
        val viewModel = AuthViewModel(FakeAuthRepository(listOf(admin)), splashDurationMs = 0L, dispatcher = Dispatchers.Unconfined)

        assertTrue(viewModel.estadoUi.value.estadoSesion is EstadoSesion.SinSesion)

        viewModel.actualizarIdentificador("admin@gmail.com")
        viewModel.actualizarContrasena("wrong")
        viewModel.iniciarSesion()
        assertTrue(viewModel.estadoUi.value.estadoSesion is EstadoSesion.SinSesion)
        assertEquals("Credenciales inválidas", viewModel.estadoUi.value.error)

        viewModel.actualizarContrasena("admin123")
        viewModel.iniciarSesion()
        val autenticado = viewModel.estadoUi.value.estadoSesion as? EstadoSesion.Autenticado
        assertEquals("admin", autenticado?.usuario?.fullName)
        assertNull(viewModel.estadoUi.value.error)

        viewModel.cerrarSesion()
        assertTrue(viewModel.estadoUi.value.estadoSesion is EstadoSesion.SinSesion)
    }

    @Test
    fun testPermisosDeNavegacionPorRol() {
        val rutasAdmin = Ruta.obtenerRutasPermitidas(UserRole.entries.map { it.name })
        assertEquals(listOf(Ruta.Recepcion, Ruta.Taller, Ruta.Entregas, Ruta.Gerencia), rutasAdmin)

        val rutasMecanico = Ruta.obtenerRutasPermitidas(listOf(UserRole.MECANICO.name))
        assertEquals(listOf(Ruta.Taller), rutasMecanico)

        val rutasRepartidor = Ruta.obtenerRutasPermitidas(listOf(UserRole.REPARTIDOR.name))
        assertEquals(listOf(Ruta.Entregas), rutasRepartidor)

        val rutasVacias = Ruta.obtenerRutasPermitidas(emptyList())
        assertTrue(rutasVacias.isEmpty())
    }
}

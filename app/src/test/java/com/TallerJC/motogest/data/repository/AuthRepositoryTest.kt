package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.dao.UserDao
import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.data.local.session.SessionManager
import com.TallerJC.motogest.data.security.PasswordHasher
import com.TallerJC.motogest.domain.model.enums.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    private class FakeUserDao : UserDao {
        private val users = mutableListOf<UserEntity>()
        private val flow = MutableStateFlow<List<UserEntity>>(emptyList())

        private fun emit() {
            flow.value = users.toList()
        }

        override fun getAllUsers(): Flow<List<UserEntity>> = flow

        override suspend fun getUserById(userId: String): UserEntity? = users.find { it.id == userId }

        override fun observeUserById(userId: String): Flow<UserEntity?> =
            flow.map { list -> list.find { it.id == userId } }

        override suspend fun getUserByEmail(email: String): UserEntity? =
            users.find { it.email.equals(email, ignoreCase = true) }

        override suspend fun getUserByFullName(fullName: String): UserEntity? =
            users.find { it.fullName.equals(fullName, ignoreCase = true) }

        override suspend fun insertUser(user: UserEntity) {
            users.removeAll { it.id == user.id }
            users.add(user)
            emit()
        }

        override suspend fun insertUsers(users: List<UserEntity>) {
            users.forEach { insertUser(it) }
        }

        override suspend fun updateUser(user: UserEntity) = insertUser(user)

        override suspend fun deleteUserById(userId: String) {
            users.removeAll { it.id == userId }
            emit()
        }

        override suspend fun countUsers(): Int = users.size
    }

    private class FakeSessionManager(initialUserId: String? = null) : SessionManager {
        var userId: String? = initialUserId
        override fun guardarSesion(userId: String) { this.userId = userId }
        override fun obtenerUsuarioId(): String? = userId
        override fun limpiarSesion() { userId = null }
    }

    @Test
    fun testInicializarAdminYLoginPorCorreoOUsuario() = runBlocking {
        val userDao = FakeUserDao()
        val sessionManager = FakeSessionManager()
        val repo = AuthRepositoryImpl(userDao, sessionManager)

        val sesionInicial = repo.inicializarSesionYAdmin()
        assertNull(sesionInicial)

        val admin = userDao.getUserByEmail(AuthRepositoryImpl.ADMIN_EMAIL)
        assertNotNull(admin)
        assertEquals(AuthRepositoryImpl.ADMIN_NAME, admin?.fullName)
        assertEquals(UserRole.entries.map { it.name }.toSet(), admin?.roles?.toSet())

        // Login con correo
        val resCorreo = repo.iniciarSesion("admin@gmail.com", AuthRepositoryImpl.ADMIN_DEFAULT_PASSWORD)
        assertTrue(resCorreo is AuthResult.Exito)
        assertEquals(admin?.id, sessionManager.obtenerUsuarioId())
        assertEquals(admin?.id, repo.usuarioActual.first()?.id)

        repo.cerrarSesion()
        assertNull(sessionManager.obtenerUsuarioId())
        assertNull(repo.usuarioActual.first())

        // Login con nombre de usuario y contraseña incorrecta
        val resFallido = repo.iniciarSesion("admin", "incorrecta")
        assertTrue(resFallido is AuthResult.Error)

        val resNombre = repo.iniciarSesion("admin", AuthRepositoryImpl.ADMIN_DEFAULT_PASSWORD)
        assertTrue(resNombre is AuthResult.Exito)
    }

    @Test
    fun testPersistenciaSesionYBloqueoCuentaInactiva() = runBlocking {
        val userDao = FakeUserDao()
        val empleado = UserEntity(
            id = "u_mec",
            fullName = "Pedro Mecánico",
            email = "pedro@motogest.com",
            roles = listOf(UserRole.MECANICO.name),
            isActive = true,
            password = PasswordHasher.hash("clave123")
        )
        userDao.insertUser(empleado)

        // Simula reabrir app con sesión previa guardada
        val sessionManager = FakeSessionManager("u_mec")
        val repo = AuthRepositoryImpl(userDao, sessionManager)
        val restaurado = repo.inicializarSesionYAdmin()
        assertEquals("u_mec", restaurado?.id)

        // Si el gerente desactiva la cuenta, limpia sesión y bloquea acceso
        userDao.insertUser(empleado.copy(isActive = false))
        assertNull(repo.usuarioActual.first())
        val intento = repo.iniciarSesion("pedro@motogest.com", "clave123")
        assertTrue(intento is AuthResult.Error)
    }
}

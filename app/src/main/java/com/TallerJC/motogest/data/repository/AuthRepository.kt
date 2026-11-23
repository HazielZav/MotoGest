package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.dao.UserDao
import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.data.local.session.SessionManager
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSource
import com.TallerJC.motogest.data.remote.mapper.toDto
import com.TallerJC.motogest.data.remote.mapper.toEntity
import com.TallerJC.motogest.data.security.PasswordHasher
import com.TallerJC.motogest.domain.model.enums.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

sealed interface AuthResult {
    data class Exito(val usuario: UserEntity) : AuthResult
    data class Error(val mensaje: String) : AuthResult
}

interface AuthRepository {
    val usuarioActual: Flow<UserEntity?>
    suspend fun inicializarSesionYAdmin(): UserEntity?
    suspend fun iniciarSesion(identificador: String, contrasena: String): AuthResult
    fun cerrarSesion()
    fun obtenerIdUsuarioActual(): String?
}

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val remoteDataSource: RemoteWorkOrderDataSource? = null
) : AuthRepository {

    private val _usuarioIdActivo = MutableStateFlow(sessionManager.obtenerUsuarioId())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val usuarioActual: Flow<UserEntity?> = _usuarioIdActivo.flatMapLatest { id ->
        if (id == null) {
            flowOf(null)
        } else {
            userDao.observeUserById(id).map { user ->
                if (user != null && user.isActive) user else null
            }
        }
    }

    override suspend fun inicializarSesionYAdmin(): UserEntity? {
        sincronizarUsuariosFaltantes()
        asegurarUsuarioAdmin()

        val idGuardado = sessionManager.obtenerUsuarioId() ?: return null
        val usuarioGuardado = userDao.getUserById(idGuardado)
        if (usuarioGuardado != null && usuarioGuardado.isActive) {
            _usuarioIdActivo.value = usuarioGuardado.id
            return usuarioGuardado
        }

        cerrarSesion()
        return null
    }

    override suspend fun iniciarSesion(identificador: String, contrasena: String): AuthResult {
        val query = identificador.trim()
        if (query.isBlank() || contrasena.isBlank()) {
            return AuthResult.Error("Ingresa tu usuario o correo y tu contraseña.")
        }

        var usuario = buscarUsuarioPorIdentificador(query)
        if (usuario == null) {
            sincronizarUsuariosFaltantes()
            usuario = buscarUsuarioPorIdentificador(query)
        }

        if (usuario == null) {
            return AuthResult.Error("Usuario o contraseña incorrectos.")
        }
        if (!usuario.isActive) {
            return AuthResult.Error("Tu cuenta está desactivada. Contacta al gerente.")
        }
        if (usuario.password.isBlank()) {
            return AuthResult.Error("Este usuario no tiene contraseña asignada. Solicítala al gerente.")
        }
        if (!PasswordHasher.matches(contrasena, usuario.password)) {
            return AuthResult.Error("Usuario o contraseña incorrectos.")
        }

        sessionManager.guardarSesion(usuario.id)
        _usuarioIdActivo.value = usuario.id
        return AuthResult.Exito(usuario)
    }

    override fun cerrarSesion() {
        sessionManager.limpiarSesion()
        _usuarioIdActivo.value = null
    }

    override fun obtenerIdUsuarioActual(): String? {
        return _usuarioIdActivo.value ?: sessionManager.obtenerUsuarioId()
    }

    private suspend fun buscarUsuarioPorIdentificador(query: String): UserEntity? {
        return userDao.getUserByEmail(query) ?: userDao.getUserByFullName(query)
    }

    private suspend fun sincronizarUsuariosFaltantes() {
        val remote = remoteDataSource ?: return
        try {
            val remotos = remote.fetchUsers()
            val nuevos = remotos.filter { userDao.getUserById(it.id) == null }
            if (nuevos.isNotEmpty()) {
                userDao.insertUsers(nuevos.map { it.toEntity() })
            }
        } catch (_: Exception) {
        }
    }

    private suspend fun asegurarUsuarioAdmin() {
        val todosLosRoles = UserRole.entries.map { it.name }
        val adminExistente = userDao.getUserByEmail(ADMIN_EMAIL)

        val adminFinal = if (adminExistente == null) {
            UserEntity(
                id = ADMIN_ID,
                fullName = ADMIN_NAME,
                email = ADMIN_EMAIL,
                roles = todosLosRoles,
                isActive = true,
                password = PasswordHasher.hash(ADMIN_DEFAULT_PASSWORD)
            )
        } else {
            val requiereActualizacion = !adminExistente.isActive ||
                adminExistente.roles.toSet() != todosLosRoles.toSet() ||
                adminExistente.password.isBlank()
            if (!requiereActualizacion) return

            adminExistente.copy(
                roles = todosLosRoles,
                isActive = true,
                password = adminExistente.password.ifBlank { PasswordHasher.hash(ADMIN_DEFAULT_PASSWORD) }
            )
        }

        userDao.insertUser(adminFinal)
        remoteDataSource?.uploadUser(adminFinal.toDto())
    }

    companion object {
        const val ADMIN_ID = "admin-motogest"
        const val ADMIN_NAME = "admin"
        const val ADMIN_EMAIL = "admin@gmail.com"
        const val ADMIN_DEFAULT_PASSWORD = "admin123"
    }
}

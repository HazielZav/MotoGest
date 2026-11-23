package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.dao.UserDao
import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSource
import com.TallerJC.motogest.data.remote.dto.ClientDto
import com.TallerJC.motogest.data.remote.dto.MotorcycleDto
import com.TallerJC.motogest.data.remote.dto.UserDto
import com.TallerJC.motogest.data.remote.dto.WorkOrderDto
import com.TallerJC.motogest.domain.model.enums.UserRole
import com.TallerJC.motogest.fake.FakeWorkOrderDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GerenciaRepositoryTest {

    private class FakeUserDao : UserDao {
        private val users = mutableListOf<UserEntity>()
        private val flow = MutableStateFlow<List<UserEntity>>(emptyList())

        private fun emit() {
            flow.value = users.sortedWith(compareByDescending<UserEntity> { it.isActive }.thenBy { it.fullName })
        }

        override fun getAllUsers(): Flow<List<UserEntity>> = flow

        override suspend fun getUserById(userId: String): UserEntity? = users.find { it.id == userId }

        override suspend fun getUserByEmail(email: String): UserEntity? =
            users.find { it.email.equals(email, ignoreCase = true) }

        override suspend fun insertUser(user: UserEntity) {
            val idx = users.indexOfFirst { it.id == user.id }
            if (idx != -1) users[idx] = user else users.add(user)
            emit()
        }

        override suspend fun insertUsers(users: List<UserEntity>) {
            users.forEach { insertUser(it) }
        }

        override suspend fun updateUser(user: UserEntity) {
            insertUser(user)
        }

        override suspend fun deleteUserById(userId: String) {
            users.removeAll { it.id == userId }
            emit()
        }

        override suspend fun countUsers(): Int = users.size
    }

    private class FakeRemoteDataSource(
        private val remoteUsers: MutableList<UserDto> = mutableListOf()
    ) : RemoteWorkOrderDataSource {
        override suspend fun fetchOrders(): List<WorkOrderDto> = emptyList()
        override suspend fun uploadOrder(order: WorkOrderDto): Boolean = true
        override suspend fun deleteOrder(orderId: String): Boolean = true
        override suspend fun fetchClients(): List<ClientDto> = emptyList()
        override suspend fun uploadClient(client: ClientDto): Boolean = true
        override suspend fun fetchMotorcycles(): List<MotorcycleDto> = emptyList()
        override suspend fun uploadMotorcycle(moto: MotorcycleDto): Boolean = true
        override suspend fun fetchUsers(): List<UserDto> = remoteUsers.toList()
        override suspend fun uploadUser(user: UserDto): Boolean {
            val idx = remoteUsers.indexOfFirst { it.id == user.id }
            if (idx != -1) remoteUsers[idx] = user else remoteUsers.add(user)
            return true
        }
        override suspend fun deleteUser(userId: String): Boolean {
            remoteUsers.removeAll { it.id == userId }
            return true
        }
    }

    @Test
    fun testGuardarModificarEliminarYSincronizarEmpleados() = runBlocking {
        val remote = FakeRemoteDataSource(
            mutableListOf(
                UserDto("u_remoto", "María Gerente", "maria@motogest.com", listOf(UserRole.GERENTE.name), true)
            )
        )
        val repo = GerenciaRepositoryImpl(FakeWorkOrderDao(), FakeUserDao(), remote)

        // Sincronizar desde la nube
        assertTrue(repo.sincronizarEmpleados())
        assertEquals(1, repo.obtenerEmpleados().first().size)

        // Guardar nuevo empleado
        val nuevo = UserEntity("u_local", "Jorge Mecánico", "jorge@motogest.com", listOf(UserRole.MECANICO.name), true)
        repo.guardarEmpleado(nuevo)
        assertEquals(2, repo.obtenerEmpleados().first().size)
        assertEquals(2, remote.fetchUsers().size)

        // Modificar roles
        repo.guardarEmpleado(nuevo.copy(roles = listOf(UserRole.MECANICO.name, UserRole.REPARTIDOR.name)))
        val actualizado = repo.obtenerEmpleados().first().first { it.id == "u_local" }
        assertEquals(2, actualizado.roles.size)

        // Eliminar empleado
        repo.eliminarEmpleado("u_local")
        assertEquals(1, repo.obtenerEmpleados().first().size)
        assertEquals(1, remote.fetchUsers().size)
    }
}

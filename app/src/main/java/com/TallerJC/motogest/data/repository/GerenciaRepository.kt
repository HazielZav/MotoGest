package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.dao.UserDao
import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSource
import com.TallerJC.motogest.data.remote.mapper.toDto
import com.TallerJC.motogest.data.remote.mapper.toEntity
import com.TallerJC.motogest.data.security.PasswordHasher
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import kotlinx.coroutines.flow.Flow

interface GerenciaRepository {
    fun obtenerOrdenesEntregadas(): Flow<List<WorkOrderWithDetails>>
    fun obtenerTodasLasOrdenes(): Flow<List<WorkOrderWithDetails>>
    fun obtenerHistorialMoto(motorcycleId: String): Flow<List<WorkOrderWithDetails>>
    fun obtenerHistorialCliente(clientId: String): Flow<List<WorkOrderWithDetails>>
    fun obtenerEmpleados(): Flow<List<UserEntity>>
    suspend fun guardarEmpleado(empleado: UserEntity, nuevaContrasena: String? = null)
    suspend fun eliminarEmpleado(userId: String)
    suspend fun sincronizarEmpleados(): Boolean
}

class GerenciaRepositoryImpl(
    private val workOrderDao: WorkOrderDao,
    private val userDao: UserDao,
    private val remoteDataSource: RemoteWorkOrderDataSource? = null
) : GerenciaRepository {

    override fun obtenerOrdenesEntregadas(): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getOrdersByStatusWithDetails(OrderStatus.ENTREGADA.name)
    }

    override fun obtenerTodasLasOrdenes(): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getAllOrdersWithDetails()
    }

    override fun obtenerHistorialMoto(motorcycleId: String): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getOrdersByMotorcycleId(motorcycleId)
    }

    override fun obtenerHistorialCliente(clientId: String): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getOrdersByClientId(clientId)
    }

    override fun obtenerEmpleados(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    override suspend fun guardarEmpleado(empleado: UserEntity, nuevaContrasena: String?) {
        val passwordFinal = if (!nuevaContrasena.isNullOrBlank()) {
            PasswordHasher.hash(nuevaContrasena)
        } else {
            empleado.password
        }
        val empleadoFinal = empleado.copy(password = passwordFinal)
        userDao.insertUser(empleadoFinal)
        remoteDataSource?.uploadUser(empleadoFinal.toDto())
    }

    override suspend fun eliminarEmpleado(userId: String) {
        userDao.deleteUserById(userId)
        remoteDataSource?.deleteUser(userId)
    }

    override suspend fun sincronizarEmpleados(): Boolean {
        val remote = remoteDataSource ?: return true
        return try {
            val remotos = remote.fetchUsers()
            if (remotos.isNotEmpty()) {
                userDao.insertUsers(remotos.map { it.toEntity() })
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}

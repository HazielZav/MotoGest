package com.TallerJC.motogest.data.remote.datasource

import com.TallerJC.motogest.data.remote.api.MotoGestApiService
import com.TallerJC.motogest.data.remote.dto.ClientDto
import com.TallerJC.motogest.data.remote.dto.MotorcycleDto
import com.TallerJC.motogest.data.remote.dto.WorkOrderDto

interface RemoteWorkOrderDataSource {
    suspend fun fetchOrders(): List<WorkOrderDto>
    suspend fun uploadOrder(order: WorkOrderDto): Boolean
    suspend fun deleteOrder(orderId: String): Boolean

    suspend fun fetchClients(): List<ClientDto>
    suspend fun uploadClient(client: ClientDto): Boolean

    suspend fun fetchMotorcycles(): List<MotorcycleDto>
    suspend fun uploadMotorcycle(moto: MotorcycleDto): Boolean
}

class RemoteWorkOrderDataSourceImpl(
    private val apiService: MotoGestApiService
) : RemoteWorkOrderDataSource {

    override suspend fun fetchOrders(): List<WorkOrderDto> {
        return try {
            val response = apiService.getOrders()
            if (response.isSuccessful) {
                response.body()?.values?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun uploadOrder(order: WorkOrderDto): Boolean {
        return try {
            val response = apiService.upsertOrder(order.id, order)
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun deleteOrder(orderId: String): Boolean {
        return try {
            val response = apiService.deleteOrder(orderId)
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun fetchClients(): List<ClientDto> {
        return try {
            val response = apiService.getClients()
            if (response.isSuccessful) {
                response.body()?.values?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun uploadClient(client: ClientDto): Boolean {
        return try {
            val response = apiService.upsertClient(client.id, client)
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun fetchMotorcycles(): List<MotorcycleDto> {
        return try {
            val response = apiService.getMotorcycles()
            if (response.isSuccessful) {
                response.body()?.values?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun uploadMotorcycle(moto: MotorcycleDto): Boolean {
        return try {
            val response = apiService.upsertMotorcycle(moto.id, moto)
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }
}

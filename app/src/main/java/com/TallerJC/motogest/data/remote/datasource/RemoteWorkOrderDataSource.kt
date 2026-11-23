package com.TallerJC.motogest.data.remote.datasource

import com.TallerJC.motogest.data.remote.api.MotoGestApiService
import com.TallerJC.motogest.data.remote.dto.ClientDto
import com.TallerJC.motogest.data.remote.dto.MotorcycleDto
import com.TallerJC.motogest.data.remote.dto.WorkOrderDto
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

interface RemoteWorkOrderDataSource {
    suspend fun fetchOrders(): Result<List<WorkOrderDto>>
    suspend fun uploadOrder(order: WorkOrderDto): Boolean
    suspend fun deleteOrder(orderId: String): Boolean

    suspend fun fetchClients(): Result<List<ClientDto>>
    suspend fun uploadClient(client: ClientDto): Boolean

    suspend fun fetchMotorcycles(): Result<List<MotorcycleDto>>
    suspend fun uploadMotorcycle(moto: MotorcycleDto): Boolean
}

class RemoteWorkOrderDataSourceImpl(
    private val apiService: MotoGestApiService
) : RemoteWorkOrderDataSource {

    override suspend fun fetchOrders(): Result<List<WorkOrderDto>> =
        fetchCollection { apiService.getOrders() }

    override suspend fun uploadOrder(order: WorkOrderDto): Boolean =
        executeWrite { apiService.upsertOrder(order.id, order) }

    override suspend fun deleteOrder(orderId: String): Boolean =
        executeWrite { apiService.deleteOrder(orderId) }

    override suspend fun fetchClients(): Result<List<ClientDto>> =
        fetchCollection { apiService.getClients() }

    override suspend fun uploadClient(client: ClientDto): Boolean =
        executeWrite { apiService.upsertClient(client.id, client) }

    override suspend fun fetchMotorcycles(): Result<List<MotorcycleDto>> =
        fetchCollection { apiService.getMotorcycles() }

    override suspend fun uploadMotorcycle(moto: MotorcycleDto): Boolean =
        executeWrite { apiService.upsertMotorcycle(moto.id, moto) }

    private suspend inline fun <T> fetchCollection(
        call: suspend () -> Response<Map<String, T>?>
    ): Result<List<T>> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Result.success(response.body()?.values?.toList() ?: emptyList())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend inline fun executeWrite(
        call: suspend () -> Response<*>
    ): Boolean {
        return try {
            call().isSuccessful
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }
}

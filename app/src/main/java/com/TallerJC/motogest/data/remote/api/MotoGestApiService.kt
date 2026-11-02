package com.TallerJC.motogest.data.remote.api

import com.TallerJC.motogest.data.remote.dto.ClientDto
import com.TallerJC.motogest.data.remote.dto.MotorcycleDto
import com.TallerJC.motogest.data.remote.dto.WorkOrderDto
import retrofit2.Response
import retrofit2.http.*

interface MotoGestApiService {

    // --- ÓRDENES ---
    @GET("work_orders.json")
    suspend fun getOrders(): Response<Map<String, WorkOrderDto>?>

    @GET("work_orders/{id}.json")
    suspend fun getOrderById(@Path("id") id: String): Response<WorkOrderDto?>

    @PUT("work_orders/{id}.json")
    suspend fun upsertOrder(@Path("id") id: String, @Body order: WorkOrderDto): Response<WorkOrderDto>

    @DELETE("work_orders/{id}.json")
    suspend fun deleteOrder(@Path("id") id: String): Response<Unit>

    // --- CLIENTES ---
    @GET("clients.json")
    suspend fun getClients(): Response<Map<String, ClientDto>?>

    @GET("clients/{id}.json")
    suspend fun getClientById(@Path("id") id: String): Response<ClientDto?>

    @PUT("clients/{id}.json")
    suspend fun upsertClient(@Path("id") id: String, @Body client: ClientDto): Response<ClientDto>

    @DELETE("clients/{id}.json")
    suspend fun deleteClient(@Path("id") id: String): Response<Unit>

    // --- MOTOCICLETAS ---
    @GET("motorcycles.json")
    suspend fun getMotorcycles(): Response<Map<String, MotorcycleDto>?>

    @GET("motorcycles/{id}.json")
    suspend fun getMotorcycleById(@Path("id") id: String): Response<MotorcycleDto?>

    @PUT("motorcycles/{id}.json")
    suspend fun upsertMotorcycle(@Path("id") id: String, @Body moto: MotorcycleDto): Response<MotorcycleDto>

    @DELETE("motorcycles/{id}.json")
    suspend fun deleteMotorcycle(@Path("id") id: String): Response<Unit>
}

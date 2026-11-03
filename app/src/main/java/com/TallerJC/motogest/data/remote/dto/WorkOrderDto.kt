package com.TallerJC.motogest.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WorkOrderDto(
    @SerializedName("id") val id: String,
    @SerializedName("folio") val folio: String,
    @SerializedName("clientId") val clientId: String,
    @SerializedName("motorcycleId") val motorcycleId: String,
    @SerializedName("receivedByUserId") val receivedByUserId: String,
    @SerializedName("status") val status: String,
    @SerializedName("serviceType") val serviceType: String,
    @SerializedName("reportedFailure") val reportedFailure: String,
    @SerializedName("deliveryType") val deliveryType: String,
    @SerializedName("deliveryCost") val deliveryCost: Double = 0.0,
    @SerializedName("totalLaborCost") val totalLaborCost: Double = 0.0,
    @SerializedName("totalPartsCost") val totalPartsCost: Double = 0.0,
    @SerializedName("totalAmount") val totalAmount: Double = 0.0,
    @SerializedName("recommendations") val recommendations: String? = null,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("startedAt") val startedAt: Long? = null,
    @SerializedName("completedAt") val completedAt: Long? = null,
    @SerializedName("deliveredAt") val deliveredAt: Long? = null,
    @SerializedName("updatedAt") val updatedAt: Long = System.currentTimeMillis()
)

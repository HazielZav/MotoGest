package com.TallerJC.motogest.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClientDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String? = null,
    @SerializedName("defaultDeliveryCost") val defaultDeliveryCost: Double = 0.0,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
)

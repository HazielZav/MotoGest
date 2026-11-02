package com.TallerJC.motogest.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MotorcycleDto(
    @SerializedName("id") val id: String,
    @SerializedName("clientId") val clientId: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("color") val color: String,
    @SerializedName("photoUrl") val photoUrl: String = "",
    @SerializedName("plate") val plate: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("vin") val vin: String? = null,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
)

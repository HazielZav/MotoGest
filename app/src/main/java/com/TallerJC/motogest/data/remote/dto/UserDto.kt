package com.TallerJC.motogest.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("roles") val roles: List<String> = emptyList(),
    @SerializedName("isActive") val isActive: Boolean = true
)

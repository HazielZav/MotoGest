package com.TallerJC.motogest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val address: String? = null,
    val defaultDeliveryCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

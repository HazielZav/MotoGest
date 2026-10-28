package com.TallerJC.motogest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "motorcycles",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["clientId"])
    ]
)
data class MotorcycleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val clientId: String,
    val brand: String,
    val model: String,
    val color: String,
    val photoUrl: String,
    val plate: String? = null,
    val year: Int? = null,
    val vin: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

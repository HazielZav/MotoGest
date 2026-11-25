package com.TallerJC.motogest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.TallerJC.motogest.domain.model.enums.MediaSyncStatus
import com.TallerJC.motogest.domain.model.enums.MediaTag
import java.util.UUID

/**
 * Metadatos locales de imágenes persistidas en `filesDir/images/`.
 * Diseñada para desacoplar la captura local de la futura subida a nube
 * (Firebase Storage / Azure Blob Storage mediante patrón Valet Key).
 */
@Entity(
    tableName = "media_records",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MotorcycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["motorcycleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["motorcycleId"]),
        Index(value = ["syncStatus"])
    ]
)
data class MediaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderId: String? = null,
    val motorcycleId: String? = null,
    val localUri: String,
    val localPath: String,
    val fileName: String,
    val mimeType: String = "image/jpeg",
    val fileSizeBytes: Long = 0L,
    val mediaType: String = "IMAGE",
    val tag: String = MediaTag.INTAKE_EVIDENCE.name,
    val notes: String? = null,
    val syncStatus: String = MediaSyncStatus.PENDING.name,
    val remoteUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

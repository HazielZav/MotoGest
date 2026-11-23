package com.TallerJC.motogest.domain.model.enums

/**
 * Estado de sincronización de un archivo multimedia local respecto a almacenamiento remoto
 * (Firebase Storage / Azure Blob Storage mediante patrón Valet Key).
 */
enum class MediaSyncStatus {
    PENDING,
    UPLOADING,
    SYNCED,
    FAILED
}

/**
 * Etiqueta funcional para distinguir el propósito de una imagen persistida localmente.
 */
enum class MediaTag {
    MOTO_PROFILE,
    INTAKE_EVIDENCE
}

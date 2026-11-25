package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.dao.MediaDao
import com.TallerJC.motogest.data.local.entity.MediaEntity
import com.TallerJC.motogest.data.local.storage.LocalImageStorage
import com.TallerJC.motogest.domain.model.enums.MediaSyncStatus
import com.TallerJC.motogest.domain.model.enums.MediaTag
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de persistencia local de imágenes y contrato de lectura/actualización
 * para el futuro flujo de sincronización en nube (patrón Valet Key).
 */
interface MediaRepository {
    // --- Operaciones locales (UI / Recepción) ---
    fun observeOrderImages(orderId: String): Flow<List<MediaEntity>>
    fun observeMotorcycleImages(motorcycleId: String): Flow<List<MediaEntity>>
    suspend fun saveImageMetadata(media: MediaEntity)
    suspend fun saveImagesMetadata(mediaList: List<MediaEntity>)
    suspend fun deleteImage(mediaId: String)
    suspend fun deleteMotorcycleProfileImages(motorcycleId: String)

    // --- Contrato para el equipo de nube (Valet Key / WorkManager) ---
    suspend fun getPendingUploads(): List<MediaEntity>
    fun observePendingUploadsCount(): Flow<Int>
    suspend fun markAsUploading(mediaId: String)
    suspend fun markAsSynced(mediaId: String, remoteUrl: String)
    suspend fun markAsFailed(mediaId: String)
}

class LocalMediaRepository(
    private val mediaDao: MediaDao,
    private val imageStorage: LocalImageStorage? = null
) : MediaRepository {

    override fun observeOrderImages(orderId: String): Flow<List<MediaEntity>> {
        return mediaDao.getMediaByOrderId(orderId)
    }

    override fun observeMotorcycleImages(motorcycleId: String): Flow<List<MediaEntity>> {
        return mediaDao.getMediaByMotorcycleId(motorcycleId)
    }

    override suspend fun saveImageMetadata(media: MediaEntity) {
        mediaDao.insertMedia(media)
    }

    override suspend fun saveImagesMetadata(mediaList: List<MediaEntity>) {
        if (mediaList.isEmpty()) return
        mediaDao.insertMediaList(mediaList)
    }

    override suspend fun deleteImage(mediaId: String) {
        val existing = mediaDao.getMediaById(mediaId)
        mediaDao.deleteMedia(mediaId)
        if (existing != null) {
            imageStorage?.deleteLocalFile(existing.localPath)
        }
    }

    override suspend fun deleteMotorcycleProfileImages(motorcycleId: String) {
        val existing = mediaDao.getMediaByMotorcycleIdAndTag(motorcycleId, MediaTag.MOTO_PROFILE.name)
        mediaDao.deleteMediaByMotorcycleIdAndTag(motorcycleId, MediaTag.MOTO_PROFILE.name)
        existing.forEach { media ->
            imageStorage?.deleteLocalFile(media.localPath)
        }
    }

    override suspend fun getPendingUploads(): List<MediaEntity> {
        return mediaDao.getPendingSyncMedia()
    }

    override fun observePendingUploadsCount(): Flow<Int> {
        return mediaDao.observePendingSyncCount()
    }

    override suspend fun markAsUploading(mediaId: String) {
        mediaDao.updateSyncStatus(mediaId, MediaSyncStatus.UPLOADING.name)
    }

    override suspend fun markAsSynced(mediaId: String, remoteUrl: String) {
        mediaDao.markAsSynced(
            id = mediaId,
            remoteUrl = remoteUrl,
            syncStatus = MediaSyncStatus.SYNCED.name
        )
    }

    override suspend fun markAsFailed(mediaId: String) {
        mediaDao.updateSyncStatus(mediaId, MediaSyncStatus.FAILED.name)
    }
}

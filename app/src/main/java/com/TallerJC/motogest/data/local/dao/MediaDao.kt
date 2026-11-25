package com.TallerJC.motogest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.TallerJC.motogest.data.local.entity.MediaEntity
import com.TallerJC.motogest.domain.model.enums.MediaSyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_records WHERE orderId = :orderId ORDER BY createdAt ASC")
    fun getMediaByOrderId(orderId: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_records WHERE motorcycleId = :motorcycleId ORDER BY createdAt DESC")
    fun getMediaByMotorcycleId(motorcycleId: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_records WHERE motorcycleId = :motorcycleId AND tag = :tag")
    suspend fun getMediaByMotorcycleIdAndTag(motorcycleId: String, tag: String): List<MediaEntity>

    @Query("DELETE FROM media_records WHERE motorcycleId = :motorcycleId AND tag = :tag")
    suspend fun deleteMediaByMotorcycleIdAndTag(motorcycleId: String, tag: String)

    @Query("SELECT * FROM media_records WHERE id = :id")
    suspend fun getMediaById(id: String): MediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaEntity>)

    @Query("DELETE FROM media_records WHERE id = :id")
    suspend fun deleteMedia(id: String)

    @Query("DELETE FROM media_records WHERE orderId = :orderId")
    suspend fun deleteMediaByOrderId(orderId: String)

    // --- Contrato para el equipo de nube (Patrón Valet Key / WorkManager) ---

    @Query("SELECT * FROM media_records WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY createdAt ASC")
    suspend fun getPendingSyncMedia(): List<MediaEntity>

    @Query("SELECT COUNT(*) FROM media_records WHERE syncStatus IN ('PENDING', 'FAILED')")
    fun observePendingSyncCount(): Flow<Int>

    @Query("UPDATE media_records SET syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSyncStatus(
        id: String,
        syncStatus: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE media_records SET syncStatus = :syncStatus, remoteUrl = :remoteUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsSynced(
        id: String,
        remoteUrl: String,
        syncStatus: String = MediaSyncStatus.SYNCED.name,
        updatedAt: Long = System.currentTimeMillis()
    )
}

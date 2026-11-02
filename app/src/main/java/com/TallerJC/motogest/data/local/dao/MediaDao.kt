package com.TallerJC.motogest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.TallerJC.motogest.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_records WHERE orderId = :orderId ORDER BY createdAt ASC")
    fun getMediaByOrderId(orderId: String): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaEntity>)

    @Query("DELETE FROM media_records WHERE id = :id")
    suspend fun deleteMedia(id: String)

    @Query("DELETE FROM media_records WHERE orderId = :orderId")
    suspend fun deleteMediaByOrderId(orderId: String)
}

package com.TallerJC.motogest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.TallerJC.motogest.data.local.converter.Converters
import com.TallerJC.motogest.data.local.dao.MediaDao
import com.TallerJC.motogest.data.local.dao.NotificationDao
import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.entity.*

@Database(
    entities = [
        ClientEntity::class,
        MotorcycleEntity::class,
        SparePartEntity::class,
        UserEntity::class,
        WorkItemEntity::class,
        WorkOrderEntity::class,
        NotificationEntity::class,
        MediaEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workOrderDao(): WorkOrderDao
    abstract fun notificationDao(): NotificationDao
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motogest_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

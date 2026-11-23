package com.TallerJC.motogest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.TallerJC.motogest.data.local.converter.Converters
import com.TallerJC.motogest.data.local.dao.MediaDao
import com.TallerJC.motogest.data.local.dao.NotificationDao
import com.TallerJC.motogest.data.local.dao.UserDao
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
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workOrderDao(): WorkOrderDao
    abstract fun notificationDao(): NotificationDao
    abstract fun mediaDao(): MediaDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN password TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motogest_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

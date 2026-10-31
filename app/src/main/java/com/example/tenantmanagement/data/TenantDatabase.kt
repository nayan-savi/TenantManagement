package com.example.tenantmanagement.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Tenant::class, Flat::class, Building::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TenantDatabase : RoomDatabase() {

    abstract fun tenantDao(): TenantDao
    abstract fun flatDao(): FlatDao
    abstract fun buildingDao(): BuildingDao

    companion object {
        @Volatile
        private var INSTANCE: TenantDatabase? = null

        fun getDatabase(context: Context): TenantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TenantDatabase::class.java,
                    "tenant_management_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

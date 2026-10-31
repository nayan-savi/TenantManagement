package com.example.tenantmanagement.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTenantStatus(status: TenantStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTenantStatus(statusString: String): TenantStatus {
        return try {
            TenantStatus.valueOf(statusString)
        } catch (e: Exception) {
            TenantStatus.ACTIVE
        }
    }
}

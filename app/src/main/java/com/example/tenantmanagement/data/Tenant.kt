package com.example.tenantmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TenantStatus(val displayName: String) {
    ACTIVE("Active"),
    NOTICE_PERIOD("Notice Period"),
    VACATED("Vacated")
}

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val flatNumber: String,
    val buildingName: String = "",
    val flatId: Int? = null,
    val dateJoined: String,
    val rent: Double,
    val advanceAmount: Double,
    val aadharNumber: String,
    val status: TenantStatus = TenantStatus.ACTIVE,
    val yearlyIncrement: String, // e.g. "10%" or "₹2,000"
    val agreementUri: String? = null, // URI string for uploaded document/photo
    val agreementFileName: String? = null,
    val phoneNumber: String = "",
    val emailAddress: String = "",
    val lastIncrementYear: Int = 0,
    val notes: String = ""
)

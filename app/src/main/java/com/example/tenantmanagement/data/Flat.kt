package com.example.tenantmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flats")
data class Flat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val buildingId: Int = 0,
    val buildingName: String,
    val locality: String = "",
    val flatNumber: String,
    val ecAccountNumber: String = "",
    val rrNumber: String = "",
    val waterMeterNumber: String = "",
    val notes: String = ""
)

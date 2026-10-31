package com.example.tenantmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buildings")
data class Building(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val locality: String = "",
    val totalFloors: String = "",
    val ownerName: String = "",
    val ownerEmail: String = "",
    val notes: String = ""
)

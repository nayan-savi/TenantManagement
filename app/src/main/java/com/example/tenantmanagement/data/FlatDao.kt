package com.example.tenantmanagement.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlatDao {
    @Query("SELECT * FROM flats ORDER BY buildingName ASC, flatNumber ASC")
    fun getAllFlats(): Flow<List<Flat>>

    @Query("SELECT * FROM flats WHERE id = :id")
    fun getFlatById(id: Int): Flow<Flat?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlat(flat: Flat): Long

    @Update
    suspend fun updateFlat(flat: Flat)

    @Delete
    suspend fun deleteFlat(flat: Flat)
}

package com.example.tenantmanagement.data

import kotlinx.coroutines.flow.Flow

class TenantRepository(
    private val tenantDao: TenantDao,
    private val flatDao: FlatDao,
    private val buildingDao: BuildingDao
) {

    val allTenants: Flow<List<Tenant>> = tenantDao.getAllTenants()
    val allFlats: Flow<List<Flat>> = flatDao.getAllFlats()
    val allBuildings: Flow<List<Building>> = buildingDao.getAllBuildings()

    fun getTenantById(id: Int): Flow<Tenant?> {
        return tenantDao.getTenantById(id)
    }

    suspend fun insertTenant(tenant: Tenant): Long {
        return tenantDao.insertTenant(tenant)
    }

    suspend fun updateTenant(tenant: Tenant) {
        tenantDao.updateTenant(tenant)
    }

    suspend fun deleteTenant(tenant: Tenant) {
        tenantDao.deleteTenant(tenant)
    }

    fun searchTenants(query: String): Flow<List<Tenant>> {
        return tenantDao.searchTenants(query)
    }

    suspend fun insertFlat(flat: Flat): Long {
        return flatDao.insertFlat(flat)
    }

    suspend fun updateFlat(flat: Flat) {
        flatDao.updateFlat(flat)
    }

    suspend fun deleteFlat(flat: Flat) {
        flatDao.deleteFlat(flat)
    }

    suspend fun insertBuilding(building: Building): Long {
        return buildingDao.insertBuilding(building)
    }

    suspend fun updateBuilding(building: Building) {
        buildingDao.updateBuilding(building)
    }

    suspend fun deleteBuilding(building: Building) {
        buildingDao.deleteBuilding(building)
    }
}

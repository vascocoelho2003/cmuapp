package com.example.cmuapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cmuapp.data.entities.Establishment
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the Establishment entity.
 * Provides methods for interacting with the Establishment table in the Room database.
 */
@Dao
interface EstablishmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(establishments: List<Establishment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(establishment: Establishment)

    @Query("SELECT * FROM establishment")
    fun getAllEstablishments(): Flow<List<Establishment>>

    @Query("SELECT * FROM establishment WHERE id = :id LIMIT 1")
    suspend fun getEstablishmentById(id: String): Establishment?

    @Query("SELECT * FROM establishment")
    suspend fun getAllEstablishmentsOnce(): List<Establishment>

    @Query("SELECT * FROM establishment WHERE id = :id")
    fun getEstablishmentByIdFlow(id: String): Flow<Establishment>

    @Query("SELECT * FROM establishment WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<Establishment>
}
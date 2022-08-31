package com.training.tracking_app.data.dao

import androidx.room.*
import com.training.tracking_app.data.Travel

@Dao
interface TravelDao {

    @Query("SELECT * FROM Travel")
    fun getAll() : List<Travel>

    @Query("SELECT * FROM Travel WHERE id = :id")
    suspend fun getById(id : Int) : Travel

    @Query("SELECT * FROM Travel WHERE status = 1 AND code = :code")
    suspend fun getByCode(code : String) : Travel

    @Update
    suspend fun update(travel : Travel)

    @Insert
    suspend fun insert(travel : List<Travel>)

    @Insert
    suspend fun insert(travel : Travel)

    @Delete
    suspend fun delete(travel : Travel)
}
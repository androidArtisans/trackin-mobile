package com.training.tracking_app.data.dao

import androidx.room.*
import com.training.tracking_app.data.Travel

@Dao
interface TravelDao {

    @Query("SELECT * FROM Travel")
    fun getAll() : List<Travel>

    @Query("SELECT * FROM Travel WHERE status = 'TRUE' AND code = :code")
    fun getByCode(code : String) : Travel

    @Update
    fun update(travel : Travel)

    @Insert
    fun insert(travel : List<Travel>)

    @Delete
    fun delete(travel : Travel)
}
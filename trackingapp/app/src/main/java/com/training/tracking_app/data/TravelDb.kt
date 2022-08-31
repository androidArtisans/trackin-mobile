package com.training.tracking_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.training.tracking_app.data.dao.TravelDao


@Database(
    entities = [Travel::class],
    version = 1
)
abstract class TravelDb : RoomDatabase() {

    abstract fun travelDao() : TravelDao

}
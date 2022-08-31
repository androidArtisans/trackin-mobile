package com.training.tracking_app.data

import android.app.Application
import androidx.room.Room

class RoomApp : Application() {
    val room = Room
        .databaseBuilder(this, TravelDb::class.java, "Track")
        .build()
}
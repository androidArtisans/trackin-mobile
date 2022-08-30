package com.training.tracking_app

import android.app.Application
import androidx.room.Room
import com.training.tracking_app.data.TravelDb

class TravelApp : Application() {
    var room = Room
        .databaseBuilder(this, TravelDb::class.java,"appTrack")
        .build()
}
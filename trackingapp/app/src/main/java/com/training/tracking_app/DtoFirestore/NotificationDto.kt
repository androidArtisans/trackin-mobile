package com.training.tracking_app.DtoFirestore

import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

data class NotificationDto(val color : String,
                            val coordinates : GeoPoint,
                            val description : String,
                            val order : Int,
                            val title : String,
                            val travel : String) : Serializable{
    constructor() : this("",GeoPoint(0.0,0.0),"",99999,"","")
}
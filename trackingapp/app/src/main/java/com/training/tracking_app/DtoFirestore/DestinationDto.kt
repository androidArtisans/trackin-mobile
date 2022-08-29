package com.training.tracking_app.DtoFirestore

import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

data class DestinationDto(
    val coordinates : GeoPoint,
    val description: String,
    val name : String
) : Serializable{
    constructor() : this(GeoPoint(0.0,0.0), "","")
}
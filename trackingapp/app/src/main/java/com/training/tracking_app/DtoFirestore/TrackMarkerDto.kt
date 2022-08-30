package com.training.tracking_app.DtoFirestore

import org.osmdroid.util.GeoPoint
import java.io.Serializable

data class TrackMarkerDto(
    val point : GeoPoint,
    val icon : Int,
    val title : String,
    val subDescription : String,
    val step : Int,
    val end : Boolean,
    val start : Boolean
) : Serializable
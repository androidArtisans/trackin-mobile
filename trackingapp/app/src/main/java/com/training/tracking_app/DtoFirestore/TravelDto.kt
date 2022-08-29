package com.training.tracking_app.DtoFirestore

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

data class TravelDto(
                     val code : String,
                     val hora : Timestamp,
                     val llegada : Timestamp,
                     val route : String,
                     val status : Boolean,
                     val streamer : HashMap<String, String> = HashMap<String, String>(),
                     val viewers : HashMap<String, String> = HashMap<String, String>()) : Serializable{
    constructor() : this("", Timestamp(Date()),Timestamp(Date()),"",false, HashMap<String, String>(), HashMap<String, String>())
}
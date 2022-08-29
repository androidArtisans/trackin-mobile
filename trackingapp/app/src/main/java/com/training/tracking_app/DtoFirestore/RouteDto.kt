package com.training.tracking_app.DtoFirestore

import java.io.Serializable

data class RouteDto(val description : String,
                    val from : String,
                    val status : Boolean,
                    val to : String) : Serializable{
        constructor() : this("", "", false, "")
}
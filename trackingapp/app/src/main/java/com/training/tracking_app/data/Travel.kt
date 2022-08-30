package com.training.tracking_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Travel(
    @PrimaryKey(autoGenerate = true)
    val id : Int
,    val idTravel : String,
    val code : String,
    val status : Boolean
)
package com.training.tracking_app.Dto

data class TravelDto(
    val code : String,
    val description : String,
    val point : ArrayList<PointDto>?
    )
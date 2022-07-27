package com.training.tracking_app.Dto

import java.io.Serializable

data class NotificacionDto(
    val title : String,
    val description: String,
    val color : String,
    val timeNotificaciont : String
):Serializable
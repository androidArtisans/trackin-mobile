package com.training.tracking_app.network.response

import com.google.gson.annotations.SerializedName
import com.training.tracking_app.Dto.NotificacionDto
import com.training.tracking_app.Dto.TravelDto
import java.io.Serializable

data class NotificationResponse(
    @SerializedName("status") var status : String,
    @SerializedName("notification") var notification : NotificacionDto,
    @SerializedName("error") var error : String,
): Serializable
package com.training.tracking_app.network.response

import com.google.gson.annotations.SerializedName
import com.training.tracking_app.Dto.TravelDto
import java.io.Serializable

data class TravelResponse(
    @SerializedName("status") var status : String,
    @SerializedName("travel") var travel : TravelDto,
    @SerializedName("error") var error : String,
):Serializable
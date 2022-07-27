package com.training.tracking_app.network.response.api

import com.training.tracking_app.Dto.PointDto
import com.training.tracking_app.DtoLaravel.Trackin
import com.training.tracking_app.network.response.TravelResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiResource {
    @POST("trakin")
    suspend fun addTrackin(@Body point: Trackin):Response<*>

    @GET("findByCode/{code}")
    suspend fun findTravel(@Path("code") code : String): Response<*>

    @GET("travel/{code}")
    suspend fun getNotification(@Path("code") code : String): Response<TravelResponse>


}
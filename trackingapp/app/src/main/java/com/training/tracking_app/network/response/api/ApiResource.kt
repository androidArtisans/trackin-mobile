package com.training.tracking_app.network.response.api

import com.training.tracking_app.network.response.TravelResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiResource {
    @GET("/travel/{code}")
    suspend fun findTravel(@Path("code") code : String): Response<TravelResponse>


    @GET("/travel/{code}")
    suspend fun getNotification(@Path("code") code : String): Response<TravelResponse>

}
package com.training.tracking_app.network.response.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val URL = "https://laravel-zwhc.frb.io/api/"
object ApiObject {

    fun getRetro() : ApiResource{
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit
                    .Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
        return retrofit.create(ApiResource::class.java)
    }
}
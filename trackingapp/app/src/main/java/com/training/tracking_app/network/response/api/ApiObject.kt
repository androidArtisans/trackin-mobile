package com.training.tracking_app.network.response.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val URL = "http://10.240.2.110:3000/"
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
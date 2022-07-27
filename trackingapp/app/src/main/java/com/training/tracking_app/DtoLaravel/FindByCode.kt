package com.training.tracking_app.DtoLaravel

data class FindByCode (

    var status : Int, var id : String , var code : String, var points : ArrayList<Trackin>?
    )
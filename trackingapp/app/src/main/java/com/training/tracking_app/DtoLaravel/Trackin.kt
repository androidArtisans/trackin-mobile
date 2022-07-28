package com.training.tracking_app.DtoLaravel

import java.sql.Date

data class Trackin(var latitude:String,
                   var longitude:String,
                   var message :String,
                   var mac_address : String,
                   var register_point_date : String,
                   var message_type :String
)
package com.training.tracking_app.helper

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.internal.LinkedTreeMap
import com.training.tracking_app.DtoLaravel.FindByCode
import com.training.tracking_app.DtoLaravel.Trackin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

object HelperApi {
    @RequiresApi(Build.VERSION_CODES.O)
    fun findByCode(obj : List<*>) : FindByCode?{
        var _res : FindByCode? = null
        Log.d("AAA", obj.toString())
        if(obj.isNotEmpty()){
            val _trackin : ArrayList<Trackin> = ArrayList<Trackin>()
            val _aux = obj.get(0) as LinkedTreeMap<*, *>
            _res = FindByCode(200,_aux.get("id").toString(), _aux.get("code").toString(), null)
            if(_aux.get("trakins") != null){
                val _list = _aux.get("trakins") as List<LinkedTreeMap<*, *>>
                for(p in _list){
                    _trackin.add(Trackin(
                        p.get("latitude").toString(),
                        p.get("longitude").toString(),
                        p.get("message").toString(),
                        _res.code,
                        getDateMySQL(),
                        "Automatic"
                    )
                    )
                }
                _res.points = _trackin
            }
        }
        return _res
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateMySQL() : String {
        var current = LocalDateTime.now()
        var formater = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        var formated = current.format(formater)
        return formated
    }
}
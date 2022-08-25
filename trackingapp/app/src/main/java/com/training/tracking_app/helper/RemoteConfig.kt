package com.training.tracking_app.helper

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class RemoteConfig(val appTitle: String,
                   val maxDistance: Int,
                   val minDistance: Int,
                   val refreshTime: Int,
                   val version: Int){
    fun getVersionString(): String {
        return version.toString()
    }
}
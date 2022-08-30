package com.training.tracking_app

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.room.Room
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.training.tracking_app.data.TravelDb
import com.training.tracking_app.data.dao.TravelDao
import com.training.tracking_app.databinding.ActivityMainBinding
import com.training.tracking_app.databinding.ActivitySplashBinding
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.helper.RemoteConfig

class SplashActivity : AppCompatActivity() {
    lateinit var binding : ActivitySplashBinding
    lateinit var handler : Handler

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 60
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate().addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                HelperApi.showLog("REMOTE CONFIG DONE...")
                binding.tvAppTitle.text = remoteConfig["appTitle"].asString()
                binding.tvVersion.text = "VERSIÓN ${remoteConfig["version"].asString()}"
            }
        }



        setContentView(binding.root)
        handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}
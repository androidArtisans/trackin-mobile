package com.training.tracking_app


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import androidx.fragment.app.Fragment
import com.training.tracking_app.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val TAG = "PermissionDemo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupPermissions()
        init()
    }

    private fun setupPermissions() {
        val permission = checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Permission to access the location is required for this app to tracking.")
                        .setTitle("Permission required")
                            builder.setPositiveButton("OK"
                            ) { dialog, id ->
                                Log.i(TAG, "Clicked")
                                makeRequest()
                    }

                    val dialog = builder.create()
                dialog.show()
            } else {
                makeRequest()
            }
        }
    }
    private fun makeRequest() {
        requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            225)
    }

    override fun onResume() {
        super.onResume()
        if (checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            init()
        }
    }

    override fun onPause() {
        super.onPause()
        if (checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
           init()
        }
    }

    private fun init(){
        if(checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            loadFragment(TrackFragment())
            val bundle = Bundle()
            binding.bottomNav.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.tracks -> {
                        bundle.putString("code", "")
                        val fragmentTrack = TrackFragment()
                        fragmentTrack.arguments = bundle
                        loadFragment(fragmentTrack)
                    }
//                    R.id.notifications -> {
//                        bundle.putString("code", "")
//                        val fragmentNotyif = NotificationFragment()
//                        fragmentNotyif.arguments = bundle
//                        loadFragment(fragmentNotyif)
//                    }
                    R.id.gps -> {
                        bundle.putString("code", "")
                        val fragmentGps = GpsFragment()
                        fragmentGps.arguments = bundle
                        loadFragment(fragmentGps)
                    }
                }
                true
            }
        }else{
           makeRequest()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
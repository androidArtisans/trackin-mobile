package com.training.tracking_app

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.training.tracking_app.databinding.FragmentGpsBinding

class GpsFragment : Fragment() {

    var _binding : FragmentGpsBinding? = null
    private val binding get() = _binding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation : Location ? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGpsBinding.inflate(inflater, container, false)
        binding!!.fbSend.setOnClickListener{
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            readLastKnownLocation()
        }
        return binding!!.root
    }

    @SuppressLint("MissingPermission") //permission are checked before
    fun readLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it) }
            }
    }
    fun updateLocation(newLocation: Location) {
        lastLocation = newLocation
        Log.d("LONGITUD", newLocation.longitude.toString())
        Log.d("LONGITUD", newLocation.latitude.toString())

    }

}
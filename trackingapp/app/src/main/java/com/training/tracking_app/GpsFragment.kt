package com.training.tracking_app

import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.training.tracking_app.DtoLaravel.Trackin
import com.training.tracking_app.databinding.FragmentGpsBinding
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.network.response.api.ApiObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.sql.Date
import java.util.*

class GpsFragment : Fragment() {

    var _binding : FragmentGpsBinding? = null
    private val binding get() = _binding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation : Location ? = null

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission") //permission are checked before
    fun readLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it) }
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLocation(newLocation: Location) {
        lastLocation = newLocation
        var _point = Trackin(
            newLocation.latitude.toString(),
            newLocation.longitude.toString(),
            "REGISTRO",
            "012012",
            HelperApi.getDateMySQL(),
            "Automatic"
            )
        CoroutineScope(Dispatchers.IO).launch {
            val _res: Response<*>
            _res = ApiObject.getRetro().addTrackin(_point)
            val _response = _res.body()!!
            activity?.runOnUiThread{
                if(_res.isSuccessful){
                    Toast.makeText(context, "Ubicacion registrada Correctamente", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

}
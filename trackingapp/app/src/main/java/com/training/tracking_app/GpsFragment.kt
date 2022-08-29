package com.training.tracking_app

import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.training.tracking_app.DtoLaravel.Device
import com.training.tracking_app.DtoLaravel.Trackin
import com.training.tracking_app.databinding.FragmentGpsBinding
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.helper.showCustomToast
import com.training.tracking_app.network.response.api.ApiObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response


class GpsFragment : Fragment() {
    private val TAG ="TRACKING"

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
        binding!!.ivSubscribe.setOnClickListener {
                subscribe()
        }
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun subscribe(){
        var type = "Celular"
        var device = Device(
            macAddres(),
            HelperApi.getDateMySQL(),
            type
        )
        Log.d(TAG, device.toString())
        CoroutineScope(Dispatchers.IO).launch {
            val _res: Response<*>
            _res = ApiObject.getRetro().subscribe(device)
            Log.d(TAG, _res.toString())
            activity?.runOnUiThread{
                if(_res.isSuccessful){
                    Toast(context).showCustomToast(getString(R.string.subs_ok), requireActivity())
                }else{
                    Toast(context).showCustomToast(getString(R.string.bad_sequence), requireActivity())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission") //permission are checked before
    fun readLastKnownLocation() {
        var _point = Trackin(
            "10",
            "12",
            "- REGISTRO MANUAL POR 123456 - ",
            "1231231",
            HelperApi.getDateMySQL(),
            "Automatic"
        )
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it) }
            }
    }
    fun registerFireStore(point : Trackin){
        val db = Firebase.firestore
        /* FIREBASE */
        db.collection("travel")
            .add(point)
            .addOnSuccessListener {
                Log.d(TAG, "REFERENCE ${it.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "ERROR ", e)
            }
        /* FIREBASE*/
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLocation(newLocation: Location) {
        lastLocation = newLocation
        var androidId = macAddres()
        var _point = Trackin(
            newLocation.latitude.toString(),
            newLocation.longitude.toString(),
            "- REGISTRO MANUAL POR $androidId - ",
            androidId,
            HelperApi.getDateMySQL(),
            "Automatic"
            )

        registerFireStore(_point)
        CoroutineScope(Dispatchers.IO).launch {
            val _res: Response<*>
            _res = ApiObject.getRetro().addTrackin(_point)
            Log.d(TAG, _res.toString())
            activity?.runOnUiThread{
                if(_res.isSuccessful){
                    Toast(context).showCustomToast(getString(R.string.ubication_success), requireActivity())
                }else{
                    Toast(context).showCustomToast(getString(R.string.no_device_reg), requireActivity())
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun macAddres(): String {
        val ctx = requireContext().applicationContext
        return Settings.Secure.getString(ctx.contentResolver, "android_id")
    }
}
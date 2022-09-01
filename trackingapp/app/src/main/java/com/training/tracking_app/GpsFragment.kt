package com.training.tracking_app

import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.training.tracking_app.DtoFirestore.NotificationDto
import com.training.tracking_app.data.TravelDb
import com.training.tracking_app.databinding.FragmentGpsBinding
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.helper.showCustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GpsFragment : Fragment() {

    private var _binding : FragmentGpsBinding? = null
    private val binding get() = _binding
    private val db = Firebase.firestore
    lateinit var _room : TravelDb

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation : Location ? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGpsBinding.inflate(inflater, container, false)
        _room = Room.databaseBuilder(container!!.context,TravelDb::class.java,"Track").build()

        binding!!.fbSend.setOnClickListener{
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            readLastKnownLocation(HelperApi.INFO, getString(R.string.registry_auto))
        }
        binding!!.fbSendError.setOnClickListener{
            if(binding!!.etMsg.text.toString().trim().isNotEmpty()){
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                readLastKnownLocation(HelperApi.ERROR, binding!!.etMsg.text.toString().trim())
            } else {
                Toast(context).showCustomToast( getString(R.string.msg_required), requireActivity())
            }
        }
        binding!!.fbSendWarning.setOnClickListener{
            if(binding!!.etMsg.text.toString().trim().isNotEmpty()) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                readLastKnownLocation(HelperApi.WARNING, binding!!.etMsg.text.toString().trim())
            } else {
                Toast(context).showCustomToast( getString(R.string.msg_required), requireActivity())
            }
        }
        setTitle()
        return binding!!.root
    }

    private fun setTitle(){
        CoroutineScope(Dispatchers.IO).launch {
            var dataDB = _room.travelDao().getActiveTravel()
            activity?.runOnUiThread{
                if(dataDB != null){
                    binding!!.tvTitle.text = "TRACKING > TRAVEL > ${dataDB.code.toString().uppercase()}"
                } else {
                    binding!!.tvTitle.text = "TRACKING > TRAVEL > --"
                    Toast(context).showCustomToast(getString(R.string.travel_select), requireActivity())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    fun readLastKnownLocation(_type : String, msg : String) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it, _type, msg) }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLocation(newLocation: Location, _type : String, msg : String) {
        CoroutineScope(Dispatchers.IO).launch {
            val travelActive = _room.travelDao().getActiveTravel()
            lastLocation = newLocation
            val androidId = macAddres()
            val _fbNotification = NotificationDto(
                _type,
                GeoPoint(newLocation.latitude,newLocation.longitude),
                msg,
                0,
                _type,
                travelActive!!.idTravel
            )
            db.collection("notification").add(_fbNotification)
                .addOnSuccessListener {
                    HelperApi.showLog("SUCCESS "+it.id)
                }
                .addOnFailureListener { e ->
                    HelperApi.showLog("FAILURE "+ e.toString())
                }
            activity?.runOnUiThread{
                binding!!.etMsg.setText("")
                Toast(context).showCustomToast(getString(R.string.success_reg), requireActivity())
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun macAddres(): String {
        val ctx = requireContext().applicationContext
        return Settings.Secure.getString(ctx.contentResolver, "android_id")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun subscribe(){}
}
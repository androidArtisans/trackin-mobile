package com.training.tracking_app

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.training.tracking_app.Dto.NotificacionDto
import com.training.tracking_app.DtoLaravel.FindByCode
import com.training.tracking_app.DtoLaravel.Trackin
import com.training.tracking_app.databinding.FragmentGpsBinding
import com.training.tracking_app.databinding.FragmentNotificationBinding
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.network.response.TravelResponse
import com.training.tracking_app.network.response.api.ApiObject
import com.training.tracking_app.ui.adapter.NotificacionsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class NotificationFragment : Fragment() {
    var code : String ? = null
    var _binding : FragmentNotificationBinding? = null
    private val binding get() = _binding

    private var listNotificacions =  ArrayList<Trackin>()
    private lateinit var adapter: NotificacionsAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationBinding.inflate(inflater, container,false)
        initRecyclerView(binding!!.root)
        binding!!.btnFind.setOnClickListener{
            if(!binding!!.etCode.text.toString().equals("")){
                findTravel(binding!!.etCode.text.toString())
            } else {
                Toast.makeText(context, "Su viaje ya se encuentra en progreso, pero no se tiene ningun track", Toast.LENGTH_SHORT).show()
            }
        }
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun findTravel(code : String) {
        CoroutineScope(Dispatchers.IO).launch {
            val _res: Response<*>
            _res = ApiObject.getRetro().findTravel("012012")
            val _response : FindByCode? = HelperApi.findByCode(_res.body()!! as List<*>)
            activity?.runOnUiThread{
                if(_res.isSuccessful){
                    if(_response != null){
                        if(_response.points != null)
                            fillNotification(_response.points!!)
                        else
                            Toast.makeText(context, "Su viaje ya se encuentra en progreso, pero no se tiene ningun track", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Su viaje aun no se encuentra disponible", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun fillNotification(points: ArrayList<Trackin>) {
        for( p: Trackin in points){
            listNotificacions.add(p)
        }
        adapter.notifyDataSetChanged()
    }


    private fun initRecyclerView(view : View) {
        adapter = NotificacionsAdapter(listNotificacions)
        var rv = view.findViewById<RecyclerView>(R.id.rvNotificacions)
        rv.layoutManager = LinearLayoutManager(view.context)
        rv.adapter = adapter
    }
}
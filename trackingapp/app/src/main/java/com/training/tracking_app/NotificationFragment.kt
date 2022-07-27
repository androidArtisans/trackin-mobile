package com.training.tracking_app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.training.tracking_app.Dto.NotificacionDto
import com.training.tracking_app.databinding.FragmentGpsBinding
import com.training.tracking_app.databinding.FragmentNotificationBinding
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


    var listNotificacions = listOf(
        NotificacionDto("Notificacion 1", "DESCRIPCION", "#ff0000", "hoy"),
        NotificacionDto("Notificacion 2", "DESCRIPCION", "#00ff00", "hoy"),
        NotificacionDto("Notificacion 3", "DESCRIPCION", "#0000ff", "hoy")
    )
    private lateinit var adapter: NotificacionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationBinding.inflate(inflater, container,false)
        initRecyclerView(binding!!.root)
        binding!!.btnMore.setOnClickListener{
            val bundle = arguments
            val message = bundle!!.getString("code")
            Toast.makeText(context, "MAS NOTIF $message" ,Toast.LENGTH_SHORT).show()

            getNotification()
        }
        return binding!!.root
    }

    private fun getNotification() {
        Toast.makeText(context, "MAS NOTIF $code" ,Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            val _res: Response<TravelResponse>
            _res = ApiObject.getRetro().findTravel("123")
            val _response = _res.body()!!

            activity?.runOnUiThread{
                if(_res.isSuccessful){
                    if(_response.status == "200"){
                        if(_response.travel.route != null){

                        } else {

                        }
                    } else {

                    }
                }
            }
        }
    }


    private fun initRecyclerView(view : View) {
        adapter = NotificacionsAdapter(listNotificacions)
        var rv = view.findViewById<RecyclerView>(R.id.rvNotificacions)
        rv.layoutManager = LinearLayoutManager(view.context)
        rv.adapter = adapter
    }
}
package com.training.tracking_app

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.training.tracking_app.DtoFirestore.TravelDto
import com.training.tracking_app.DtoLaravel.FindByCode
import com.training.tracking_app.DtoLaravel.Trackin
import com.training.tracking_app.data.RoomApp
import com.training.tracking_app.data.Travel
import com.training.tracking_app.data.TravelDb
import com.training.tracking_app.databinding.FragmentNotificationBinding
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.helper.showCustomToast
import com.training.tracking_app.network.response.api.ApiObject
import com.training.tracking_app.ui.adapter.NotificacionsAdapter
import com.training.tracking_app.ui.adapter.adapterFirebase.ClickListener
import com.training.tracking_app.ui.adapter.adapterFirebase.TravelAdapter
import io.grpc.LoadBalancer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response


class NotificationFragment : Fragment() {
    var code : String ? = null
    var _binding : FragmentNotificationBinding? = null
    private val binding get() = _binding

    lateinit var _room : TravelDb
    private val db = Firebase.firestore

    private var listTravelDB =  ArrayList<Travel>()
    private lateinit var adapter: TravelAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationBinding.inflate(inflater, container,false)
        _room = Room.databaseBuilder(container!!.context,TravelDb::class.java,"Track").build()
        initRecyclerView(binding!!.root)

        binding!!.btnFind.setOnClickListener{
            if(!binding!!.etCode.text.toString().equals("")){
                CoroutineScope(Dispatchers.IO).launch {
                    getData(binding!!.etCode.text.toString().trim())
                    listTravelDB = _room.travelDao().getAll() as ArrayList
                    activity?.runOnUiThread{
                        updateAdapter(listTravelDB)
                    }
                }
            } else {
                Toast(context).showCustomToast(getString(R.string.no_points), requireActivity())
            }
        }
        return binding!!.root
    }

    suspend fun getData(code : String){
        var travelDB = _room.travelDao().getByCode(code)
        HelperApi.showLog("EXISTE "+travelDB.toString())
        if(travelDB == null){
            val documentTravel = getTravel(code)[0]
            val travelFB = documentTravel.toObject(TravelDto::class.java)!!
            val newTravel = Travel(id=0, idTravel = documentTravel.id,code = travelFB.code, status = travelFB.status)
            _room.travelDao().insert(newTravel)
        }
    }

    private suspend fun getTravel(code : String) : List<DocumentSnapshot>{
        val snapshot = db.collection("travel").whereEqualTo("status",true).whereEqualTo("code",code).limit(1).get().await()
        return snapshot.documents
    }


    var clickListener = object: ClickListener {
        override fun onClickTravel(view: View, travel: Travel) {
            CoroutineScope(Dispatchers.IO).launch {
                listTravelDB = _room.travelDao().getAll() as ArrayList
                setStatus(travel.id,listTravelDB)
                listTravelDB = _room.travelDao().getAll() as ArrayList
                activity?.runOnUiThread{
                    updateAdapter(listTravelDB)
                }
            }
        }
    }

    suspend fun setStatus(id : Int, listTravelDB : ArrayList<Travel>){
        for (i in listTravelDB){
            i.status = false
            _room.travelDao().update(i)
        }
        var travelDB = _room.travelDao().getById(id)
        travelDB.status = true
        _room.travelDao().update(travelDB)

    }

    private fun initRecyclerView(view : View) {
        adapter = TravelAdapter(listTravelDB)
        adapter.setClickListener(clickListener)
        var rv = view.findViewById<RecyclerView>(R.id.rvNotificacions)
        rv.layoutManager = LinearLayoutManager(view.context)
        rv.adapter = adapter
    }

    private fun updateAdapter(listTravelDB : ArrayList<Travel>){
        HelperApi.showLog("FILL "+listTravelDB.toString())
        adapter = TravelAdapter(listTravelDB)
        adapter.setClickListener(clickListener)
        var rv = binding!!.rvNotificacions
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

    }


//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun findTravel(code : String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val _res: Response<*>
//            _res = ApiObject.getRetro().findTravel(code)
//            activity?.runOnUiThread{
//                if(_res.isSuccessful){
//                    val _response : FindByCode? = HelperApi.findByCode(_res.body()!! as List<*>)
//                    if(_response != null){
//                        if(_response.points != null)
//                            fillNotification(_response.points!!)
//                        else
//                            Toast(context).showCustomToast(getString(R.string.no_points), requireActivity())
//                    } else {
//                        Toast(context).showCustomToast(getString(R.string.code_no_available), requireActivity())
//                    }
//                }else {
//                    Toast(context).showCustomToast(getString(R.string.code_no_available), requireActivity())
//                }
//            }
//        }
//    }


}
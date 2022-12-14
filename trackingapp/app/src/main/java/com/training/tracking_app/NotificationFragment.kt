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
import com.gun0912.tedpermission.provider.TedPermissionProvider.context
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

        binding!!.btnAll.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {
                listTravelDB.clear()
                listTravelDB = _room.travelDao().getAll() as ArrayList
                activity?.runOnUiThread{
                    if(listTravelDB.size > 0)
                        updateAdapter(listTravelDB)
                    else
                        Toast(context).showCustomToast(getString(R.string.no_db_travel), requireActivity())
                }
            }
        }

        binding!!.btnFind.setOnClickListener{
            if(!binding!!.etCode.text.toString().equals("")){
                CoroutineScope(Dispatchers.IO).launch {
                    var codeInput = binding!!.etCode.text.toString().trim()
                    val codeExists =getData(codeInput)
                    listTravelDB.clear()
                    listTravelDB = _room.travelDao().getAll() as ArrayList
                    activity?.runOnUiThread{
                        if(codeExists){
                            updateAdapter(listTravelDB)
                        } else {
                            Toast(context).showCustomToast(getString(R.string.check_status_travel), requireActivity())
                        }
                    }
                }
            } else {
                Toast(context).showCustomToast(getString(R.string.no_points), requireActivity())
            }
        }
        return binding!!.root
    }

    suspend fun getData(code : String) : Boolean{
        var res = false;
        val travelDB = _room.travelDao().getByCode(code)
        if(travelDB == null){
            val documentTravel = getTravel(code)
            if(documentTravel.size > 0) {
                val fbData = documentTravel[0]
                val travelFB = fbData.toObject(TravelDto::class.java)!!
                val newTravel = Travel(id=0, idTravel = fbData.id,code = travelFB.code, status = false)
                _room.travelDao().insert(newTravel)
                res = true
            }
        } else {
            res = true
        }
        return res
    }

    private suspend fun getTravel(code : String) : List<DocumentSnapshot>{
        val snapshot = db.collection("travel").whereEqualTo("status",true).whereEqualTo("code",code).limit(1).get().await()
        return snapshot.documents
    }

    var clickListener = object: ClickListener {
        override fun onClickTravel(view: View, travel: Travel) {
            CoroutineScope(Dispatchers.IO).launch {
                listTravelDB.clear()
                listTravelDB = _room.travelDao().getAll() as ArrayList
                setStatus(travel.id,listTravelDB)
                listTravelDB.clear()
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
        travelDB!!.status = true
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
        adapter = TravelAdapter(listTravelDB)
        adapter.setClickListener(clickListener)
        var rv = binding!!.rvNotificacions
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

    }

}
package com.training.tracking_app

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.icu.text.Normalizer.NO
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.training.tracking_app.DtoFirestore.*
import com.training.tracking_app.data.RoomApp
import com.training.tracking_app.data.TravelDb
import com.training.tracking_app.data.dao.TravelDao
import com.training.tracking_app.databinding.FragmentGpsBinding
import com.training.tracking_app.databinding.FragmentTrackBinding
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.helper.showCustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class TrackFragment : Fragment() {

    var code : String? = null
    private lateinit var osmView : MapView
    private lateinit var mapController : MapController

    private val db = Firebase.firestore
    lateinit var _room : TravelDb
    private val cbba = GeoPoint(-17.4140, -66.1653)
    private var _travelMain = TravelDto()
    private var _routeMain = RouteDto()
    private var roadTravel = ArrayList<TrackMarkerDto>()

    private var _binding : FragmentTrackBinding? = null
    private val binding get() = _binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onResume() {
        super.onResume()
        osmView.onResume()
    }

    override fun onPause() {
        super.onPause()
        osmView.onPause()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTrackBinding.inflate(inflater, container, false)

        val ctx: Context = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        _room = Room.databaseBuilder(container!!.context,TravelDb::class.java,"Track").build()

        val btnSheet = binding!!.btnSheetBottom

        btnSheet.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val menu = inflater.inflate(R.layout.bottom_sheet_dialog, null)
            val ivClose = menu.findViewById<ImageView>(R.id.ivClose)
            val code = menu.findViewById<EditText>(R.id.etCode)
            val btnFind = menu.findViewById<Button>(R.id.btnFind)
            val btnUpdate = menu.findViewById<Button>(R.id.btnUpdate)
            ivClose.setOnClickListener {
                dialog.dismiss()
            }
           btnUpdate.setOnClickListener {
                if(code.text.toString() != ""){
                    CoroutineScope(Dispatchers.IO).launch {
                        getData(binding!!.tvTitle.text.toString().trim())
                        dialog.dismiss()
                    }
                } else {
                    Toast(requireContext()).showCustomToast(getString(R.string.need_code), requireActivity())
                }
            }
            btnFind.setOnClickListener {
                if(code.text.toString() != ""){
                    CoroutineScope(Dispatchers.IO).launch {
                        getData(code.text.toString())
                        dialog.dismiss()
                    }
                } else {
                    Toast(requireContext()).showCustomToast(getString(R.string.need_code), requireActivity())
                }
            }
            dialog.setContentView(menu)
            dialog.show()
        }

        // <-- UPDATE -->
        binding!!.btnUpdate.setOnClickListener{
            init()
            CoroutineScope(Dispatchers.IO).launch {
                val travelActive = _room.travelDao().getActiveTravel()
                if(travelActive != null) {
                    getData(travelActive.code)
                } else {
                    activity?.runOnUiThread{
                        Toast(requireContext()).showCustomToast(getString(R.string.check_status_travel), requireActivity())
                    }
                }
            }
        }
        init()
        return binding!!.root
    }

    private fun init(){
        mapConfig()
        myLocation()
        drawCompass()
        rotationGesture()
    }

    private fun mapConfig(){
        osmView = binding!!.osmView
        osmView.setTileSource(TileSourceFactory.MAPNIK)
        mapController = osmView.controller as MapController
        mapController.setCenter(cbba)
        mapController.setZoom(15)
        osmView.setMultiTouchControls(true)
    }

    private fun myLocation(){
        val dm = this.resources.displayMetrics
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), osmView)
        locationOverlay.enableMyLocation()
        osmView.overlays.add(locationOverlay)
    }

    private fun drawCompass(){
        val internalCompass = InternalCompassOrientationProvider(context)
        val compass = CompassOverlay(context, internalCompass, osmView)
        compass.enableCompass()
        osmView.overlays.add(compass)
    }

    private fun rotationGesture(){
        val rotationGesture = RotationGestureOverlay(osmView)
        rotationGesture.isEnabled
        osmView.setMultiTouchControls(true)
        osmView.overlays.add(rotationGesture)
    }

    private fun miniMap(){
        val dm = this.resources.displayMetrics
        val miniMap = MinimapOverlay(context, osmView.tileRequestCompleteHandler)
        miniMap.width = dm.widthPixels / 5
        miniMap.height = dm.heightPixels / 5
        osmView.overlays.add(miniMap)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getData(code : String){

        var _destination: DestinationDto
        val documentsNotif = ArrayList<NotificationDto>()
        val notifiticationPoints = ArrayList<TrackMarkerDto>()
        var travelId = ""

        try {
            val documentTravel = getTravel(code)[0]
            if(documentTravel != null){

                roadTravel.clear()
                travelId = documentTravel.id
                _travelMain = documentTravel.toObject(TravelDto::class.java)!!

                _routeMain = getRoute(_travelMain.route).toObject(RouteDto::class.java)!!
                _destination = getGeoPointFs(_routeMain.to).toObject(DestinationDto::class.java)!!
                val iniTravel = TrackMarkerDto(GeoPoint(_destination.coordinates.latitude, _destination.coordinates.longitude),
                    R.mipmap.end_point,
                    "INIT TRAVEL",
                    "START TO ${HelperApi.formatDateView(_travelMain.hora)}",
                    0,
                    false,
                    true
                )
                roadTravel.add(iniTravel)

                _destination = getGeoPointFs(_routeMain.from).toObject(DestinationDto::class.java)!!
                val endTravel = TrackMarkerDto(GeoPoint(_destination.coordinates.latitude, _destination.coordinates.longitude),
                    R.mipmap.start_truck,
                    "END TRAVEL",
                    "END TO: ${HelperApi.formatDateView(_travelMain.llegada)}",
                    0,
                    true,
                    false
                )
                roadTravel.add(endTravel)
                drawRoadTravel(roadTravel)

                for(doc in getNotifications(travelId)){
                    val _notificationDto = doc.toObject(NotificationDto::class.java)!!
                    documentsNotif.add(_notificationDto)
                    notifiticationPoints.add(
                        TrackMarkerDto(
                            GeoPoint(_notificationDto.coordinates.latitude,_notificationDto.coordinates.longitude),
                            getIconByType(_notificationDto.color),
                            _notificationDto.title,
                            _notificationDto.description,
                            _notificationDto.order,
                            end = false,
                            start = false
                        )
                    )
                }
                drawNotif(notifiticationPoints)
            } else {
                Toast(context).showCustomToast(getString(R.string.travel_not_ready), requireActivity())
            }
        }catch(e : Exception){
            HelperApi.showLog(e.message.toString())
        }
    }

    private fun getIconByType(type : String) : Int {
        when(type){
            "info" -> return R.mipmap.info_marker
            "error" -> return R.mipmap.error_marker
            "warning" -> return R.mipmap.warn_marker
        }
        return R.mipmap.basic_marker
    }

    private suspend fun getTravel(code : String) : List<DocumentSnapshot>{
        val snapshot = db.collection("travel").whereEqualTo("status",true).whereEqualTo("code",code).limit(1).get().await()
        return snapshot.documents
    }

    private suspend fun getRoute(code: String): DocumentSnapshot {
        return db.collection("route").document(code).get().await()
    }

    private suspend fun getGeoPointFs(id: String): DocumentSnapshot {
        return db.collection("destination").document(id).get().await()
    }

    private suspend fun getNotifications(idTravel:String) : List<DocumentSnapshot>{
        val snapshot = db.collection("notification").whereEqualTo("travel",idTravel).get().await()
        return snapshot.documents
    }

    private fun drawRoadTravel(list : ArrayList<TrackMarkerDto>){
        if(list != null){
            val geoPointList = ArrayList<GeoPoint>()
            for(i in list){
                geoPointList.add(i.point)
                drawMarker(i)
            }
            drawRoad(geoPointList, R.color.app_secondary)
            osmView.invalidate()
        }
    }

    private fun drawMarker(data : TrackMarkerDto){
        val _marker = Marker(osmView)
        _marker.position = data.point
        _marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        _marker.title = data.title
        if(data.icon > 0 )
            _marker.icon = resources.getDrawable(data.icon)
        else
            _marker.icon = resources.getDrawable(R.mipmap.basic_marker)
        _marker.subDescription = data.subDescription
        osmView.invalidate()
        osmView.overlays.add(_marker)
    }

    private fun drawRoad(list : ArrayList<GeoPoint>, color: Int){
        val roadManager: RoadManager = OSRMRoadManager(this.context,"VIAJE")
        val road = roadManager.getRoad(list)
        val roadOverlay = RoadManager.buildRoadOverlay(road)
        roadOverlay.color = resources.getColor(color)
        roadOverlay.width = 16F
        osmView.overlays.add(roadOverlay)
    }

    private fun drawNotif( listPoint : ArrayList<TrackMarkerDto>?) {
        if (listPoint != null) {
            for ( element in listPoint) {
                val nodeMarker = Marker(osmView)
                nodeMarker.position = element.point
                nodeMarker.title = element.title
                nodeMarker.image = resources.getDrawable(R.mipmap.icon_notif)
                nodeMarker.icon = resources.getDrawable(element.icon)
                nodeMarker.subDescription = element.subDescription
                osmView.getOverlays().add(nodeMarker)
            }
        }
        osmView.invalidate()
    }

    private fun clearMap(){
        osmView.overlays.forEach {
            osmView.overlays.remove(it)
        }
    }

}
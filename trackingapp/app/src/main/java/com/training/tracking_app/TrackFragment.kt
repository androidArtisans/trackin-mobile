package com.training.tracking_app

import android.content.Context
import android.graphics.Bitmap
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.training.tracking_app.DtoFirestore.DestinationDto
import com.training.tracking_app.DtoFirestore.NotificationDto
import com.training.tracking_app.DtoFirestore.RouteDto
import com.training.tracking_app.DtoFirestore.TravelDto
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.helper.showCustomToast
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay


class TrackFragment : Fragment() {

    private val DEFAULT_ZOOM = 30




    private val db = Firebase.firestore
    private val cbba = GeoPoint(-17.4140, -66.1653)


    private var _travelMain = TravelDto()
    private var _mainId = ""

    private var travelPoints = ArrayList<GeoPoint>()

    private var notificationPoints = ArrayList<NotificationDto>()

    var code : String? = null
    private lateinit var _view : View
    private lateinit var osmView : MapView

    private lateinit var mapController : MapController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx: Context = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        _view = inflater.inflate(R.layout.fragment_track, container, false)
        val btnSheet = _view.findViewById<ImageView>(R.id.btnSheetBottom)

        btnSheet.setOnClickListener {
            val dialog = BottomSheetDialog(_view.context)
            val menu = inflater.inflate(R.layout.bottom_sheet_dialog, null)
            val ivClose = menu.findViewById<ImageView>(R.id.ivClose)
            val code = menu.findViewById<EditText>(R.id.etCode)
            val btnFind = menu.findViewById<Button>(R.id.btnFind)
            val btnUpdate = menu.findViewById<Button>(R.id.btnUpdate)
            ivClose.setOnClickListener {
                dialog.dismiss()
            }
           /* btnUpdate.setOnClickListener {
                if(code.text.toString() != ""){
                    findTravel(code.text.toString(), dialog)
                } else {
                    Toast(_view.context).showCustomToast(getString(R.string.need_code), requireActivity())
                }
            }*/
            btnFind.setOnClickListener {
                if(code.text.toString() != ""){
                    findTravel("0001")
                } else {
                    Toast(_view.context).showCustomToast(getString(R.string.need_code), requireActivity())
                }
            }

            //dialog.setCancelable(false)
            dialog.setContentView(menu)
            dialog.show()
        }
        init()
        return _view
    }

    private fun init(){
        //Toast(_view.context).showCustomToast(getString(R.string.search_box), requireActivity())
        mapConfig()
        drawCompass()
    }

    private fun findTravel(code : String){
        clearMap()
        /* GET TRAVEL DATA */
         db.collection("travel").whereEqualTo("code",code)
         .limit(1).get()
        .addOnSuccessListener { travelList ->
            for (t in travelList){
                _mainId = t.id.toString()
                _travelMain = (t.toObject(TravelDto::class.java))
            }
            findRoute("")
            getNotifications()
        }
        /* END TRAVEL DATA */
    }

    private fun getNotifications(){
        HelperApi.showLog("ID "+_mainId)
        db
        .collection("notification")
        .whereEqualTo("travel", _mainId)
        .addSnapshotListener{snapshots, _ ->
            notificationPoints.clear()
            for(dc in snapshots!!.documentChanges){
                val notif = dc.document.toObject(NotificationDto::class.java)

                notificationPoints.add(notif)
            }
            drawNotif(notificationPoints)
        }
    }

    private fun findRoute(idRoute : String){
        db
        .collection("route")
        .document(_travelMain.route)
        .get()
        .addOnSuccessListener { route ->
            val r = route.toObject(RouteDto::class.java)
            if(route != null){
                    getPoint(r!!.to, true)
                    getPoint(r.from, false)
                }
        }
    }

    private fun getPoint(idDestination : String, isStart : Boolean){
        var res = GeoPoint(0.0,0.0)
        db
        .collection("destination")
        .document(idDestination)
        .addSnapshotListener{ snapshots, _ ->
            if(snapshots != null){
                val destination = snapshots.toObject(DestinationDto::class.java)
                res = GeoPoint(destination!!.coordinates.latitude, destination.coordinates.longitude)
                travelPoints.add(res)
            }
            drawRoute(travelPoints)
        }
    }

    private fun mapConfig(){
        osmView = _view.findViewById(R.id.osmView)
        osmView.setTileSource(TileSourceFactory.MAPNIK)
        mapController = osmView.controller as MapController
        mapController.setCenter(cbba)
        mapController.setZoom(15)
        osmView.setMultiTouchControls(true)
    }

    private fun drawCompass(){
        val internalCompass = InternalCompassOrientationProvider(_view.context)
        val compass = CompassOverlay(_view.context, internalCompass, osmView)
        compass.enableCompass()
        osmView.overlays.add(compass)
    }

    private fun drawRoute( listPoint : ArrayList<GeoPoint>?) {
        var roadManager: RoadManager = OSRMRoadManager(_view.context,"MAPA")
        val road = roadManager.getRoad(listPoint)
        val roadOverlay = RoadManager.buildRoadOverlay(road)
        roadOverlay.color = resources.getColor(R.color.app_primary)
        roadOverlay.width = 16F
        osmView.overlays.add(roadOverlay)
        osmView.invalidate()
    }

    private fun drawNotif( listPoint : ArrayList<NotificationDto>?) {

        if (listPoint != null) {
            val icon = resources.getDrawable(R.drawable.marker)
            for (i in listPoint) {
                HelperApi.showLog("NODO " + i.toString())
                val nodeMarker = Marker(osmView)
                nodeMarker.position = GeoPoint(i.coordinates.latitude, i.coordinates.longitude)
                nodeMarker.title = i.title
                nodeMarker.image = icon
                nodeMarker.snippet = i.color
                nodeMarker.subDescription = i.description

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

    private fun drawMark(point: GeoPoint){
        val markerMe = Marker(osmView)
        markerMe.position = point
        markerMe.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        osmView.invalidate()
        osmView.overlays.add(markerMe)
    }

    private fun rotationGesture(){
        val rotationGesture = RotationGestureOverlay(osmView)
        rotationGesture.isEnabled
        osmView.setMultiTouchControls(true)
        osmView.overlays.add(rotationGesture)
    }

    private fun miniMap(){
        val dm = this.resources.displayMetrics
        val miniMap = MinimapOverlay(_view.context, osmView.tileRequestCompleteHandler)
        miniMap.width = dm.widthPixels / 5
        miniMap.height = dm.heightPixels / 5
        osmView.overlays.add(miniMap)
    }
}
package com.training.tracking_app

import android.content.Context
import android.graphics.Color
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
import com.training.tracking_app.helper.showCustomToast
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay

class TrackFragment : Fragment() {

    private val DEFAULT_ZOOM = 30

    private var managerRoad =

    private val db = Firebase.firestore
    private val cbba = GeoPoint(-17.4140, -66.1653)

    private var travelPoints = ArrayList<GeoPoint>()
    private var notificationPoints = ArrayList<GeoPoint>()

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
                    findTravel(code.text.toString(), dialog)
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
        Toast(_view.context).showCustomToast(getString(R.string.search_box), requireActivity())
        mapConfig()
        drawCompass()
        osmView.invalidate()
    }

    private fun findTravel(code : String, dialog : BottomSheetDialog) {
        clearMap()
        val tvTitle = _view.findViewById<TextView>(R.id.tvTitle)
        /* GET TRAVEL DATA */
        db
        .collection("travel")
        .whereEqualTo("code", code)
        .limit(1)
        .addSnapshotListener{snapshots, _ ->
            val _idTravel = snapshots!!.documents[0].id.toString()
            val travel = snapshots.documents[0].toObject(TravelDto::class.java)
            tvTitle.text = travel!!.code
            findRoute(travel.route)
            getNotifications(_idTravel)
            drawRoute(notificationPoints)
            dialog.dismiss()
        }
        /* END TRAVEL DATA */
    }

    private fun getNotifications(idTravel : String){
        db
        .collection("notification")
        .whereEqualTo("travel", idTravel)
        .addSnapshotListener{snapshots, _ ->
            for(dc in snapshots!!.documentChanges){
                val notif = dc.document.toObject(NotificationDto::class.java)
                notificationPoints.add(GeoPoint(notif.coordinates.latitude, notif.coordinates.longitude))
            }
        }
    }

    private fun findRoute(idRoute : String){
        db
        .collection("route")
        .document(idRoute)
        .addSnapshotListener{ snapshots, _ ->
            if(snapshots!= null){
                val route = snapshots.toObject(RouteDto::class.java)
                if(route != null){
                    getPoint(route.to, true)
                    getPoint(route.from, false)
                    drawRoute(travelPoints)
                }
            }
        }

    }

    private fun getPoint(idDestination : String, isStart : Boolean) {
        var res = GeoPoint(0.0,0.0)
        db
        .collection("destination")
        .document(idDestination)
        .addSnapshotListener { snapshots, _ ->
            if(snapshots != null){
                val destination = snapshots.toObject(DestinationDto::class.java)
                res = GeoPoint(destination!!.coordinates.latitude, destination.coordinates.longitude)
                travelPoints.add(res)
                if(isStart) {
                    osmView.controller.setCenter(res)
                }
            }
        }
    }

    private fun mapConfig(){
        osmView = _view.findViewById(R.id.osmView)
        osmView.setTileSource(TileSourceFactory.MAPNIK)
        mapController = osmView.controller as MapController
        mapController.setCenter(cbba)
        mapController.setZoom(DEFAULT_ZOOM)
        osmView.setMultiTouchControls(true)
    }

    private fun drawCompass(){
        val internalCompass = InternalCompassOrientationProvider(_view.context)
        val compass = CompassOverlay(_view.context, internalCompass, osmView)
        compass.enableCompass()
        osmView.overlays.add(compass)
    }

    private fun drawRoute( listPoint : ArrayList<GeoPoint>?) {
        for ( p in listPoint!!){
            drawMark(p)
        }
        val line = Polyline()
        line.points = listPoint
        line.color = Color.parseColor("#FB2E50")
        line.isGeodesic = true
        osmView.overlays.add(line)

    }

    private fun clearMap(){
        osmView.overlays.forEach {
            osmView.overlays.remove(it)
        }
    }

    private fun drawMark(point: GeoPoint){
        val markerMe = Marker(osmView)
        markerMe.position = point
        //markerMe.setIcon(_view.context.resources.getDrawable(R.drawable.marker))
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
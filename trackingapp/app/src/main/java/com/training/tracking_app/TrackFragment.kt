package com.training.tracking_app

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.training.tracking_app.DtoLaravel.FindByCode
import com.training.tracking_app.DtoLaravel.Trackin
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.helper.showCustomToast
import com.training.tracking_app.network.response.api.ApiObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
import retrofit2.Response

class TrackFragment : Fragment() {

    val ZOOM = 30

    val cbba = GeoPoint(-17.4140, -66.1653)
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
        var btnSheet = _view.findViewById<ImageView>(R.id.btnSheetBottom)

        btnSheet.setOnClickListener {
            val dialog = BottomSheetDialog(_view.context)
            val menu = inflater.inflate(R.layout.bottom_sheet_dialog, null)
            val ivClose = menu.findViewById<ImageView>(R.id.ivClose)
            var code = menu.findViewById<EditText>(R.id.etCode)
            val btnFind = menu.findViewById<Button>(R.id.btnFind)
            val btnUpdate = menu.findViewById<Button>(R.id.btnUpdate)
            ivClose.setOnClickListener {
                dialog.dismiss()
            }
            btnUpdate.setOnClickListener {
                if(!code.text.toString().equals("")){
                    findTravel(code.text.toString(), dialog)
                } else {
                    Toast(_view.context).showCustomToast(getString(R.string.need_code), requireActivity())
                }
            }
            btnFind.setOnClickListener {
                if(!code.text.toString().equals("")){
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

    override fun onResume() {
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun findTravel(code : String, dialog : BottomSheetDialog) {
        clearMap()
        CoroutineScope(Dispatchers.IO).launch {
            val _res: Response<*>
            _res = ApiObject.getRetro().findTravel(code)
            val _response : FindByCode? = HelperApi.findByCode(_res.body()!! as List<*>)
            activity?.runOnUiThread{
                if(_res.isSuccessful){
                    if(_response != null){
                        if(_response.points != null)
                            drawRoute(_response.points)
                        else
                            Toast(_view.context).showCustomToast(getString(R.string.no_points), requireActivity())
                        dialog.dismiss()
                    } else {
                        Toast(_view.context).showCustomToast(getString(R.string.code_no_available), requireActivity())
                    }
                }
            }
        }
    }

    private fun init(){
        mapConfig()
        drawCompass()
        //drawRoute(_listPoint)
    }

    private fun mapConfig(){
        osmView = _view.findViewById(R.id.osmView)
        osmView.setTileSource(TileSourceFactory.MAPNIK)
        mapController = osmView.controller as MapController
        mapController.setCenter(cbba)
        mapController.setZoom(ZOOM)
        osmView.setMultiTouchControls(true)
    }

    private fun drawCompass(){
        val internalCompass = InternalCompassOrientationProvider(_view.context)
        val compass = CompassOverlay(_view.context, internalCompass, osmView)
        compass.enableCompass()
        osmView.overlays.add(compass)
    }

    private fun drawRoute( listPoint : ArrayList<Trackin>?) {
        val list = ArrayList<GeoPoint>()
        for ( p : Trackin in listPoint!!){
            val _actual = GeoPoint(p.latitude.toDouble(), p.longitude.toDouble())
            list.add(_actual)
            drawMark(_actual)
        }
        Log.d("TAG", list.toString())
        val line = Polyline()
        line.points = list
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
        markerMe.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
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
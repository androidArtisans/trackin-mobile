package com.training.tracking_app

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.training.tracking_app.Dto.PointDto
import com.training.tracking_app.DtoLaravel.FindByCode
import com.training.tracking_app.DtoLaravel.Trackin
import com.training.tracking_app.helper.HelperApi
import com.training.tracking_app.network.response.TravelResponse
import com.training.tracking_app.network.response.api.ApiObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.osmdroid.config.Configuration
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

    var code : String? = null

    lateinit var _view : View

    val cbba = GeoPoint(-17.4140, -66.1653)
    private lateinit var osmView : MapView
    private lateinit var mapController : MapController

    val _listPoint = arrayListOf<PointDto>(
        PointDto("-17.3978832","-66.1530828", "",""),
        PointDto("-17.397788", "-66.151195","",""),
        PointDto("-17.4150","-66.1663","",""),
        PointDto("-17.4155","-66.1668","","")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val ctx: Context = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _view = inflater.inflate(R.layout.fragment_track, container, false)
        var btnSheet = _view.findViewById<ImageView>(R.id.btnSheetBottom)
        btnSheet.setOnClickListener {
            val dialog = BottomSheetDialog(_view.context)
            val menu = inflater.inflate(R.layout.bottom_sheet_dialog, null)
            val ivClose = menu.findViewById<ImageView>(R.id.ivClose)
            var code = menu.findViewById<EditText>(R.id.etCode)
            val btnFind = menu.findViewById<Button>(R.id.btnFind)
            ivClose.setOnClickListener {
                dialog.dismiss()
            }
            btnFind.setOnClickListener {
                if(!code.text.toString().equals("")){
                    val bundle = Bundle()
                    bundle.putString("code", code.text.toString())
                    findTravel(code.text.toString(), dialog)
                } else {
                    Toast.makeText(_view.context, "DEBE INGRESAR UN CODIGO", Toast.LENGTH_SHORT).show()
                }
            }
            //dialog.setCancelable(false)
            dialog.setContentView(menu)
            dialog.show()
        }
        init()
        return _view
    }

    private fun findTravel(code : String, dialog : BottomSheetDialog) {
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
                            Toast.makeText(_view.context, "Su viaje ya se encuentra en progreso, pero no se tiene ningun track", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(_view.context, "Su viaje aun no se encuentra disponible", Toast.LENGTH_SHORT).show()
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
        osmView = _view.findViewById<MapView>(R.id.osmView)
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
package com.flowz.locationtrackingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.flowz.locationtrackingapp.ui.LocationViewModel
import com.flowz.locationtrackingapp.util.GpsUtils
import com.flowz.locationtrackingapp.util.GpsUtils.Companion.GPS_REQUEST
import com.flowz.locationtrackingapp.util.GpsUtils.Companion.LOCATION_REQUEST
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var mapView: MapView
    private var isGPSEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initGoogleMap(savedInstanceState)


        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)

        GpsUtils(this).turnGPSOn(object : GpsUtils.OnGpsListener{

            override fun gpsStatus(isGPSEnable: Boolean) {
                this@MainActivity.isGPSEnabled = isGPSEnabled
            }
        })


    }

    private fun initGoogleMap(savedInstanceState: Bundle?){

        mapView = map_view

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mapView.onCreate(mapViewBundle)

        mapView.getMapAsync(this)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                isGPSEnabled = true
                invokeLocationAction()
            }
        }
    }

    private fun invokeLocationAction() {
        when{
            !isGPSEnabled -> lat_long.text = getString(R.string.enable_gps)

            isPermissionsGranted() -> startLocationUpdate()

            shouldShowRequestPermissionRationale() -> lat_long.text = getString(R.string.grant_permission)

            else-> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_REQUEST)
        }

    }

    private fun startLocationUpdate(){
        locationViewModel.fetchLocationData().observe(this, Observer {

            lat_long.text = getString(R.string.current_location) + (it.longitude ).toString()+ (it.latitude).toString()
        })
    }

    private fun isPermissionsGranted() =
            ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRequestPermissionRationale() =
            ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            )

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                invokeLocationAction()
            }
        }
    }

    companion object{
        const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onMapReady(googleMap: GoogleMap) {

//        googleMap.addMarker(MarkerOptions().position(StartLocation).title("StartLocation"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(StartLocation))
//        googleMap.addPolyline(PolylineOptions()
//                .clickable(true)
//                .add(StartLocation)
//                .add(StopLocation)
//                .width(8f)
//                .color(resources.getColor(R.color.colorAccent))
//        )
//        googleMap.addCircle(CircleOptions()
//                .center(StopLocation)
//                .radius(50000.0)
//                .strokeWidth(3f)
//                .strokeColor(resources.getColor(R.color.colorAccent))
//                .fillColor(resources.getColor(R.color.colorAccent)))

        locationViewModel.fetchLocationData().observe(this, Observer {

            googleMap.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))

        })


//
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return
        }
        googleMap.isMyLocationEnabled = true
        Toast.makeText(this, getString(R.string.current), Toast.LENGTH_LONG).show()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        invokeLocationAction()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }


    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}


}
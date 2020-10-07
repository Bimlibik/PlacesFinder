package com.foxy.testproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.here.android.mpa.common.Image
import com.here.android.mpa.common.*
import com.here.android.mpa.common.PositioningManager.*
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapMarker
import com.here.android.mpa.search.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference
import java.util.*


class MainActivity : AppCompatActivity(), OnPositionChangedListener {

    private var map: Map? = null
    private lateinit var mapFragment: AndroidXMapFragment
    private lateinit var positionManager: PositioningManager
    private lateinit var hereDataSource: LocationDataSourceHERE

    private lateinit var currentGeoCoordinate: GeoCoordinate

    private val REQUEST_CODE_ASK_PERMISSIONS = 1
    private val REQUIRED_SDK_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
    }

    private fun initialize() {
        Log.i("TAG2", "initialize: ")
        setContentView(R.layout.activity_main)
        btn_locate.setOnClickListener { locate() }
        btn_cinema.setOnClickListener { search() }

        mapFragment =
            (supportFragmentManager.findFragmentById(R.id.map_fragment) as AndroidXMapFragment?)!!

        MapSettings.setDiskCacheRootPath(
            "${applicationContext.getExternalFilesDir(null)}${File.separator}.here-maps"
        )

        mapFragment.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                map = mapFragment.map
                map?.let {
                    it.setCenter(GeoCoordinate(0.0, 0.0, 0.0), Map.Animation.NONE)
//                    it.zoomLevel = (it.maxZoomLevel + it.minZoomLevel) / 2
                    it.zoomLevel = it.minZoomLevel
                    it.projectionMode = Map.Projection.MERCATOR
                }
            } else {
                println("Error: Cannot initialize Map Fragment")
            }
        }

        if (MapEngine.isInitialized()) {
            mapFragment.positionIndicator?.isVisible = true
        }

        hereDataSource = LocationDataSourceHERE.getInstance()

    }

    override fun onPositionUpdated(
        locationMethod: LocationMethod?,
        geoPosition: GeoPosition?,
        mapMatched: Boolean
    ) {
        map?.let { map ->
            geoPosition?.let { position ->
                map.setCenter(position.coordinate, Map.Animation.NONE)
                map.zoomLevel = (map.maxZoomLevel + map.minZoomLevel) / 2
                positionManager.stop()
                currentGeoCoordinate = position.coordinate
                Log.i("TAG2", "onPositionUpdated: ${position.coordinate}")
            }
        }
    }

    override fun onPositionFixChanged(p0: LocationMethod?, p1: LocationStatus?) {
//        TODO("TODO")
    }

    override fun onResume() {
        super.onResume()
        if (this::positionManager.isInitialized && MapEngine.isInitialized()) {
            positionManager.start(LocationMethod.GPS_NETWORK)
        }
    }

    override fun onPause() {
        if (this::positionManager.isInitialized && MapEngine.isInitialized()) {
            positionManager.stop()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (this::positionManager.isInitialized && MapEngine.isInitialized()) {
            positionManager.removeListener(this)
        }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                for ((i, permission) in permissions.withIndex()) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(
                            this, "Required permission $permission not granted",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                }
            }
        }
        initialize()
    }

    private fun locate() {
        if (this::hereDataSource.isInitialized && MapEngine.isInitialized()) {
            if (this::positionManager.isInitialized) {
                positionManager.start(LocationMethod.GPS_NETWORK)
            } else {
                positionManager = getInstance()
                positionManager.dataSource = hereDataSource
                positionManager.addListener(WeakReference(this))
                mapFragment.positionIndicator?.isVisible = true
            }
            if (positionManager.start(LocationMethod.GPS_NETWORK_INDOOR)) {
                // Position updates started successfully
            }
        }
    }

    private fun search() {
        Log.i("TAG2", "on search click")
        val searchRequest = SearchRequest("Cinema").setSearchCenter(map?.center!!)
        searchRequest.execute { discoveryResultPage, errorCode ->
            Log.i("TAG2", "search: discovery")
            if (errorCode == ErrorCode.NONE) {
                val items = discoveryResultPage?.items!!
                addMarkerAtPlace(items as List<PlaceLink>)
//                for (item in items) {
//                    Log.i("TAG2", "size = ${items.size}, item = ${item.title}")
//                    if (item.resultType == DiscoveryResult.ResultType.PLACE) {
//                        val placeLink: PlaceLink = item as PlaceLink
//                        Log.i("TAG2", "item position = ${placeLink.position}")
//                        addMarkerAtPlace(placeLink)
//                    }
//                }
            } else {
                Toast.makeText(this, "Error $errorCode", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun addMarkerAtPlace(items: List<PlaceLink>) {
        val img = Image().apply { setImageResource(R.drawable.marker) }

        Log.i("TAG2", "img is valid = ${img.isValid}, type = ${img.type}")
        for (item in items) {
            Log.i("TAG2", "size = ${items.size}, item = ${item.title}")
            if (item.resultType == DiscoveryResult.ResultType.PLACE) {
                Log.i("TAG2", "item position = ${item.position}")
                Log.i("TAG2", "map1 = ${map == null}")
                val marker = MapMarker()
                marker.coordinate = item.position!!
                marker.icon = img

                map?.let {
                    Log.i("TAG2", "map2")
                    it.addMapObject(marker)
                }
            }
        }
    }

//    private fun addMarkerAtPlace(placeLink: PlaceLink) {
//        val image = Image()
//            image.setImageResource(R.drawable.ic_location)
////        try {
////            Log.i("TAG2", "img")
////            image.setImageResource(R.drawable.ic_location)
////        } catch (e: IOException) {
////            Log.i("TAG2", "img error")
////            e.printStackTrace()
////        }
//
//        val marker = MapMarker(GeoCoordinate(placeLink.position!!), image)
////        marker.icon = image
////        marker.coordinate = GeoCoordinate(placeLink.position!!)
//        map?.addMapObject(marker)
//    }

    private fun checkPermissions() {
        val missingPermissions = mutableListOf<String>()
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }

        if (missingPermissions.isNotEmpty()) {
            val permissions = missingPermissions.toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS)
        } else {
            val grantResults = IntArray(REQUIRED_SDK_PERMISSIONS.size)
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS, grantResults)
        }
    }

//    private val discoveryResultPageListener = ResultListener<DiscoveryResultPage> { discoveryResultPage, errorCode ->
//        Log.i("TAG2", "search: discovery")
//        if (errorCode == ErrorCode.NONE) {
//            val items = discoveryResultPage?.items!!
//            for (item in items) {
//                Log.i("TAG2", "size = ${items.size}, item = ${item.title}")
//                if (item.resultType == DiscoveryResult.ResultType.PLACE) {
//                    val placeLink: PlaceLink = item as PlaceLink
//                    addMarkerAtPlace(placeLink)
//                }
//            }
//        } else {
//            Toast.makeText(this, "Error $errorCode", Toast.LENGTH_LONG).show()
//        }
//
//    }

}
package com.foxy.testproject.mvp

import android.util.Log
import com.foxy.testproject.R
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapMarker
import com.here.android.mpa.mapping.MapObject
import com.here.android.mpa.search.*
import moxy.InjectViewState
import moxy.MvpPresenter
import java.lang.ref.WeakReference

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {

    private lateinit var map: Map
    private lateinit var positionManager: PositioningManager
    private lateinit var currentCoordinate: GeoCoordinate
    private var currentZoomLevel: Double = 0.0

    private var mapObjects = mutableListOf<MapObject>()
        .apply { emptyList<MapObject>() }


    fun onEngineInitializationCompleted(error: OnEngineInitListener.Error) {
        if (error == OnEngineInitListener.Error.NONE) {
            initMap()
            viewState.updateMap(map)
        } else {
            error.stackTrace
        }
    }

    fun locate(isInitialized: Boolean) {
        if (isInitialized) {
            positionManager = PositioningManager.getInstance().apply {
                addListener(setupPositioningListener())
                start(PositioningManager.LocationMethod.GPS_NETWORK)
            }
        }
    }

    fun computeResult(discoveryResultPage: DiscoveryResultPage?, errorCode: ErrorCode) {
        if (errorCode == ErrorCode.NONE) {
            if (discoveryResultPage != null) {
                val items = discoveryResultPage.items
                for (item in items) {
                    if (item.resultType == DiscoveryResult.ResultType.PLACE) {
                        val mapObject = createMapObject(item as PlaceLink)
                        map.addMapObject(mapObject)
                        viewState.updateMap(map)
                    }
                }
            } else {
                // Not found
            }
        } else {
            viewState.showError(errorCode)
        }
    }

    fun search(query: String, geoCoordinate: GeoCoordinate) {
        clearMap()
        viewState.executeRequest(query, geoCoordinate)
    }

    private fun createMapObject(item: PlaceLink): MapMarker {
        val img = Image().apply { setImageResource(R.drawable.marker) }
        val mapObject = MapMarker(item.position!!, img)
        mapObjects.add(mapObject)
        return mapObject
    }

    private fun clearMap() {
        if (mapObjects.isNotEmpty()) {
            map.removeAllMapObjects()
            mapObjects.clear()
            viewState.updateMap(map)
        }
    }

    private fun initMap() {
        if (!this::map.isInitialized) {
            map = Map()
            currentCoordinate = GeoCoordinate(0.0, 0.0, 0.0)
        }

        map.apply {
            setCenter(currentCoordinate, Map.Animation.NONE)
            zoomLevel = currentZoomLevel
            projectionMode = Map.Projection.MERCATOR
        }
    }

    private fun setupPositioningListener(): WeakReference<PositioningManager.OnPositionChangedListener> =
        WeakReference(object : PositioningManager.OnPositionChangedListener {
            override fun onPositionUpdated(
                locationMethod: PositioningManager.LocationMethod?,
                geoPosition: GeoPosition?,
                mapMatched: Boolean
            ) {
                Log.i("TAG2", "onPositionUpdated:")
                geoPosition?.let { position ->
                    currentCoordinate = position.coordinate
                    currentZoomLevel = (map.maxZoomLevel + map.minZoomLevel) / 2

                    map.setCenter(currentCoordinate, Map.Animation.NONE)
                    map.zoomLevel = currentZoomLevel
                    map.positionIndicator.isVisible = true
                    viewState.updateMap(map)
                    positionManager.stop()
//                                currentGeoCoordinate = position.coordinate
                    Log.i("TAG2", "onPositionUpdated: ${position.coordinate}")
                }
            }

            override fun onPositionFixChanged(
                p0: PositioningManager.LocationMethod?,
                p1: PositioningManager.LocationStatus?
            ) {
                Log.i("TAG2", "onPositionFixChanged")
            }

        })
}
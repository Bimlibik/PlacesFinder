package com.foxy.testproject.mvp

import com.foxy.testproject.GlobalCategories
import com.foxy.testproject.R
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapMarker
import com.here.android.mpa.mapping.MapObject
import com.here.android.mpa.search.*
import moxy.InjectViewState
import moxy.MvpPresenter
import java.lang.ref.WeakReference
import java.util.*

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {

    private lateinit var map: Map
    private lateinit var positionManager: PositioningManager
    private lateinit var currentCoordinate: GeoCoordinate
    private var currentZoomLevel: Double = 0.0

    private var mapObjects = mutableListOf<MapObject>()
        .apply { emptyList<MapObject>() }


    fun clear() {
        clearMap()
        viewState.clearQuery()
    }

    fun showMoreCategories(globalCategory: GlobalCategories, title: String) {
        val categoriesId = globalCategory.toString().toLowerCase(Locale.ROOT)
        val requestsId = "${categoriesId}_request"
        viewState.openDialog(categoriesId, requestsId, title)
    }

    fun locate(isInitialized: Boolean) {
        if (isInitialized) {
            positionManager = PositioningManager.getInstance().apply {
                addListener(setupPositioningListener())
                start(PositioningManager.LocationMethod.GPS_NETWORK)
            }
        }
    }

    fun searchByCategory(query: String, title: String, geoCoordinate: GeoCoordinate) {
        clearMap()
        viewState.closeDialog()
        viewState.showQuery(title)

        val filter = CategoryFilter().apply { add(query) }
        val exploreRequest = ExploreRequest().apply {
            setSearchCenter(geoCoordinate)
            setCategoryFilter(filter)
        }
        exploreRequest.execute { discoveryResultPage, errorCode ->
            computeResult(discoveryResultPage, errorCode)
        }
    }

    fun searchByKeyword(query: String, geoCoordinate: GeoCoordinate) {
        if (query.isNotEmpty()) {
            clearMap()

            val searchRequest = SearchRequest(query).setSearchCenter(geoCoordinate)
            searchRequest.execute { discoveryResultPage, errorCode ->
                computeResult(discoveryResultPage, errorCode)
            }

            viewState.showQuery(query)
        }
    }

    fun onEngineInitializationCompleted(error: OnEngineInitListener.Error) {
        if (error == OnEngineInitListener.Error.NONE) {
            initMap()
            viewState.updateMap(map)
        } else {
            error.stackTrace
        }
    }

    private fun computeResult(discoveryResultPage: DiscoveryResultPage?, errorCode: ErrorCode) {
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
                geoPosition?.let { position ->
                    currentCoordinate = position.coordinate
                    currentZoomLevel = (map.maxZoomLevel + map.minZoomLevel) / 2

                    map.setCenter(currentCoordinate, Map.Animation.NONE)
                    map.zoomLevel = currentZoomLevel
                    map.positionIndicator.isVisible = true
                    viewState.updateMap(map)
                    positionManager.stop()
                }
            }

            override fun onPositionFixChanged(
                p0: PositioningManager.LocationMethod?,
                p1: PositioningManager.LocationStatus?
            ) {

            }

        })
}
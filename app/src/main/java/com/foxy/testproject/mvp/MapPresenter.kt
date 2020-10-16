package com.foxy.testproject.mvp

import android.location.Location
import com.foxy.testproject.GlobalCategories
import com.here.sdk.core.GeoCircle
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapviewlite.MapCircle
import com.here.sdk.mapviewlite.MapCircleStyle
import com.here.sdk.mapviewlite.MapScene
import com.here.sdk.mapviewlite.PixelFormat

import moxy.InjectViewState
import moxy.MvpPresenter
import java.util.*

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {

    private var currentCoordinates: GeoCoordinates = GeoCoordinates(0.0, 0.0)
    private var currentZoomLevel: Double = 0.0

//    private var mapObjects = mutableListOf<MapObject>()
//        .apply { emptyList<MapObject>() }

    private lateinit var circle: MapCircle


    fun clear() {
//        clearMap()
        viewState.clearQuery()
    }

    fun showMoreCategories(globalCategory: GlobalCategories, title: String) {
        val categoriesId = globalCategory.toString().toLowerCase(Locale.ROOT)
        val requestsId = "${categoriesId}_request"
        viewState.openDialog(categoriesId, requestsId, title)
    }

    fun locate(isStarted: Boolean) {
        if (isStarted) return
        viewState.startLocating()
    }

    fun saveLocation(location: Location?) {
        location?.let {
            currentCoordinates = GeoCoordinates(location.latitude, location.longitude)
            currentZoomLevel = CUSTOM_ZOOM_LEVEL
            viewState.updateMap(currentCoordinates, currentZoomLevel)
        }
    }

//    fun searchByCategory(query: String, title: String, geoCoordinate: GeoCoordinate) {
//        clearMap()
//        viewState.closeDialog()
//        viewState.showQuery(title)
//
//        val filter = CategoryFilter().apply { add(query) }
//        val exploreRequest = ExploreRequest().apply {
//            setSearchCenter(geoCoordinate)
//            setCategoryFilter(filter)
//        }
//        exploreRequest.execute { discoveryResultPage, errorCode ->
//            computeResult(discoveryResultPage, errorCode)
//        }
//    }

//    fun searchByKeyword(query: String, geoCoordinate: GeoCoordinate) {
//        if (query.isNotEmpty()) {
//            clearMap()
//
//            val searchRequest = SearchRequest(query).setSearchCenter(geoCoordinate)
//            searchRequest.execute { discoveryResultPage, errorCode ->
//                computeResult(discoveryResultPage, errorCode)
//            }
//
//            viewState.showQuery(query)
//        }
//    }

    fun onMapSceneInitializationCompleted(errorCode: MapScene.ErrorCode?) {
        if (errorCode == null) {
            viewState.updateMap(currentCoordinates, currentZoomLevel)
            val newCircle = createMapCircle(currentCoordinates, 500f)
            if (this::circle.isInitialized) {
                viewState.updateMapCircle(circle, newCircle)
            } else {
                viewState.updateMapCircle(newCircle, newCircle)
            }
            circle = newCircle
        } else {
            viewState.showError(errorCode)
        }
    }

    private fun createMapCircle(coordinates: GeoCoordinates, radius: Float): MapCircle {
        val geoCircle = GeoCircle(coordinates, radius)
        val circleStyle =
            MapCircleStyle().apply { setStrokeColor(0x00908AA0, PixelFormat.RGBA_8888) }
        return MapCircle(geoCircle, circleStyle)
    }

//    private fun computeResult(discoveryResultPage: DiscoveryResultPage?, errorCode: ErrorCode) {
//        if (errorCode == ErrorCode.NONE) {
//            if (discoveryResultPage != null) {
//                val items = discoveryResultPage.items
//                for (item in items) {
//                    if (item.resultType == DiscoveryResult.ResultType.PLACE) {
//                        val mapObject = createMapObject(item as PlaceLink)
//                        map.addMapObject(mapObject)
//                        viewState.updateMap(map)
//                    }
//                }
//            } else {
//                // Not found
//            }
//        } else {
//            viewState.showError(errorCode)
//        }
//    }

//    private fun createMapObject(item: PlaceLink): MapMarker {
//        val img = Image().apply { setImageResource(R.drawable.marker) }
//        val mapObject = MapMarker(item.position!!, img)
//        mapObjects.add(mapObject)
//        return mapObject
//    }
//

//
//    private fun clearMap() {
//        if (mapObjects.isNotEmpty()) {
//            map.removeMapObjects(mapObjects)
//            mapObjects.clear()
//            viewState.updateMap(map)
//        }
//    }

}

private const val CUSTOM_ZOOM_LEVEL = 15.0

private const val LOCATION_UPDATE_INTERVAL_IN_MS = 10000L
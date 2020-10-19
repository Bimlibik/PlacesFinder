package com.foxy.testproject.mvp

import android.location.Location
import android.util.Log
import com.foxy.testproject.GlobalCategories
import com.foxy.testproject.data.Category
import com.foxy.testproject.data.ICategoriesRepository
import com.here.sdk.core.GeoCircle
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.mapviewlite.*
import com.here.sdk.search.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter

@InjectViewState
class MapPresenter(private val repository: ICategoriesRepository) : MvpPresenter<MapView>() {

    private var currentCoordinates: GeoCoordinates = GeoCoordinates(0.0, 0.0)
    private var currentZoomLevel: Double = 0.0

    private var categories: List<Category> = mutableListOf()

    private var places = mutableListOf<Place>()
        .apply { emptyList<Place>() }

    private var mapObjects = mutableListOf<MapMarker>()
        .apply { emptyList<MapMarker>() }

    private lateinit var circle: MapCircle

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadCategories()
    }


    fun clear() {
        clearMap()
        viewState.clearQuery()
    }

    fun showMoreCategories(globalCategory: GlobalCategories, title: String) {
        val groupCategories = mutableListOf<Category>()
        val titles = arrayListOf<String>()
        for (category in categories) {
            if (category.groupId == globalCategory.id) {
                groupCategories.add(category)
                titles.add(category.name)
            }
        }
        viewState.openDialog(title, groupCategories, titles)
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

    fun searchByCategory(query: Category, geoCoordinate: GeoCoordinates) {
        val categoryQuery = CategoryQuery(PlaceCategory(query.categoryId), geoCoordinate)
        val searchOptions = SearchOptions(LanguageCode.RU_RU, MAX_SEARCH_RESULT)
        viewState.closeDialog()
        viewState.startSearching(categoryQuery, searchOptions)
        viewState.showQuery(query.name)
    }

    fun searchByKeyword(query: String, geoCoordinate: GeoCoordinates) {
        if (query.isNotEmpty()) {
            clearMap()
            val textQuery = TextQuery(query, geoCoordinate)
            val searchOptions = SearchOptions(LanguageCode.RU_RU, MAX_SEARCH_RESULT)
            viewState.startSearching(textQuery, searchOptions)
            viewState.showQuery(query)
        }
    }

    fun computeResult(searchError: SearchError?, result: MutableList<Place>?) {
        if (searchError != null) {
            Log.i("TAG2", "computeResult: Search Error: $searchError")
            return
        }

        if (result != null && result.isNotEmpty()) {
            places = result
            clearMap()

            for (place in result) {
                place.geoCoordinates?.let {
                    viewState.addMarkerToMap(createMarker(it))
                }
                Log.i("Place", "Place: title - ${place.title}")
            }
        }

    }

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

    private fun createMarker(geoCoordinate: GeoCoordinates): MapMarker {
        val mapObject = MapMarker(geoCoordinate)
        mapObjects.add(mapObject)
        return mapObject
    }

    private fun clearMap() {
        if (mapObjects.isNotEmpty()) {
            mapObjects.forEach {
                viewState.removeMarkers(it)
            }
            mapObjects.clear()
            places.clear()
        }
    }

    private fun loadCategories() = GlobalScope.launch {
        categories = repository.getCategories()
    }

}

private const val CUSTOM_ZOOM_LEVEL = 15.0
private const val MAX_SEARCH_RESULT = 30

private const val LOCATION_UPDATE_INTERVAL_IN_MS = 10000L
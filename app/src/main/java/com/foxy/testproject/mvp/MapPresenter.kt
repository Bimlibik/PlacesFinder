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

    private lateinit var searchEngine: SearchEngine
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
        searchEngine = SearchEngine()
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
            currentZoomLevel = DEFAULT_ZOOM_LEVEL
            viewState.updateMap(currentCoordinates, currentZoomLevel)
        }
    }

    fun searchByCategory(query: Category, geoCoordinate: GeoCoordinates) {
        clearMap()
        val categoryQuery = CategoryQuery(PlaceCategory(query.categoryId), geoCoordinate)
        val searchOptions = SearchOptions(LanguageCode.RU_RU, MAX_SEARCH_RESULT)
        searchEngine.search(categoryQuery, searchOptions) { error, result ->
            computeResult(error, result)
        }

        viewState.closeDialog()
        viewState.showQuery(query.name)
    }

    fun searchByKeyword(query: String, geoCoordinate: GeoCoordinates) {
        if (query.isNotEmpty()) {
            clearMap()
            val textQuery = TextQuery(query, geoCoordinate)
            val searchOptions = SearchOptions(LanguageCode.RU_RU, MAX_SEARCH_RESULT)
            searchEngine.search(textQuery, searchOptions) { error, result ->
                computeResult(error, result)
            }
            viewState.showQuery(query)
        }
    }

    fun onMapSceneInitializationCompleted(errorCode: MapScene.ErrorCode?) {
        if (errorCode == null) {
            viewState.updateMap(currentCoordinates, currentZoomLevel)
            val newCircle = createMapCircle(currentCoordinates, DEFAULT_RADIUS)
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

    private fun computeResult(searchError: SearchError?, result: MutableList<Place>?) {
        if (searchError != null) {
            Log.i("TAG2", "computeResult: Search Error: $searchError")
            return
        }

        if (result != null && result.isNotEmpty()) {
            places = result

            for (place in result) {
                place.geoCoordinates?.let {
                    viewState.addMarkerToMap(createMarker(it), DEFAULT_IMG_SCALE)
                }
                Log.i("Place", "Place: title - ${place.title}")
            }
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

private const val DEFAULT_ZOOM_LEVEL = 15.0
private const val DEFAULT_RADIUS = 500f
private const val DEFAULT_IMG_SCALE = 0.5f
private const val MAX_SEARCH_RESULT = 30

package com.foxy.testproject.mvp

import android.location.Location
import android.util.Log
import android.view.View
import com.foxy.testproject.GlobalCategories
import com.foxy.testproject.PlaceMetadata
import com.foxy.testproject.R
import com.foxy.testproject.data.Category
import com.foxy.testproject.data.ICategoriesRepository
import com.foxy.testproject.data.ICategoriesRepository.CategoriesLoaded
import com.foxy.testproject.utils.DialogType
import com.here.sdk.core.GeoCircle
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.core.Metadata
import com.here.sdk.mapviewlite.*
import com.here.sdk.search.*
import moxy.InjectViewState
import moxy.MvpPresenter

@InjectViewState
class MapPresenter(
    private val repository: ICategoriesRepository,
) : MvpPresenter<MapView>() {

    private lateinit var searchEngine: SearchEngine
    private var currentCoordinates: GeoCoordinates = GeoCoordinates(0.0, 0.0)
    private var currentZoomLevel: Double = 0.0
    private var showGpsInfo = true

    private var categories: MutableList<Category> = mutableListOf()

    private var mapObjects = mutableListOf<MapMarker>()
        .apply { emptyList<MapMarker>() }

    private lateinit var circle: MapCircle
    private lateinit var currentDot: MapMarker

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadCategories()
        searchEngine = SearchEngine()
    }

    fun closeDialog(type: DialogType) {
        when (type) {
            DialogType.CATEGORIES -> viewState.hideCategoriesDialog()
            DialogType.GPS -> {
                viewState.hideGpsInfoDialog()
                viewState.updateToolbar(View.GONE, R.string.app_name)
            }
            else -> viewState.hideMarkerDetails()
        }
    }

    fun showDetails(pickMapItemsResult: PickMapItemsResult?) {
        if (pickMapItemsResult == null) return

        val topmostMarker = pickMapItemsResult.topmostMarker ?: return
        val metadata = topmostMarker.metadata
        viewState.showMarkerDetails()
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
        viewState.openCategoriesDialog(title, groupCategories, titles)
    }

    fun showGpsInfo() {
        if (showGpsInfo) {
            viewState.openGpsInfoDialog()
            showGpsInfo = !showGpsInfo
        }
    }

    fun enableGps() {
        viewState.openGpsSettings()
        viewState.hideGpsInfoDialog()
        viewState.updateToolbar(View.VISIBLE, R.string.toolbar_title)
    }

    fun locate(isGpsEnabled: Boolean) {
        if (isGpsEnabled) {
            viewState.startLocating()
        } else {
            viewState.openGpsInfoDialog()
        }
    }

    fun saveLocation(location: Location?) {
        location?.let {
            currentCoordinates = GeoCoordinates(location.latitude, location.longitude)
            currentZoomLevel = DEFAULT_ZOOM_LEVEL
            viewState.updateMap(currentCoordinates, currentZoomLevel)
            viewState.updateToolbar(View.GONE, R.string.app_name)
            createDot(currentCoordinates)
            createMapCircle(currentCoordinates)
        }
    }

    fun searchByCategory(query: Category, geoCoordinate: GeoCoordinates) {
        clearMap()
        val categoryQuery = CategoryQuery(PlaceCategory(query.categoryId), geoCoordinate)
        val searchOptions = SearchOptions(LanguageCode.RU_RU, MAX_SEARCH_RESULT)
        searchEngine.search(categoryQuery, searchOptions) { error, result ->
            computeResult(error, result)
        }

        viewState.hideCategoriesDialog()
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
            for (place in result) {
                place.geoCoordinates?.let {
                    mapObjects.add(createMarker(place))
                }
                Log.i("Place", "Place: title - ${place.title}")
            }
            viewState.addMarkersToMap(mapObjects)
        }
    }

    private fun createDot(coordinates: GeoCoordinates) {
        val newDot = MapMarker(coordinates)
        if (this::currentDot.isInitialized) {
            viewState.showCurrentLocation(currentDot, newDot)
        } else {
            viewState.showCurrentLocation(newDot, newDot)
        }
        currentDot = newDot
    }

    private fun createMapCircle(coordinates: GeoCoordinates, radius: Float = DEFAULT_RADIUS) {
        val geoCircle = GeoCircle(coordinates, radius)
        val circleStyle =
            MapCircleStyle().apply { setStrokeColor(0x00908AA0, PixelFormat.RGBA_8888) }
        val newCircle = MapCircle(geoCircle, circleStyle)

        if (this::circle.isInitialized) {
            viewState.updateMapCircle(circle, newCircle)
        } else {
            viewState.updateMapCircle(newCircle, newCircle)
        }
        circle = newCircle
    }

    private fun createMarker(place: Place): MapMarker {
        val metaData = Metadata()
        metaData.setCustomValue("poi", PlaceMetadata(place))
        val mapObject = MapMarker(place.geoCoordinates!!)
        mapObject.metadata = metaData
        return mapObject
    }

    private fun clearMap() {
        if (mapObjects.isNotEmpty()) {
            viewState.removeMarkers(mapObjects)
            mapObjects.clear()
        }
    }

    private fun loadCategories() {
        repository.getCategories(object : CategoriesLoaded {
            override fun onDataLoaded(loadedCategories: List<Category>) {
                categories.clear()
                categories.addAll(loadedCategories)
            }

            override fun onDataNotAvailable() {
                loadCategories()
            }
        })
    }

}

private const val DEFAULT_ZOOM_LEVEL = 15.0
private const val DEFAULT_RADIUS = 500f
private const val MAX_SEARCH_RESULT = 30

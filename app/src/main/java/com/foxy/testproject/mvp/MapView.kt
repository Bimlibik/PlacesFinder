package com.foxy.testproject.mvp


import com.foxy.testproject.data.Category
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapviewlite.MapCircle
import com.here.sdk.mapviewlite.MapMarker
import com.here.sdk.mapviewlite.MapScene
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface MapView : MvpView {

    fun updateMap(coordinates: GeoCoordinates, zoomLevel: Double)

    fun updateMapCircle(oldMapCircle: MapCircle, newMapCircle: MapCircle)

    fun startLocating()

    fun openGpsInfoDialog()

    fun hideGpsInfoDialog()

    @StateStrategyType(value = SkipStrategy::class)
    fun openGpsSettings()

    fun addMarkersToMap(mapObjects: List<MapMarker>)

    fun removeMarkers(mapObjects: List<MapMarker>)

    fun openCategoriesDialog(title: String, result: List<Category>, titles: List<String>)

    fun hideCategoriesDialog()

    fun showMarkerDetails()

    fun hideMarkerDetails()

    fun showError(errorCode: MapScene.ErrorCode)

    fun showQuery(query: String)

    fun clearQuery()
}
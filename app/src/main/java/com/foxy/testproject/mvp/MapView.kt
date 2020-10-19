package com.foxy.testproject.mvp


import com.foxy.testproject.data.Category
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapviewlite.MapCircle
import com.here.sdk.mapviewlite.MapMarker
import com.here.sdk.mapviewlite.MapScene
import com.here.sdk.search.CategoryQuery
import com.here.sdk.search.SearchOptions
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface MapView : MvpView {

    fun updateMap(coordinates: GeoCoordinates, zoomLevel: Double)

    fun updateMapCircle(oldMapCircle: MapCircle, newMapCircle: MapCircle)

    fun startLocating()

    fun startSearching(categoryQuery: CategoryQuery, searchOptions: SearchOptions)

    fun addMarkerToMap(marker: MapMarker)

    fun removeMarkers(marker: MapMarker)

    fun closeDialog()

    fun showError(errorCode: MapScene.ErrorCode)

    fun openDialog(title: String, result: List<Category>, titles: List<String>)

    fun showQuery(query: String)

    fun clearQuery()
}
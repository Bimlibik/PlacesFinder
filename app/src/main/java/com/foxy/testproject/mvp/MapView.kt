package com.foxy.testproject.mvp


import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapviewlite.MapCircle
import com.here.sdk.mapviewlite.MapScene
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface MapView : MvpView {

    fun updateMap(coordinates: GeoCoordinates, zoomLevel: Double)

    fun updateMapCircle(oldMapCircle: MapCircle, newMapCircle: MapCircle)

    fun startLocating()

    fun closeDialog()

    fun showError(errorCode: MapScene.ErrorCode)

    fun openDialog(categoriesId: String, requestsId: String, title: String)

    fun showQuery(query: String)

    fun clearQuery()
}
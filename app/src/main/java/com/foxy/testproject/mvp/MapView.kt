package com.foxy.testproject.mvp

import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapMarker
import com.here.android.mpa.mapping.MapObject
import com.here.android.mpa.search.DiscoveryResult
import com.here.android.mpa.search.ErrorCode
import com.here.android.mpa.search.PlaceLink
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface MapView : MvpView {

    fun updateMap(map: Map)

    fun executeRequest(query: String, geoCoordinate: GeoCoordinate)

    fun showError(errorCode: ErrorCode)
}
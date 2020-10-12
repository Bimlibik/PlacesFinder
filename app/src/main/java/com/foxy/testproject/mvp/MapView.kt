package com.foxy.testproject.mvp

import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.search.ErrorCode
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface MapView : MvpView {

    fun updateMap(map: Map)

    fun executeRequest(query: String, geoCoordinate: GeoCoordinate)

    fun showError(errorCode: ErrorCode)
}
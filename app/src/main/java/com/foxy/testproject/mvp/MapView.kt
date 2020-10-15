package com.foxy.testproject.mvp

import com.here.android.mpa.mapping.Map
import com.here.android.mpa.search.ErrorCode
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface MapView : MvpView {

    fun updateMap(map: Map)

    fun closeDialog()

    fun showError(errorCode: ErrorCode)

    fun openDialog(categoriesId: String, requestsId: String, title: String)

    fun showQuery(query: String)

    fun clearQuery()
}
package com.foxy.testproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.foxy.testproject.R
import com.foxy.testproject.mvp.MapPresenter
import com.foxy.testproject.mvp.MapView
import com.here.android.mpa.common.ApplicationContext
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.MapEngine
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.search.ErrorCode
import com.here.android.mpa.search.SearchRequest
import kotlinx.android.synthetic.main.fragment_map.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter

class MapFragmentView : MvpAppCompatFragment(), MapView {

    private var mapFragment: AndroidXMapFragment = AndroidXMapFragment()

    @InjectPresenter
    lateinit var presenter: MapPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createMapFragment()
        initSearchButtons()
    }

    override fun updateMap(map: Map) {
        mapFragment.setMap(map)
    }

    override fun executeRequest(query: String, geoCoordinate: GeoCoordinate) {
        val searchRequest = SearchRequest(query).setSearchCenter(geoCoordinate)
        searchRequest.execute { discoveryResultPage, errorCode ->
            presenter.computeResult(discoveryResultPage, errorCode)
        }
    }

    override fun showError(errorCode: ErrorCode) {
        Toast.makeText(requireContext(), "Error $errorCode", Toast.LENGTH_LONG).show()
    }

    private fun initSearchButtons() {
        btn_locate.setOnClickListener { presenter.locate(MapEngine.isInitialized()) }
        btn_cinema.setOnClickListener { presenter.search("Cinema", mapFragment.map?.center!!) }
    }

    private fun initMapEngine() {
        mapFragment.init(ApplicationContext(requireContext())) { error ->
            presenter.onEngineInitializationCompleted(error)
        }
    }

    private fun createMapFragment() {
        childFragmentManager.beginTransaction()
            .add(R.id.map_view, mapFragment)
            .commit()
        initMapEngine()
    }

}
package com.foxy.testproject.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.foxy.testproject.GlobalCategories
import com.foxy.testproject.PlatformPositioningProvider
import com.foxy.testproject.R
import com.foxy.testproject.mvp.MapPresenter
import com.foxy.testproject.mvp.MapView
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Location
import com.here.sdk.mapviewlite.*
import kotlinx.android.synthetic.main.fragment_map.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter

class MapFragmentView : MvpAppCompatFragment(), MapView, LocationListener {

    private lateinit var mapView: MapViewLite
    private lateinit var dialog: AlertDialog
    private lateinit var platformPositioningProvider: PlatformPositioningProvider

    @InjectPresenter
    lateinit var presenter: MapPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        platformPositioningProvider = PlatformPositioningProvider(requireContext())
        setHasOptionsMenu(true)
        createMapView(savedInstanceState)
        initSearchButtons()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_locate -> {
                presenter.locate(platformPositioningProvider.isStarted())
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        platformPositioningProvider.stopLocating()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        startLocating()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun updateMap(coordinates: GeoCoordinates, zoomLevel: Double) {
        mapView.camera.target = coordinates
        mapView.camera.zoomLevel = zoomLevel
    }

    override fun updateMapCircle(oldMapCircle: MapCircle, newMapCircle: MapCircle) {
        mapView.mapScene.removeMapCircle(oldMapCircle)
        mapView.mapScene.addMapCircle(newMapCircle)
    }

    override fun startLocating() {
        platformPositioningProvider.startLocating(object : PlatformPositioningProvider.PlatformLocationListener {
            override fun onLocationUpdated(location: android.location.Location?) {
                presenter.saveLocation(location)
            }
        })
    }

    override fun closeDialog() {
        dialog.dismiss()
    }

    override fun showError(errorCode: MapScene.ErrorCode) {
        Toast.makeText(requireContext(), "Error $errorCode", Toast.LENGTH_LONG).show()
    }

    override fun openDialog(categoriesId: String, requestsId: String, title: String) {
        val categoriesResId =
            resources.getIdentifier(categoriesId, "array", requireContext().packageName)
        val requestResId =
            resources.getIdentifier(requestsId, "array", requireContext().packageName)
        val categories = resources.getStringArray(categoriesResId)
        val requests = resources.getStringArray(requestResId)
        dialog = AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            setItems(categories) { _, which ->
//                presenter.searchByCategory(
//                    requests[which],
//                    categories[which],
//                    mapFragment.map?.center!!
//                )
            }
            create()
        }.show()
    }

    override fun showQuery(query: String) {
        field_request.setText(query)
        btn_search.visibility = View.GONE
        btn_clear.visibility = View.VISIBLE
    }

    override fun clearQuery() {
        field_request.text.clear()
        btn_search.visibility = View.VISIBLE
        btn_clear.visibility = View.GONE
    }

    private fun initSearchButtons() {
        btn_eat_drink.setOnClickListener {
            presenter.showMoreCategories(
                GlobalCategories.EAT_AND_DRINK,
                btn_eat_drink.text.toString()
            )
        }
        btn_going_out.setOnClickListener {
            presenter.showMoreCategories(
                GlobalCategories.GOING_OUT,
                btn_going_out.text.toString()
            )
        }
        btn_sights.setOnClickListener {
            presenter.showMoreCategories(
                GlobalCategories.SIGHTS,
                btn_sights.text.toString()
            )
        }
        btn_transport.setOnClickListener {
            presenter.showMoreCategories(
                GlobalCategories.TRANSPORT,
                btn_transport.text.toString()
            )
        }
        btn_accommodation.setOnClickListener {
            presenter.showMoreCategories(
                GlobalCategories.ACCOMMODATION,
                btn_accommodation.text.toString()
            )
        }
        btn_shopping.setOnClickListener {
            presenter.showMoreCategories(
                GlobalCategories.SHOPPING,
                btn_shopping.text.toString()
            )
        }
        btn_services.setOnClickListener {
            presenter.showMoreCategories(
                GlobalCategories.SERVICES,
                btn_services.text.toString()
            )
        }
        btn_facilities.setOnClickListener {
            presenter.showMoreCategories(
                GlobalCategories.FACILITIES,
                btn_facilities.text.toString()
            )
        }
        btn_search.setOnClickListener {
//            presenter.searchByKeyword(
//                field_request.text.toString(),
//                mapFragment.map?.center!!
//            )
        }

        btn_clear.setOnClickListener { presenter.clear() }
    }

    private fun initMapScene() {
        mapView.mapScene.loadScene(MapStyle.NORMAL_DAY) { errorCode ->
            presenter.onMapSceneInitializationCompleted(errorCode)
        }
    }

    private fun createMapView(savedInstanceState: Bundle?) {
        mapView = requireActivity().findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        initMapScene()
    }

    override fun onLocationChanged(location: android.location.Location) {
        Toast.makeText(requireContext(), "Location: ${location.latitude}, ${location.longitude}", Toast.LENGTH_LONG).show()
    }


}
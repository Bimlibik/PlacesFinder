package com.foxy.testproject.ui

import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.foxy.testproject.GlobalCategories
import com.foxy.testproject.PlatformPositioningProvider
import com.foxy.testproject.R
import com.foxy.testproject.data.Category
import com.foxy.testproject.mvp.MapPresenter
import com.foxy.testproject.mvp.MapView
import com.foxy.testproject.utils.DialogType
import com.foxy.testproject.utils.InjectorUtils
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Point2D
import com.here.sdk.gestures.TapListener
import com.here.sdk.mapviewlite.*
import kotlinx.android.synthetic.main.fragment_map.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class MapFragmentView : MvpAppCompatFragment(), MapView {

    private lateinit var mapView: MapViewLite
    private lateinit var dialog: AlertDialog
    private lateinit var platformPositioningProvider: PlatformPositioningProvider

    @InjectPresenter
    lateinit var presenter: MapPresenter

    @ProvidePresenter
    fun providePresenter(): MapPresenter =
         MapPresenter(InjectorUtils.getCategoriesRepository())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        platformPositioningProvider = PlatformPositioningProvider(requireContext())
        setupToolbar()
        setHasOptionsMenu(true)
        createMapView(savedInstanceState)
        initSearchButtons()
        setTapGestureHandler()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_locate -> {
                presenter.locate(platformPositioningProvider.isGpsEnabled())
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
        platformPositioningProvider.startLocating(object :
            PlatformPositioningProvider.PlatformLocationListener {
            override fun onLocationUpdated(location: Location?) {
                presenter.saveLocation(location)
            }

            override fun onGpsDisabled() {
                presenter.showGpsInfo()
            }
        })
    }

    override fun openGpsInfoDialog() {
        dialog = AlertDialog.Builder(requireContext()).apply {
            setMessage(getString(R.string.dialog_msg_gps))
            setCancelable(false)
            setPositiveButton(getString(R.string.btn_gps_enable)) { _, _ -> presenter.enableGps() }
            setNegativeButton(getString(R.string.btn_undo)) { _, _ -> presenter.closeDialog(DialogType.GPS) }
            create()
        }.show()
    }

    override fun hideGpsInfoDialog() {
        dialog.dismiss()
    }

    override fun openGpsSettings() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    override fun addMarkersToMap(mapObjects: List<MapMarker>) {
        val img = MapImageFactory.fromResource(resources, R.drawable.poi)
        val style = MapMarkerImageStyle()
        mapObjects.forEach { marker ->
            marker.addImage(img, style)
            mapView.mapScene.addMapMarker(marker)
        }
    }

    override fun removeMarkers(mapObjects: List<MapMarker>) {
        mapObjects.forEach {
            mapView.mapScene.removeMapMarker(it)
        }
    }

    override fun showMarkerDetails() {
        dialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("title")
            setMessage("msg")
            setOnCancelListener { presenter.closeDialog(DialogType.MARKER) }
            create()
        }.show()
    }

    override fun hideMarkerDetails() {
        dialog.dismiss()
    }

    override fun showError(errorCode: MapScene.ErrorCode) {
        Toast.makeText(requireContext(), "Error $errorCode", Toast.LENGTH_LONG).show()
    }

    override fun openCategoriesDialog(title: String, categories: List<Category>, titles: List<String>) {
        dialog = AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            setItems(titles.toTypedArray()) { _, which ->
                presenter.searchByCategory(
                    categories[which],
                    mapView.camera.target
                )
            }
            setCancelable(false)
            setNegativeButton(getString(R.string.btn_undo)) { _, _ -> presenter.closeDialog(DialogType.CATEGORIES) }
            create()
        }.show()
    }

    override fun hideCategoriesDialog() {
        dialog.dismiss()
    }

    override fun showQuery(query: String) {
        field_request.setText(query)
        field_request.setSelection(query.length)
        btn_search.visibility = View.GONE
        btn_clear.visibility = View.VISIBLE
    }

    override fun clearQuery() {
        field_request.text.clear()
        btn_search.visibility = View.VISIBLE
        btn_clear.visibility = View.GONE
    }

    override fun updateToolbar(progressVisibility: Int, title: Int) {
        progress.visibility = progressVisibility
        toolbar_title.text = getString(title)
        Log.i("TAG2", "updateToolbar: ${getString(title)}")
    }

    private fun setTapGestureHandler() {
        mapView.gestures.tapListener = TapListener { touchPoint ->
            Log.i("TAG3", "setTapGestureHandler: ")
            pickMapMarker(touchPoint)
        }
    }

    private fun pickMapMarker(touchPoint: Point2D) {
        val radiusInPixel = 1.0
        mapView.pickMapItems(touchPoint, radiusInPixel) { pickMapItemsResult ->
            presenter.showDetails(pickMapItemsResult)
        }
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
            presenter.searchByKeyword(
                field_request.text.toString(),
                mapView.camera.target
            )
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

    private fun setupToolbar() {
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }



}
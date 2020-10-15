package com.foxy.testproject.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.foxy.testproject.GlobalCategories
import com.foxy.testproject.R
import com.foxy.testproject.mvp.MapPresenter
import com.foxy.testproject.mvp.MapView
import com.here.android.mpa.common.ApplicationContext
import com.here.android.mpa.common.MapEngine
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.search.ErrorCode
import kotlinx.android.synthetic.main.fragment_map.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter

class MapFragmentView : MvpAppCompatFragment(), MapView {

    private var mapFragment: AndroidXMapFragment = AndroidXMapFragment()

    private lateinit var dialog: AlertDialog

    @InjectPresenter
    lateinit var presenter: MapPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        createMapFragment()
        initSearchButtons()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_locate -> {
                presenter.locate(MapEngine.isInitialized())
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun updateMap(map: Map) {
        mapFragment.setMap(map)
    }

    override fun closeDialog() {
        dialog.dismiss()
    }

    override fun showError(errorCode: ErrorCode) {
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
                presenter.searchByCategory(
                    requests[which],
                    categories[which],
                    mapFragment.map?.center!!
                )
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
            presenter.searchByKeyword(
                field_request.text.toString(),
                mapFragment.map?.center!!
            )
        }

        btn_clear.setOnClickListener { presenter.clear() }
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
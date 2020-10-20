package com.foxy.testproject

import com.here.sdk.core.CustomMetadataValue
import com.here.sdk.search.Place

class PlaceMetadata(private val place: Place) : CustomMetadataValue {

    override fun getTag(): String = place.id
}
package com.foxy.testproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.here.sdk.search.PlaceCategory
import java.util.*

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val categoryId: String,
    val name: String,
    val group: String,
    val groupId: String
)
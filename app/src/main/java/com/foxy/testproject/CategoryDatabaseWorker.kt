package com.foxy.testproject

import android.content.Context
import android.util.Log
import com.foxy.testproject.data.Category
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.lang.Exception

class CategoryDatabaseWorker {

    companion object {
        fun getCategories(context: Context): List<Category> {
            try {
                context.assets.open(CATEGORY_DATA_FILENAME).use { inputStream ->
                    JsonReader(inputStream.reader()).use { jsonReader ->
                        val categoryType = object : TypeToken<List<Category>>() {}.type
                        return Gson().fromJson(jsonReader, categoryType)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding database", e)
                return emptyList()
            }
        }


        private const val TAG = "CategoryDatabaseWorker"
        private const val CATEGORY_DATA_FILENAME = "categories.json"
    }
}
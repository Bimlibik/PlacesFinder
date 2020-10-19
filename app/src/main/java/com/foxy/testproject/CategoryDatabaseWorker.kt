package com.foxy.testproject

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.foxy.testproject.data.Category
import com.foxy.testproject.data.TestProjectDb
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.coroutineScope
import java.lang.Exception

class CategoryDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            applicationContext.assets.open(CATEGORY_DATA_FILENAME).use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val categoryType = object : TypeToken<List<Category>>(){}.type
                    val categoryList: List<Category> = Gson().fromJson(jsonReader, categoryType)
                    val database = TestProjectDb.getInstance(applicationContext)
                    database.categoryDao().insertCategories(categoryList)
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding database", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "CategoryDatabaseWorker"
        private const val CATEGORY_DATA_FILENAME = "categories.json"
    }
}
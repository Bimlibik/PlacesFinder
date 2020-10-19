package com.foxy.testproject.data

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class CategoriesRepository(
    private val categoryDao: CategoryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ICategoriesRepository {

    override suspend fun getCategories(): List<Category> = withContext(ioDispatcher) {
        return@withContext try {
            categoryDao.getAllCategories()
        } catch (e: Exception) {
            Log.e(TAG, "Error while loading categories: $e")
            emptyList()
        }
    }

    companion object {
        private const val TAG = "CategoriesRepository"

        @Volatile
        private var instance: CategoriesRepository? = null

        fun getInstance(categoryDao: CategoryDao) =
            instance ?: synchronized(this) {
                instance ?: CategoriesRepository(categoryDao).also { instance = it }
            }
    }
}
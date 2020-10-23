package com.foxy.testproject.data

import com.foxy.testproject.AppExecutors
import com.foxy.testproject.data.ICategoriesRepository.CategoriesLoaded

class CategoriesRepository(
    private val categoryDao: CategoryDao,
    private val executors: AppExecutors,
) : ICategoriesRepository {

    override fun getCategories(callback: CategoriesLoaded) {
        executors.discIO().execute {
            val categories = categoryDao.getAllCategories()
            executors.mainThread().execute {
                if (categories.isEmpty()) {
                    callback.onDataNotAvailable()
                } else {
                    callback.onDataLoaded(categories)
                }
            }
        }
    }

    companion object {
        private const val TAG = "CategoriesRepository"

        @Volatile
        private var instance: CategoriesRepository? = null

        fun getInstance(categoryDao: CategoryDao, executors: AppExecutors) =
            instance ?: synchronized(this) {
                instance ?: CategoriesRepository(categoryDao, executors).also { instance = it }
            }
    }
}
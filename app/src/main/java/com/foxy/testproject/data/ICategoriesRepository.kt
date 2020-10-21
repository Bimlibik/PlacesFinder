package com.foxy.testproject.data

interface ICategoriesRepository {

    fun getCategories(callback: CategoriesLoaded)

    interface CategoriesLoaded {
        fun onDataLoaded(loadedCategories: List<Category>)
        fun onDataNotAvailable()
    }
}
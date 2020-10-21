package com.foxy.testproject.utils

import com.foxy.testproject.AppExecutors
import com.foxy.testproject.TestProjectApp
import com.foxy.testproject.data.CategoriesRepository
import com.foxy.testproject.data.ICategoriesRepository
import com.foxy.testproject.data.TestProjectDb

object InjectorUtils {

    fun getCategoriesRepository(): ICategoriesRepository =
        CategoriesRepository.getInstance(getDatabase().categoryDao(), getExecutors())

    fun getDatabase(): TestProjectDb = TestProjectDb.getInstance(
        TestProjectApp.get(),
        getExecutors()
    )

    private fun getExecutors(): AppExecutors = AppExecutors()

}
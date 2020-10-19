package com.foxy.testproject.data

interface ICategoriesRepository {

    suspend fun getCategories(): List<Category>
}
package com.foxy.testproject.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryDao {

    @Query("SELECT COUNT(*) FROM categories")
    fun count(): Int

    @Insert
    fun insertCategories(categories: List<Category>)

    @Query("SELECT * FROM categories WHERE `group` = :group")
    fun getCategoriesByGroup(group: String): List<Category>

    @Query("SELECT * FROM categories")
    fun getAllCategories(): List<Category>
}
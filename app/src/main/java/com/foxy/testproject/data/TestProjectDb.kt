package com.foxy.testproject.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.foxy.testproject.AppExecutors
import com.foxy.testproject.CategoryDatabaseWorker

@Database(entities = [Category::class], version = 1, exportSchema = false)
abstract class TestProjectDb : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DATABASE_NAME = "testProjectDatabase"

        @Volatile
        private var instance: TestProjectDb? = null

        fun getInstance(context: Context, executors: AppExecutors): TestProjectDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context, executors).also {
                    executors.discIO().execute { it.categoryDao().count() }
                    instance = it
                }
            }
        }

        private fun buildDatabase(context: Context, executors: AppExecutors): TestProjectDb {
            return Room.databaseBuilder(context, TestProjectDb::class.java, DATABASE_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        executors.discIO().execute {
                            val categories = CategoryDatabaseWorker.getCategories(context)
                            getInstance(context, executors).categoryDao().insertCategories(categories)
                        }
                        Log.i("TAG2", "onCreate: db")
                    }
                })
                .build()
        }
    }

}
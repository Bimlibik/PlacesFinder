package com.foxy.testproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.foxy.testproject.CategoryDatabaseWorker

@Database(entities = [Category::class], version = 1, exportSchema = false)
abstract class TestProjectDb : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DATABASE_NAME = "testProjectDatabase"

        @Volatile
        private var instance: TestProjectDb? = null

        fun getInstance(context: Context): TestProjectDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): TestProjectDb {
            return Room.databaseBuilder(context, TestProjectDb::class.java, DATABASE_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val request = OneTimeWorkRequestBuilder<CategoryDatabaseWorker>().build()
                        WorkManager.getInstance(context).enqueue(request)
                    }
                })
                .build()
        }
    }

}
package com.foxy.testproject

import android.app.Application
import com.foxy.testproject.data.TestProjectDb

class TestProjectApp : Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    fun getDatabase(): TestProjectDb = TestProjectDb.getInstance(INSTANCE)

    companion object {
        private lateinit var INSTANCE: TestProjectApp

        @JvmStatic
        fun get(): TestProjectApp = INSTANCE
    }
}
package com.foxy.testproject

import android.app.Application

class TestProjectApp : Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    companion object {
        private lateinit var INSTANCE: TestProjectApp

        @JvmStatic
        fun get(): TestProjectApp = INSTANCE
    }
}
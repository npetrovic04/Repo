package com.photostickers

import androidx.multidex.MultiDexApplication
import com.photostickers.helpers.AppOpenManager

class MyApplication : MultiDexApplication() {

    lateinit var appOpenManager: AppOpenManager

    companion object {
        private lateinit var instance: MyApplication

        fun getInstance(): MyApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appOpenManager = AppOpenManager(this)
    }
}
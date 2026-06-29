package com.anverter.app

import android.app.Application
import com.anverter.app.di.AppContainer

class AnverterApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

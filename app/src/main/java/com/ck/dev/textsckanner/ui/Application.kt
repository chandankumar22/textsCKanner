package com.ck.dev.textsckanner.ui

import android.app.Application
import com.ck.dev.textsckanner.BuildConfig
import com.ck.dev.textsckanner.utils.ReleaseTree
import com.ck.dev.textsckanner.utils.Utility.createLocalDirs
import timber.log.Timber

class Application:Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        createLocalDirs(this)
    }
}
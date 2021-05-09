package com.ck.dev.textsckanner.ui

import android.app.Application
import com.ck.dev.textsckanner.BuildConfig
import com.ck.dev.textsckanner.data.ScannedDocument
import com.ck.dev.textsckanner.ui.detectedtext.DetectedTextVm
import com.ck.dev.textsckanner.utils.ReleaseTree
import com.ck.dev.textsckanner.utils.StorageUtils
import com.ck.dev.textsckanner.utils.Utility.createLocalDirs
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class Application : Application() {

    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        createLocalDirs(this)
        StorageUtils.createAppFolderInInternalStorage(this)
        firebaseAnalytics = Firebase.analytics
    }

    companion object {
        lateinit var viewModel: DetectedTextVm
        var docFilenames = arrayListOf<String>()
        var isEdit = false
        var isGalleryImage = false
    }
}
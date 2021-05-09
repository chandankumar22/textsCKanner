package com.ck.dev.textsckanner.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ck.dev.textsckanner.data.AppDatabase
import com.ck.dev.textsckanner.data.AppDatabaseHelperImpl
import com.ck.dev.textsckanner.ui.detectedtext.DetectedTextVm
import com.ck.dev.textsckanner.ui.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelDetectedTextFactory(private val creator: AppDatabaseHelperImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DetectedTextVm(creator) as T
    }

}

class ViewModelFactory(private val creator: AppDatabaseHelperImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(creator) as T
    }

}
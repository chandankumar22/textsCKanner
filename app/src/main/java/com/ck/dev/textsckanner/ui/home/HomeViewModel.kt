package com.ck.dev.textsckanner.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ck.dev.textsckanner.data.AppDatabaseHelperImpl
import com.ck.dev.textsckanner.data.ScannedDocument
import com.ck.dev.textsckanner.utils.Constants
import com.ck.dev.textsckanner.utils.TASK_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class HomeViewModel(private val database: AppDatabaseHelperImpl): ViewModel() {

    val isInProgress = MutableLiveData<TASK_STATE<String>>()

    suspend fun getScannedDocs(): List<ScannedDocument> {
        return withContext(Dispatchers.IO) {
            database.getDocuments()
        }
    }

    fun getAllScannedDocLive(): LiveData<List<ScannedDocument>> {
        return database.getAllScannedDocLive()
    }

    fun deleteDocument(document: List<ScannedDocument>) {
        isInProgress.postValue(TASK_STATE.PROGRESS(""))
        viewModelScope.launch {
            try {
                database.delete(document)
                Timber.d("successfully deleted ${document.size} document/s")
                isInProgress.postValue(TASK_STATE.SUCCESS(Constants.saveSuccess))
            } catch (e: Exception) {
                Timber.e(e, "exception in deleting documents from db")
                isInProgress.postValue(TASK_STATE.FAILED(Constants.saveError))
            }
        }
    }
}
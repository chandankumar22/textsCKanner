package com.ck.dev.textsckanner.ui.detectedtext

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ck.dev.textsckanner.data.AppDatabaseHelperImpl
import com.ck.dev.textsckanner.data.ScannedDocument
import com.ck.dev.textsckanner.utils.Constants
import com.ck.dev.textsckanner.utils.Constants.saveError
import com.ck.dev.textsckanner.utils.Constants.saveSuccess
import com.ck.dev.textsckanner.utils.TASK_STATE
import kotlinx.coroutines.launch
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import timber.log.Timber
import java.io.FileOutputStream

class DetectedTextVm(private val database: AppDatabaseHelperImpl) : ViewModel() {

    var documentText = ""
    var isInProgress = MutableLiveData<TASK_STATE<String>>()

    private fun insertDocument(document: ScannedDocument) {
        Timber.i("insertDocument called")
        isInProgress.postValue(TASK_STATE.PROGRESS(""))
        viewModelScope.launch {
            try {
                database.insert(document)
                Timber.d("successfully inserted the document")
                isInProgress.postValue(TASK_STATE.SUCCESS(saveSuccess))
            } catch (e: Exception) {
                Timber.e(e, "exception in inserting document to db")
                isInProgress.postValue(TASK_STATE.FAILED(saveError))
            }
        }
    }

    fun updateFileContent(docName:String,docContent:String) {
        Timber.i("updateFileContent called")
        isInProgress.postValue(TASK_STATE.PROGRESS(""))
        viewModelScope.launch {
            try {
                database.updateFileContent(docName,docContent)
                Timber.d("successfully update the file content")
                isInProgress.postValue(TASK_STATE.SUCCESS(saveSuccess))
            } catch (e: Exception) {
                Timber.e(e, "exception in updating the file content")
                isInProgress.postValue(TASK_STATE.FAILED(saveError))
            }
        }
    }

    fun createDocFile(filePath: FileOutputStream, text: String, scannedDocument: ScannedDocument,isToInsert:Boolean = true) {
        Timber.i("createDocFile called")
        isInProgress.postValue(TASK_STATE.PROGRESS(""))
        val doc = XWPFDocument()
        try {
            val paragraph: XWPFParagraph = doc.createParagraph()
            val run: XWPFRun = paragraph.createRun()
            run.setText(text)
            doc.write(filePath)
            filePath.flush()
            filePath.close()
            if(!com.ck.dev.textsckanner.ui.Application.isEdit && isToInsert) insertDocument(scannedDocument)
        } catch (e: java.lang.Exception) {
            isInProgress.postValue(TASK_STATE.FAILED(saveError))
            println(e.message)
        }
    }
}
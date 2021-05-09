package com.ck.dev.textsckanner.data

import androidx.lifecycle.LiveData

class AppDatabaseHelperImpl(private val appDb:AppDatabase):AppDatabaseHelper {
    override suspend fun getDocuments(): List<ScannedDocument> {
        return appDb.scannedDocDao().getAllScannedDoc()
    }

    override suspend fun updateFileContent(docName: String, docTitle: String): Int {
        return appDb.scannedDocDao().updateFileContent(docName,docTitle)
    }

    override fun getAllScannedDocLive(): LiveData<List<ScannedDocument>> {
        return appDb.scannedDocDao().getAllScannedDocLive()
    }

    override suspend fun insert(scannedDocument: ScannedDocument) {
        return appDb.scannedDocDao().insert(scannedDocument)
    }

    override suspend fun delete(scannedDocument: List<ScannedDocument>) {
        return appDb.scannedDocDao().delete(scannedDocument)
    }
}
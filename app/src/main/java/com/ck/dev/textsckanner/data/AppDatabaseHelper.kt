
package com.ck.dev.textsckanner.data

import androidx.lifecycle.LiveData

interface AppDatabaseHelper {
    suspend fun getDocuments(): List<ScannedDocument>
    suspend fun updateFileContent(docName:String,docTitle:String): Int
    fun getAllScannedDocLive(): LiveData<List<ScannedDocument>>
    suspend fun insert(scannedDocument: ScannedDocument)
    suspend fun delete(scannedDocument: List<ScannedDocument>)
}
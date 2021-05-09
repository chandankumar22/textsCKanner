package com.ck.dev.textsckanner.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ScannedDocumentDao {

    @Query("SELECT * FROM scanned_document")
    suspend fun getAllScannedDoc(): List<ScannedDocument>

    @Query("SELECT * FROM scanned_document")
    fun getAllScannedDocLive(): LiveData<List<ScannedDocument>>

    @Query("UPDATE scanned_document SET documentTitle = :docTitle WHERE documentName = :docName")
    suspend fun updateFileContent(docName:String,docTitle:String): Int

    @Insert
    suspend fun insert(document: ScannedDocument)

    @Delete
    suspend fun delete(document: List<ScannedDocument>)

}
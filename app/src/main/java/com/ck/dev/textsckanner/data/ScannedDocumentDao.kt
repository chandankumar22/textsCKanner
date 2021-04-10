package com.ck.dev.textsckanner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScannedDocumentDao {

    @Query("SELECT * FROM scanned_document")
    suspend fun getAllScannedDoc(): List<ScannedDocument>

    @Insert
    suspend fun insert(document: ScannedDocument)

    @Delete
    suspend fun delete(document: ScannedDocument)

}
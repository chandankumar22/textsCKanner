package com.ck.dev.textsckanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_document")
data class ScannedDocument (
    @PrimaryKey
    @ColumnInfo(name = "documentName") var documentName: String,
    @ColumnInfo(name = "documentTitle") val documentTitle: String?,
    @ColumnInfo(name = "documentImagePath") val imgPath: String?,
    @ColumnInfo(name = "documentPath") val docPath: String?,
    @ColumnInfo(name = "documentExt") val extension: String?,
    @ColumnInfo(name = "creationDate") val creationDate: String?
)
package com.ck.dev.textsckanner.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScannedDocument::class], version = 1)
abstract class AppDatabase :RoomDatabase() {

    abstract fun scannedDocDao():ScannedDocumentDao
}
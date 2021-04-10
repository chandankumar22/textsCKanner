package com.ck.dev.textsckanner.data

interface AppDatabaseHelper {
    suspend fun getUsers(): List<ScannedDocument>

    suspend fun insert(users: ScannedDocument)
}
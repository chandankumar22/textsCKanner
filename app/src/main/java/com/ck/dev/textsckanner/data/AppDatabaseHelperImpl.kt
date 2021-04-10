package com.ck.dev.textsckanner.data

class AppDatabaseHelperImpl(private val appDb:AppDatabase):AppDatabaseHelper {
    override suspend fun getUsers(): List<ScannedDocument> {
        TODO("Not yet implemented")
    }

    override suspend fun insert(users: ScannedDocument) {
        TODO("Not yet implemented")
    }
}
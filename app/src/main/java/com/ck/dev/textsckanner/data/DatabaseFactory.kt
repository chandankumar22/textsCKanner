package com.ck.dev.textsckanner.data

import android.content.Context
import androidx.room.Room
import com.ck.dev.textsckanner.utils.Constants

object DatabaseFactory {
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class) {
                INSTANCE = buildRoomDB(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildRoomDB(context: Context) =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            Constants.APP_DB_NAME
        ).build()
}
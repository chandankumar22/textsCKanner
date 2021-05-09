package com.ck.dev.textsckanner.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.ck.dev.textsckanner.BuildConfig
import com.ck.dev.textsckanner.ui.Application
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException


fun AppCompatActivity.readFileFromInternal(dirName: String, fileName: String): String? {
    return try {
        val os = getDir(dirName, MODE_PRIVATE)
        val file = File(os, fileName)
        file.readText()
    } catch (ex: Exception) {
        Timber.e(ex, "Exception when reading file: $fileName from $dirName")
        null
    }
}

fun AppCompatActivity.writeFileInInternal(
    dirName: String,
    fileName: String,
    data: String
): String? {
    return try {
        val os = getDir(dirName, MODE_PRIVATE)
        val file = File(os, fileName)
        file.writeText(data)
        os.path
    } catch (ex: Exception) {
        Timber.e(ex, "Exception when writing file: $fileName from $dirName")
        null
    }
}

fun AppCompatActivity.writeImageInInternal(
    dirName: String,
    fileName: String,
    bitmap: Bitmap
): String? {
    return try {
        val file = getDir(dirName, MODE_PRIVATE)
        val os = File(file, fileName)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        os.writeBytes(baos.toByteArray())
        os.path
    } catch (ex: IOException) {
        Timber.e(ex, "Exception when writing image: $fileName from $dirName")
        null
    }
}

fun deleteImageInInternal(imagePath: String): Boolean {
    return try {
        // val file = getDir(dirName, MODE_PRIVATE)
        val os = File(imagePath)
        if (os.exists()) {
            os.delete()
        }
        true
    } catch (ex: IOException) {
        Timber.e(ex, "Exception when deleting file at  $imagePath")
        false
    }
}

fun Context.readImageFileFromInternal(path: String): Bitmap? {
    return try {
        //    val file = getDir(dirName, MODE_PRIVATE)
        val op = FileInputStream(File(path))
        val bmp = BitmapFactory.decodeStream(op)
        return bmp
    } catch (ex: Exception) {
        Timber.e(ex, "Exception when reading image from: $path")
        null
    }
}

fun AppCompatActivity.shareDocuments(docPath: List<String>, mimeType: String) {
    try {
        val listOfFileUris = arrayListOf<Uri>()
        docPath.forEach {
            val file = File(it)
            Timber.d("-------------newFile:${file.exists()}")
            val data =
                FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
            listOfFileUris.add(data)
        }
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setType(mimeType)
        shareIntent.action = Intent.ACTION_SEND_MULTIPLE
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, listOfFileUris)
        val intent = Intent.createChooser(shareIntent, "Share using")
        startActivity(intent)
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "document_share_success")
        }
        (application as Application).firebaseAnalytics.logEvent("document_share_success", bundle)
    } catch (ex: java.lang.Exception) {
        Timber.e(ex, "document share failed")
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "document_share_failed")
        }
        (application as Application).firebaseAnalytics.logEvent("document_share_failed", bundle)
    }
}

fun AppCompatActivity.shareText(shareBody: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        ContextCompat.startActivity(
            this,
            Intent.createChooser(
                intent,
                "Share Using"

            ), null
        )
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "text_share_success")
        }
        (application as Application).firebaseAnalytics.logEvent("text_share_success", bundle)
    } catch (ex: java.lang.Exception) {
        Timber.e(ex, "text share failed")
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "text_share_failed")
        }
        (application as Application).firebaseAnalytics.logEvent("text_share_failed", bundle)
    }

}
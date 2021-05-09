package com.ck.dev.textsckanner.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.ck.dev.textsckanner.R
import java.io.File
import java.io.IOException
import java.io.PrintWriter


object StorageUtils {

    private fun writeToFile(context: Context, filename: String){
        val sd_main = File("${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)} /ImageToText")
        var success = true
        if (!sd_main.exists())
            success = sd_main.mkdir()

        if (success) {
            val sd = File("$filename.txt")

            if (!sd.exists())
                success = sd.mkdir()

            if (success) {
                // directory exists or already created
                val dest = File(sd, filename)
                try {
                    // response is the data written to file
                    PrintWriter(dest)
                //    createDocFile("${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)} /ImageToText",filename)
                } catch (e: Exception) {
                    // handle the exception
                }
            }
        } else {
            // directory creation is not successful
        }
    }

    fun createAppFolderInInternalStorage(context: Context){
        val appName = context.getAppName()
        val file = File(Environment.getExternalStorageState(), "test.txt")
        val path =
            Environment.getDataDirectory().absolutePath.toString() + "/storage/emulated/0/$appName"
        val mFolder = File(path)
        if (!file.exists()) {
            mFolder.mkdir()
        }
       /// val rootAndroidDirectory = Environment.getExternalStorageDirectory()
        val directory = File(file,appName)
        directory.mkdirs()
    }

    fun isExternalStorageReadOnly(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
    }

    fun isExternalStorageAvailable(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == extStorageState
    }

    private fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()


    private fun createOrGetFile(
        destination: File, // e.g., /storage/emulated/0/Android/data/
        fileName: String, // e.g., tripBook.txt
        folderName: String
    )// e.g., bookTrip
            : File {
        val folder = File(destination, folderName)
        // file path = /storage/emulated/0/Android/data/bookTrip/tripBook.txt
        return File(folder, fileName)
    }


    // ----------------------------------
    // READ & WRITE STORAGE
    // ----------------------------------
    private fun readFile(context: Context, file: File): String {
        val sb = StringBuilder()
        if (file.exists()) {
            try {
                val bufferedReader = file.bufferedReader();
                bufferedReader.useLines { lines ->
                    lines.forEach {
                        sb.append(it)
                        sb.append("\n")
                    }
                }
            } catch (e: IOException) {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_happened),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return sb.toString()
    }

    // ---
    private fun writeFile(context: Context, text: String, file: File) {

        try {
            file.parentFile?.mkdirs()
            file.bufferedWriter().use { out ->
                out.write(text)
            }
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.error_happened), Toast.LENGTH_LONG)
                .show()
            return
        }

        Toast.makeText(context, context.getString(R.string.saved), Toast.LENGTH_LONG).show()
    }




}
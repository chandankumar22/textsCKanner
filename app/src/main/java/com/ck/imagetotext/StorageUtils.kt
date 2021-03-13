package com.ck.imagetotext

import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.IOException

object StorageUtils {


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
            file.parentFile.mkdirs()
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
package com.ck.dev.textsckanner.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.*

object Utility {

    var detectedText:String = ""
    lateinit var bitmap: Bitmap

    fun createLocalDirs(context: Context) {
        /*val rootInternalDir =
            File(context.filesDir.toString() + "/" + APP_PRIVATE_DIR)
        val makePvtDirSuccess: Boolean
        if (!rootInternalDir.exists()) {
            makePvtDirSuccess = rootInternalDir.mkdirs()
            if (makePvtDirSuccess) {
                APP_PRIVATE_STORAGE_DIR =
                    context.filesDir.toString() + "/" + APP_PRIVATE_DIR
            } else {
                Timber.d("Failed to create")
            }
        } else {
            APP_PRIVATE_STORAGE_DIR =
                context.filesDir.toString() + "/" + APP_PRIVATE_DIR
            Timber.d("App private dir path already exists")
        }*/
    }

    fun getImageUri(context: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    fun savePhotoToStorage(context: Context,bitmap: Bitmap) {
        //Generating a file name
        val filename = "IMG${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            context.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            context.showToast("Saved to Photos")
        }
    }

     fun AppCompatActivity.saveImageToInternal(bitmap: Bitmap){
        val imageName = "IMG${System.currentTimeMillis()}.jpg"
         try{
             val os = openFileOutput(imageName, Context.MODE_PRIVATE)
             val baos = ByteArrayOutputStream()
             bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos) // It can be also saved it as JPEG
             os.write(baos.toByteArray())
         }catch (ex:IOException){
             Timber.e(ex, "Saving image failed with")
         }

//        try {
//            val existingProfilePicPath = File(APP_PRIVATE_STORAGE_DIR, imageName)
//            if (existingProfilePicPath.exists()) {
//                existingProfilePicPath.delete()
//            }
//            if (!existingProfilePicPath.exists()) {
//                File(APP_PRIVATE_STORAGE_DIR).mkdirs()
//            } else {
//                existingProfilePicPath.delete()
//            }
//
//            val outputStream = FileOutputStream(existingProfilePicPath)
//            GlobalScope.launch {
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                outputStream.close()
//            }
//        } catch (e: FileNotFoundException) {
//            Timber.e(e, "Saving image failed with")
//        } catch (e: IOException) {
//            Timber.e(e, "Saving image failed with")
//        }
    }
}
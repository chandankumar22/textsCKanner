package com.ck.imagetotext

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import java.io.*
import java.io.FileOutputStream as FileOutputStream1


class MainActivity : AppCompatActivity() {
    private val REQUEST_GALLERY = 0
    private val REQUEST_CAMERA = 1
    private val MY_PERMISSIONS_REQUESTS = 0

    private val TAG = MainActivity::class.java.simpleName

    private var imageUri: Uri? = null
    private var detectedTextView: TextView? = null

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUESTS -> {
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // FIXME: Handle this case the user denied to grant the permissions
                }
            }
            else -> {
            }
        }
    }

    private fun requestPermissions() {
        val requiredPermissions: ArrayList<String> = ArrayList()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.CAMERA)
        }
        if (!requiredPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(),
                    MY_PERMISSIONS_REQUESTS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
        findViewById<View>(R.id.choose_from_gallery).setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_GALLERY)
        }
        findViewById<View>(R.id.take_a_photo).setOnClickListener {
            val filename = System.currentTimeMillis().toString() + ".jpg"
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, filename)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            val intent = Intent()
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, REQUEST_CAMERA)
        }
        detectedTextView = findViewById<View>(R.id.detected_text) as TextView
        detectedTextView!!.movementMethod = ScrollingMovementMethod()
    }

    private fun inspectFromBitmap(bitmap: Bitmap?) {
        val textRecognizer: TextRecognizer = TextRecognizer.Builder(this).build()
        try {
            if (!textRecognizer.isOperational()) {
                /* TextRecognizer.Builder(this).setMessage("Text recognizer could not be set up on " +
                         "your device").show()*/
                return
            }
            val frame = Frame.Builder().setBitmap(bitmap).build()
            val origTextBlocks: SparseArray<TextBlock> = textRecognizer.detect(frame)
            val textBlocks: MutableList<TextBlock> = ArrayList()
            for (i in 0 until origTextBlocks.size()) {
                val textBlock: TextBlock = origTextBlocks.valueAt(i)
                textBlocks.add(textBlock)
            }
            textBlocks.sortWith(Comparator { o1, o2 ->
                val diffOfTops = o1!!.boundingBox.top - o2!!.boundingBox.top
                val diffOfLefts = o1.boundingBox.left - o2.boundingBox.left
                if (diffOfTops != 0) {
                    diffOfTops
                } else diffOfLefts
            })
            val detectedText = StringBuilder()
            for (textBlock in textBlocks) {
                if (textBlock != null && textBlock.getValue() != null) {
                    detectedText.append(textBlock.getValue())
                    detectedText.append("\n")
                }
            }
            val input = EditText(this@MainActivity)
            val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            lp.setMargins(12,0,12,0)
            input.setLayoutParams(lp)
            val builder = AlertDialog.Builder(this)
            builder.setTitle("File name?").setView(input)
            builder.setMessage("Enter file name")
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                if (input.text.isNullOrEmpty()) {
                    Toast.makeText(this, "Dude enter the file name", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                writeToFile(input.text.toString())
                Toast.makeText(this, "$filesDir /ImageToText/${input.text}", Toast.LENGTH_LONG).show()
            }.setCancelable(false)
            builder.show()




            detectedTextView!!.text = detectedText
        } finally {
            textRecognizer.release()
        }
    }


    fun createDocFile(file:String,text:String){
        val doc = XWPFDocument()
        try {
            val paragraph: XWPFParagraph = doc.createParagraph()
            val run: XWPFRun = paragraph.createRun()
            run.setText(text)
            val f = FileOutputStream1(file)
            doc.write(f)
            f.flush()
            f.close()
        } catch (e: java.lang.Exception) {
            println(e.message)
        }
    }


    private fun writeToFile(filename:String){
        val sd_main = File("${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)} /ImageToText")
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
                    createDocFile("${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)} /ImageToText",filename)
                } catch (e: Exception) {
                    // handle the exception
                }
            }
        } else {
            // directory creation is not successful
        }
    }

    private fun inspect(uri: Uri?) {
        var inpS: InputStream? = null
        var bitmap: Bitmap? = null
        try {
            inpS = contentResolver.openInputStream(uri!!)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            options.inSampleSize = 2
            options.inScreenDensity = DisplayMetrics.DENSITY_LOW
            bitmap = BitmapFactory.decodeStream(inpS, null, options)
            inspectFromBitmap(bitmap)
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "Failed to find the file: $uri", e)
        } finally {
            bitmap?.recycle()
            if (inpS != null) {
                try {
                    inpS.close()
                } catch (e: IOException) {
                    Log.w(TAG, "Failed to close InputStream", e)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK) {
                inspect(data?.data)
            }
            REQUEST_CAMERA -> if (resultCode == Activity.RESULT_OK) {
                if (imageUri != null) {
                    inspect(imageUri)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
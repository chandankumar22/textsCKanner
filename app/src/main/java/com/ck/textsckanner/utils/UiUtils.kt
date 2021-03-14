package com.ck.textsckanner.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.util.SparseArray
import android.widget.Toast
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer

object UiUtils {

    fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    fun Image.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun Context.inspectFromBitmap(bitmap: Bitmap): String {
        val textRecognizer: TextRecognizer = TextRecognizer.Builder(this).build()
        try {
            if (!textRecognizer.isOperational()) {
                /* TextRecognizer.Builder(this).setMessage("Text recognizer could not be set up on " +
                         "your device").show()*/
                return ""
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
            /*val input = EditText(this@MainActivity)
            val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            lp.setMargins(12, 0, 12, 0)
            input.setLayoutParams(lp)
            val builder = AlertDialog.Builder(this)
            builder.setTitle("File name?").setView(input)
            builder.setMessage("Enter file name")
            builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                if (input.text.isNullOrEmpty()) {
                    Toast.makeText(this, "Dude enter the file name", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                writeToFile(input.text.toString())
                Toast.makeText(this, "$filesDir /ImageToText/${input.text}", Toast.LENGTH_LONG)
                    .show()
            }.setCancelable(false)
            builder.show()*/
            return String(detectedText)
        } finally {
            textRecognizer.release()
        }
    }
}
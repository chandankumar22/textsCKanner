package com.ck.textsckanner.ui.components.savetext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ck.textsckanner.R
import com.google.android.material.textview.MaterialTextView

class DetectedTextActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detected_text)
        with(intent.getStringExtra("text")){
            this?.let {
                with(findViewById<MaterialTextView>(R.id.detected_text)){
                    text = it
                }
            }
        }
    }
}
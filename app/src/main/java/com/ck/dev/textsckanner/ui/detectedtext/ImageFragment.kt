package com.ck.dev.textsckanner.ui.detectedtext

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ck.dev.textsckanner.R
import com.ck.dev.textsckanner.utils.Utility.bitmap
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.activity_detected_text.*
import kotlinx.android.synthetic.main.fragment_image.*

class ImageFragment : Fragment(R.layout.fragment_image) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<MaterialTextView>(R.id.edit_btn).visibility = View.GONE
        image_on_detect.setImageBitmap(bitmap)
    }
}
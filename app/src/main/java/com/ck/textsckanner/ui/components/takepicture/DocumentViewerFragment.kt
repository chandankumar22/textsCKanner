package com.ck.textsckanner.ui.components.takepicture

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ck.textsckanner.R

class DocumentViewerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_document_viewer, container, false)
    }



    companion object {
        fun newInstance(param1: String, param2: String) =
            DocumentViewerFragment().apply {}
    }
}
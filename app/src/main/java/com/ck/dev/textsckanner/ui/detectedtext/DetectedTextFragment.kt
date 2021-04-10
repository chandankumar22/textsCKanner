package com.ck.dev.textsckanner.ui.detectedtext

import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.ck.dev.textsckanner.R
import com.ck.dev.textsckanner.utils.Utility
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.fragment_detected_text.*
import timber.log.Timber


class DetectedTextFragment : Fragment(R.layout.fragment_detected_text) {

    private var isEditableTv = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detected_text.text = Utility.detectedText
        setListeners()
        val screenDefaultPd =resources.getDimension(R.dimen.screen_default_pd).toInt()
        val topPd = requireActivity().findViewById<TabLayout>(R.id.tab_layout).height + screenDefaultPd
        val botPd = requireActivity().findViewById<ConstraintLayout>(R.id.detect_footer_view).height+ screenDefaultPd
        Timber.d("\nscreenDefaultPd={$screenDefaultPd} \ntopPd={$topPd} \nbotPd={$botPd}")
        requireView().rootView.setPadding(screenDefaultPd, topPd, screenDefaultPd, botPd)
    }


    private fun setListeners() {
        requireActivity().findViewById<MaterialTextView>(R.id.edit_btn).apply {
            setOnClickListener {
                if (!isEditableTv) {
                    text = "Edit"
                    isEditableTv = !isEditableTv
                    detected_text.visibility = VISIBLE
                    editable_text.visibility = View.GONE
                    detected_text.text = Utility.detectedText
                } else {
                    text = "Save"
                    isEditableTv = !isEditableTv
                    detected_text.visibility = View.GONE
                    editable_text.visibility = VISIBLE
                    editable_text.setText(Utility.detectedText)
                    editable_text.setSelection(0)
                }
            }
            editable_text.doAfterTextChanged {
                Utility.detectedText = it.toString()
            }
        }
    }
}
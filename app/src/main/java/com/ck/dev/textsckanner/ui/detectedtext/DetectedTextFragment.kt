package com.ck.dev.textsckanner.ui.detectedtext

import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.ck.dev.textsckanner.R
import com.ck.dev.textsckanner.ui.Application
import com.ck.dev.textsckanner.utils.Utility
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.fragment_detected_text.*
import timber.log.Timber


class DetectedTextFragment : Fragment(R.layout.fragment_detected_text) {

    private var isEditableTv = true
    lateinit var viewModel:DetectedTextVm

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated called")
        super.onViewCreated(view, savedInstanceState)

        /*val screenDefaultPd =resources.getDimension(R.dimen.screen_default_pd).toInt()
        val topPd = requireActivity().findViewById<TabLayout>(R.id.tab_layout).height + screenDefaultPd
        val botPd = requireActivity().findViewById<LinearLayout>(R.id.detect_footer_view).height+ screenDefaultPd
        Timber.d("\nscreenDefaultPd={$screenDefaultPd} \ntopPd={$topPd} \nbotPd={$botPd}")
        requireView().rootView.setPadding(screenDefaultPd, topPd, screenDefaultPd, botPd)*/
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Timber.i("onActivityCreated called")
        super.onActivityCreated(savedInstanceState)
        viewModel = Application.viewModel
        detected_text.text = Utility.detectedText
        setListeners()
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
                    with(requireActivity()){
                        findViewById<MaterialTextView>(R.id.retry_scan).visibility = View.VISIBLE
                        findViewById<MaterialTextView>(R.id.save_as_doc).visibility = View.VISIBLE
                    }
                } else {
                    text = "Done"
                    isEditableTv = !isEditableTv
                    detected_text.visibility = View.GONE
                    editable_text.visibility = VISIBLE
                    editable_text.setText(Utility.detectedText)
                    editable_text.setSelection(0)
                    with(requireActivity()){
                        findViewById<MaterialTextView>(R.id.retry_scan).visibility = View.GONE
                        findViewById<MaterialTextView>(R.id.save_as_doc).visibility = View.GONE
                    }
                }
            }
            editable_text.doAfterTextChanged {
                Timber.i("doAfterTextChanged called")
                Utility.detectedText = it.toString()
                viewModel.documentText=it.toString()
            }
        }
    }
}
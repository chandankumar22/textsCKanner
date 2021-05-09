package com.ck.dev.textsckanner.ui.detectedtext

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ck.dev.textsckanner.R

class DetectedTextAdapter(private val mContext: FragmentActivity) :
    FragmentStateAdapter(
        mContext
    ) {

    companion object {
        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.detected_text_tab_1,
            R.string.detected_text_tab_2
        )
    }

    override fun getItemCount(): Int {
        return TAB_TITLES.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DetectedTextFragment()
            1 -> ImageFragment()
           /* 2 -> TextOnImageOverlayFragment()*/
            else -> null!!
        }
    }
}
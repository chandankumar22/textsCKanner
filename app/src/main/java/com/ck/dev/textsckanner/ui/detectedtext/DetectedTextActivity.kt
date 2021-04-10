package com.ck.dev.textsckanner.ui.detectedtext

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.ck.dev.textsckanner.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_detected_text.*


class DetectedTextActivity : AppCompatActivity() {

    private lateinit var tab1view: TabLayout.TabView
    private lateinit var tab2view: TabLayout.TabView
    private lateinit var tab3view: TabLayout.TabView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detected_text)
        view_pager.adapter = DetectedTextAdapter(this)
        val tabLayoutStrategy =
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = getString(R.string.detected_text_tab_1)
                        tab1view = tab.view
                    }
                    1 -> {
                        tab.text = getString(R.string.detected_text_tab_2)
                        tab2view = tab.view
                    }
                    2 -> {
                        tab.text = getString(R.string.detected_text_tab_3)
                        tab3view = tab.view
                    }
                }
            }
        val tabLayoutMediator =
            TabLayoutMediator(tab_layout, view_pager, tabLayoutStrategy)
        tabLayoutMediator.attach()

        for (i in 0 until tab_layout.tabCount) {
            val tab: TabLayout.Tab? = tab_layout.getTabAt(i)
            if (tab != null) {
                val tabTextView = TextView(this)
                tab.customView = tabTextView
                tabTextView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                tabTextView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                tabTextView.text = tab.text
                if (i == 0) {
                    tabTextView.textSize = 16f
                }
            }
        }


        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val vg = tab_layout.getChildAt(0) as (ViewGroup)
                val vgTab = vg.getChildAt(tab.position) as ViewGroup
                val tabChildCount = vgTab.childCount
                for (i in 0 until tabChildCount) {
                    val tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild is TextView) {
                        tabViewChild.textSize = 16f
                        tabViewChild.setTextColor(
                            ContextCompat.getColor(
                                this@DetectedTextActivity,
                                android.R.color.white
                            )
                        )
                        tabViewChild.typeface = Typeface.DEFAULT_BOLD;
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val vg = tab_layout.getChildAt(0) as ViewGroup
                val vgTab = vg.getChildAt(tab.getPosition()) as ViewGroup
                val tabChildsCount = vgTab.getChildCount();
                for (i in 0 until tabChildsCount) {
                    val tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild is TextView) {
                        tabViewChild.setTextSize(14f)
                        tabViewChild.setTextColor(
                            ContextCompat.getColor(
                                this@DetectedTextActivity,
                                android.R.color.white
                            )
                        )
                    }
                }
            }
        })

        view_pager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setRectBorders(position)
            }
        })
    }

    private fun setRectBorders(position: Int) {
        when (position) {
            0 -> {
                tab1view.background =
                    ContextCompat.getDrawable(this, R.drawable.shape_rectangle_with_border)
                tab2view.background = null
                tab3view.background = null
                edit_btn.visibility = View.VISIBLE
            }
            1 -> {
                tab2view.background =
                    ContextCompat.getDrawable(this, R.drawable.shape_rectangle_with_border)
                tab1view.background = null
                tab3view.background = null
                edit_btn.visibility = View.GONE
            }
            2 -> {
                tab3view.background =
                    ContextCompat.getDrawable(this, R.drawable.shape_rectangle_with_border)
                tab2view.background = null
                tab1view.background = null
                edit_btn.visibility = View.GONE
            }
        }
    }
}
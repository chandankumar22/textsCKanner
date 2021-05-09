/*
 *         Copyright (C) 2019 NEC CORPORATION
 *
 * ALL RIGHTS RESERVED BY NEC CORPORATION, THIS PROGRAM
 * MUST BE USED SOLELY FOR THE PURPOSE FOR WHICH IT WAS
 * FURNISHED BY NEC CORPORATION, NO PART OF THIS PROGRAM
 * MAY BE REPRODUCED OR DISCLOSED TO OTHERS, IN ANY FORM
 * WITHOUT THE PRIOR WRITTEN PERMISSION OF NEC CORPORATION.
 * USE OF COPYRIGHT NOTICE DOES NOT EVIDENCE PUBLICATION
 * OF THE PROGRAM
 *
 *            NEC CONFIDENTIAL AND PROPRIETARY
 *
 */
package com.ck.dev.textsckanner.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

internal class BottomNavigationBehavior(
    context: Context,
    attrs: AttributeSet
) : CoordinatorLayout.Behavior<ConstraintLayout>(context, attrs) {


    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: ConstraintLayout,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL

    }
    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: ConstraintLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        child.translationY =
            0.0f.coerceAtLeast(child.height.toFloat().coerceAtMost(child.translationY + dy))
    }
}
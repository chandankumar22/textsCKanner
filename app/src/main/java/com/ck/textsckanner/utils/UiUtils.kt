package com.ck.textsckanner.utils

import android.content.Context
import android.widget.Toast

object UiUtils {

    fun Context.toast(msg:String) = Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
}
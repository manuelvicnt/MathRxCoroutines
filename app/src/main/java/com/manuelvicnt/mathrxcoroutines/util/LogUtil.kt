package com.manuelvicnt.mathrxcoroutines.util

import android.util.Log

object LogUtil {

    fun logMessage(msg: String) {
        Log.v("MathApp", "[${Thread.currentThread().name}] $msg")
    }
}
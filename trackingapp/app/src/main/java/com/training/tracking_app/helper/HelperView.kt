package com.training.tracking_app.helper

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.training.tracking_app.R

fun Toast.showCustomToast(message: String, activity: Activity){
    val layout = activity.layoutInflater.inflate (R.layout.toast_trackin,activity.findViewById(R.id.toast_container))
    val textView = layout.findViewById<TextView>(R.id.toast_text)
    textView.text = message
    this.apply {
        duration = Toast.LENGTH_SHORT
        view = layout
        show()
    }
}
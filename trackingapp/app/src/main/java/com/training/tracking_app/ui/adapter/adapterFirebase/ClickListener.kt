package com.training.tracking_app.ui.adapter.adapterFirebase

import android.view.View
import com.training.tracking_app.data.Travel

interface ClickListener {

    fun onClickTravel(view: View, travel: Travel)
}
package com.training.tracking_app.ui.adapter.adapterFirebase

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.training.tracking_app.DtoFirestore.TravelDto
import com.training.tracking_app.R
import com.training.tracking_app.data.Travel
import com.training.tracking_app.databinding.NotificationItemBinding

class TravelViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    val binding = NotificationItemBinding.bind(view)

    fun render(t : Travel){
        if(t.status) {
            binding.ivIcon.setImageResource(R.mipmap.marker_track)
            binding.tvTitle.setTextColor(ContextCompat.getColor(view.context, R.color.app_primary))
        }else{
            binding.tvTitle.setTextColor(ContextCompat.getColor(view.context, R.color.app_default))
        }
        binding.tvTitle.text = "${t.code.toUpperCase()}"
//        binding.tvDescription.text = t.status.toString()
//        binding.tvDate.text = "TEST"
    }
}
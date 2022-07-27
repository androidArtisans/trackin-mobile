package com.training.tracking_app.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.training.tracking_app.Dto.NotificacionDto
import com.training.tracking_app.DtoLaravel.Trackin
import com.training.tracking_app.databinding.NotificationItemBinding

class NotificacionViewHolder(val view : View) : RecyclerView.ViewHolder(view) {
    private val binding = NotificationItemBinding.bind(view)

    fun render(notificacion : Trackin){
        binding.tvTitle.text = "- ${notificacion.message_type.toUpperCase()} -"
        binding.tvDescription.text = notificacion.message
        binding.tvDate.text = notificacion.register_point_date
    }
}
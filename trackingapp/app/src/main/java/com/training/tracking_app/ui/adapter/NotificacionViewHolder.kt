package com.training.tracking_app.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.training.tracking_app.Dto.NotificacionDto
import com.training.tracking_app.databinding.NotificationItemBinding

class NotificacionViewHolder(val view : View) : RecyclerView.ViewHolder(view) {
    private val binding = NotificationItemBinding.bind(view)

    fun render(notificacion : NotificacionDto){
        binding.tvTitle.text = notificacion.title
        binding.tvDescription.text = notificacion.description
        binding.tvDate.text = notificacion.timeNotificaciont
    }
}
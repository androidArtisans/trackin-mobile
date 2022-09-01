package com.training.tracking_app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.training.tracking_app.Dto.NotificacionDto
import com.training.tracking_app.DtoLaravel.Trackin
import com.training.tracking_app.R

class NotificacionsAdapter(val notificacions:List<Trackin>) : RecyclerView.Adapter<NotificacionViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NotificacionViewHolder(layoutInflater.inflate(R.layout.notification_item, parent, false))
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val item = notificacions[position]
        //holder.render(item)
    }

    override fun getItemCount(): Int  = notificacions.size

}
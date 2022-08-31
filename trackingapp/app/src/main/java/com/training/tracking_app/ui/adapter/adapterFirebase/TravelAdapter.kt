package com.training.tracking_app.ui.adapter.adapterFirebase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.training.tracking_app.R
import com.training.tracking_app.data.Travel

class TravelAdapter(val notificacions:List<Travel>) : RecyclerView.Adapter<TravelViewHolder>(){
    private lateinit var clickListener: ClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TravelViewHolder(layoutInflater.inflate(R.layout.notification_item, parent, false))
    }

    fun setClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val item = notificacions[position]
        holder.binding.container.setOnClickListener {
            clickListener.onClickTravel(holder.itemView, item)
        }
        holder.render(item)
    }

    override fun getItemCount(): Int  = notificacions.size

}
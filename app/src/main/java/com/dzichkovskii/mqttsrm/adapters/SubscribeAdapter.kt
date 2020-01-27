package com.dzichkovskii.mqttsrm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dzichkovskii.mqttsrm.R
import kotlinx.android.synthetic.main.item_subscribe_message.view.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttTopic


class SubscribeAdapter(private var message: MutableList<String>,
                       private val topic: String,
                       private val context: Context?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_subscribe_message, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //implementing bind
            when (holder) {
                is ViewHolder -> holder.bind(message[position], position)
            }
    }

    override fun getItemCount(): Int = message.size

    fun submitList(dataList: MutableList<String>){
        message = dataList
    }

    inner class ViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val subscribeMessage: TextView = view.rv_tv_subscribe_message
        private val subscribeTopic: TextView = view.rv_tv_subscribe_topic_name

        fun bind(message: String, position: Int) {
            subscribeMessage.text = message[position].toString()
            subscribeTopic.text = topic

        }
    }
}

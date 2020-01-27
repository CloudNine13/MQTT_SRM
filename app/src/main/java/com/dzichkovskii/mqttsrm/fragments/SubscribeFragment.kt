/**
 * @author Igor Dzichkovskii
 */

package com.dzichkovskii.mqttsrm.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzichkovskii.mqttsrm.R
import com.dzichkovskii.mqttsrm.adapters.SubscribeAdapter
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_subscribe.view.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class SubscribeFragment : Fragment() {

    companion object {

        fun passMQTTAndroidClientToSubscribe(mqttAndroidClient: MqttAndroidClient): SubscribeFragment {
            val fragment = SubscribeFragment()
            this.mqttAndroidClient = mqttAndroidClient
            return fragment
        }

        private var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(null, null, null)
        private lateinit var recyclerView: RecyclerView
        private lateinit var viewAdapter: SubscribeAdapter

        const val TAG = "SubscribeFragment"
    }

    private var checkedOption: Int = 0 //Default value of qos
    val listOfMessages = mutableListOf<String>()
    val topic = "test"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_subscribe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.chip_group_subscribe?.setOnCheckedChangeListener { _, checkedId: Int ->
            val chip: Chip? = view.findViewById(checkedId)
            val qos = chip?.text.toString().toInt()
            checkedOption = qos

            Log.d(TAG, "Checked option passed with value $checkedOption")
        }

        //initRecyclerView()

        view.findViewById<Button>(R.id.btn_subscribe).setOnClickListener {
            subscribe()
        }
    }

    private fun subscribe(){

        //val inputTopic = view?.findViewById(R.id.et_subscribe_topic) as EditText
        //val topic = inputTopic.text.toString()


        try {
            Log.d(TAG, "Checked option in subscribe method is $checkedOption")
            mqttAndroidClient.subscribe(topic,
                checkedOption, object : IMqttMessageListener {
                    override fun messageArrived(topic: String?, message: MqttMessage) {

                        //val data = String(message.payload, charset("UTF-8"))

                        //listOfMessages.add(data)
                        //viewAdapter.submitList(listOfMessages)
                        //viewAdapter.notifyDataSetChanged()

                        //view?.findViewById<TextView>(R.id.rv_tv_subscribe_topic_name)?.text = topic

                    }
                })
        } catch (e: Exception){}
    /**
     * Two methods to hide the keyboard after the result is shown
     */
    }

    fun initRecyclerView(){
        recyclerView = view!!.findViewById<RecyclerView>(R.id.rv_subscribe).apply {
            layoutManager = LinearLayoutManager(activity)

            viewAdapter = SubscribeAdapter(listOfMessages, topic, context)
            adapter = viewAdapter
        }
    }
}

/**
 * @author Igor Dzichkovskii
 */

package com.dzichkovskii.mqttsrm.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dzichkovskii.mqttsrm.R
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_subscribe.view.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttException

class SubscribeFragment : Fragment() {

    companion object {
        const val TAG = "SubscribeFragment"
    }

    private lateinit var mqttAndroidClient: MqttAndroidClient
    private var checkedOption: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_subscribe, container, false)

        root.findViewById<Button>(R.id.btn_subscribe).setOnClickListener {
            subscribe()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.chip_group?.setOnCheckedChangeListener { _, checkedId: Int ->
            val chip: Chip? = view.findViewById(checkedId)
            val qos = chip?.text.toString().toInt()
            checkedOption = qos

            Log.d(TAG, "Checked option passed with value $checkedOption")
        }
    }


        private fun subscribe(){

            val topic = "123"

            Log.d(TAG, "Checked option in subscribe is $checkedOption")

            try {
                mqttAndroidClient.subscribe(topic, checkedOption, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        // Give your callback on Subscription here
                    }
                    override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                    ) {
                        // Give your subscription failure callback here
                    }
                })
            } catch (e: MqttException) {
                // Give your subscription failure callback here
            }
        }
    }

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
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dzichkovskii.mqttsrm.R
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_subscribe.view.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class SubscribeFragment : Fragment() {

    companion object {

        fun newInstance(mqttAndroidClient: MqttAndroidClient): SubscribeFragment {
            val fragment = SubscribeFragment()
            this.mqttAndroidClient = mqttAndroidClient
            return fragment
        }

        private var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(null, null, null)

        const val TAG = "SubscribeFragment"
        const val ON_SUCCESS = "You subscribed successfully."
        const val ON_FAILURE = "You didn't subscribed to topic. Probably this topic don't exist."
        const val CONNECTION_ERROR = "The topic don't exist or you have connection problems. " +
                "Check your internet connection or change the topic's name"
    }

    private var checkedOption: Int = 0 //Default value of qos

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_subscribe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.chip_group?.setOnCheckedChangeListener { _, checkedId: Int ->
            val chip: Chip? = view.findViewById(checkedId)
            val qos = chip?.text.toString().toInt()
            checkedOption = qos

            Log.d(TAG, "Checked option passed with value $checkedOption")
        }

        mqttAndroidClient.setCallback(
            object: MqttCallback {
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d(TAG, "Message arrived")
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.d(TAG, "Connection lost")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "Delivery completed")
                }

            }
        )

        view.findViewById<Button>(R.id.btn_subscribe).setOnClickListener {
            subscribe()
        }
    }

    private val connectFragment = ConnectFragment()

    private fun subscribe(){

        val inputTopic = view?.findViewById(R.id.et_topic) as EditText
        val topic = inputTopic.text.toString()
        //val topic = "test"

        try {
            Log.d(TAG, "Checked option in subscribe method is $checkedOption")
            mqttAndroidClient.subscribe(topic,
                checkedOption, context, object: IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            Toast.makeText(context, ON_SUCCESS, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Connected successfully")
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken,
                            exception: Throwable
                        ) {
                            Toast.makeText(context, ON_FAILURE, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Didn't connected")
                        }
                    })
        } catch (e: MqttException) {
            connectFragment.displayErrorMessage(CONNECTION_ERROR, view, this)
        }
    }
}

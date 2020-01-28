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
import androidx.fragment.app.Fragment
import com.dzichkovskii.mqttsrm.R
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_subscribe.view.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class PublishFragment : Fragment() {

    companion object {

        private var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(null, null, null)

        val fragment = SubscribeFragment()

        fun passMQTTAndroidClientToPublish(mqttAndroidClient: MqttAndroidClient): SubscribeFragment {
            this.mqttAndroidClient = mqttAndroidClient
            return fragment
        }

        const val TAG = "PublishFragment"
    }

    init {
        mqttAndroidClient.setCallback(object: MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
            }
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                fragment.update(message.toString(), topic.toString())
            }
            override fun connectionLost(cause: Throwable?) {
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }
        })
    }

    private var checkedOption: Int = 0 //Default value of qos

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_publish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.chip_group_subscribe?.setOnCheckedChangeListener { _, checkedId: Int ->
            val chip: Chip? = view.findViewById(checkedId)
            val qos = chip?.text.toString().toInt()
            checkedOption = qos

            Log.d(TAG, "Checked option passed with value $checkedOption")
        }

        view.findViewById<Button>(R.id.btn_publish).setOnClickListener {
            val topic = view.findViewById<EditText>(R.id.et_publish_topic).text.toString()
            publish(topic, checkedOption)
        }
    }

    private fun publish(topic: String, qos: Int) {
        val encodedPayload: ByteArray
        try {
            val data = view!!.findViewById<EditText>(R.id.et_publish_message).text.toString()
            encodedPayload = data.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            message.qos = qos
            mqttAndroidClient.publish(topic, message)
        } catch (e: Exception) {

        } catch (e: MqttException) {

        }
    }
}
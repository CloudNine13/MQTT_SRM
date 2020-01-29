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
import kotlinx.android.synthetic.main.fragment_publish.view.*
import kotlinx.android.synthetic.main.fragment_subscribe.view.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class PublishFragment : Fragment() {

    companion object {

        private var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(null, null, null)
        var isSubscribed = false

        fun passMQTTAndroidClientToPublish(mqttAndroidClient: MqttAndroidClient): PublishFragment {
            val fragment = PublishFragment()
            this.mqttAndroidClient = mqttAndroidClient
            return fragment
        }

        fun passIsSubscribedToPublish(isSubscribed: Boolean): PublishFragment {
            val fragment = PublishFragment()
            this.isSubscribed = isSubscribed
            return fragment
        }

        const val TAG = "PublishFragment"
        const val PUBLISH_SUCCESS = "You have sent your message"
        const val PUBLISH_FAILURE = "Your message haven't sent. Please, check your Internet connection"
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

        view.chip_group_publish.setOnCheckedChangeListener { _, checkedId: Int ->
            val chip: Chip? = view.findViewById(checkedId)
            val qos = chip?.text.toString().toInt()
            checkedOption = qos

            Log.d(TAG, "Checked option passed with value $checkedOption")
        }

        val buttonPublish = view.findViewById<Button>(R.id.btn_publish)

        buttonPublish.isEnabled = isSubscribed

        buttonPublish.setOnClickListener {
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
            mqttAndroidClient.publish(topic, message, qos, object : IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Toast.makeText(context, PUBLISH_SUCCESS, Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Toast.makeText(context, PUBLISH_FAILURE, Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {

        } catch (e: MqttException) {

        }
    }
}
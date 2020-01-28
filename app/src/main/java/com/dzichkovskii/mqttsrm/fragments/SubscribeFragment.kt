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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dzichkovskii.mqttsrm.R
import com.dzichkovskii.mqttsrm.interfaces.UIUpdaterInterface
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_subscribe.*
import kotlinx.android.synthetic.main.fragment_subscribe.view.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class SubscribeFragment : Fragment(), UIUpdaterInterface {

    companion object {
        fun passDataToSubscribe(mqttAndroidClient: MqttAndroidClient): SubscribeFragment {
            val fragment = SubscribeFragment()
            this.mqttAndroidClient = mqttAndroidClient
            return fragment
        }

        private var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(null, null, null)
        private var savedState: Bundle? = null
        const val TAG = "SubscribeFragment"
        const val STATE_TEXT = "Subscribe text"
        const val SET_GET_TEXT = "SetGet"
    }

    init{
        mqttAndroidClient.setCallback(object: MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
            }
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                update(message.toString(), topic.toString())
            }
            override fun connectionLost(cause: Throwable?) {
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }
        })
    }

    private lateinit var messageText: TextView
    private var checkedOption: Int = 0 //Default value of qos
    private lateinit var newText: String
    private val topic = "test"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_subscribe, container, false)

        messageText = root.findViewById(R.id.et_subscribe)

        if(savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(SET_GET_TEXT)
        }
        if (savedState != null) {
            messageText.text = savedState?.getCharSequence(SET_GET_TEXT)
        }
        savedState = null
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(savedInstanceState != null) {
            newText = savedInstanceState.getString(STATE_TEXT)!!
        }

        view.chip_group_subscribe?.setOnCheckedChangeListener { _, checkedId: Int ->
            val chip: Chip? = view.findViewById(checkedId)
            val qos = chip?.text.toString().toInt()
            checkedOption = qos

            Log.d(TAG, "Checked option passed with value $checkedOption")
        }


        view.findViewById<Button>(R.id.btn_subscribe).setOnClickListener {
            subscribe()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        savedState = saveState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(SET_GET_TEXT, if (savedState != null) savedState else saveState())
    }

    private fun saveState(): Bundle? {
        val state = Bundle()
        state.putCharSequence(SET_GET_TEXT, messageText.text.toString())
        return state
    }

    override fun update(message: String, topic: String) {

        val text = et_subscribe.text.toString()

        newText = """$text
            
Topic: $topic 
Message: $message
        """

        et_subscribe.setText(newText)
        et_subscribe.setSelection(et_subscribe.text.length)
    }

    private fun subscribe(){

        //val inputTopic = view?.findViewById(R.id.et_subscribe_topic) as EditText
        //val topic = inputTopic.text.toString()


        try {
            Log.d(TAG, "Checked option in subscribe method is $checkedOption")
            mqttAndroidClient.subscribe(topic,
                checkedOption, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Toast.makeText(context, "You have subscribed to topic", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }

                })
        } catch (e: Exception){}
    }
}

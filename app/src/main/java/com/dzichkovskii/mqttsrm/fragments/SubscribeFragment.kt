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
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dzichkovskii.mqttsrm.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.fragment_subscribe.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class SubscribeFragment : Fragment() {

    companion object {

        /**
         * This is the method to get MqttAndroidClient from ConnectFrament
         * @param mqttAndroidClient is the value we are getting from outside
         * @see ConnectFragment.connect
         */
        fun passMqttAndroidClientToSubscribe(mqttAndroidClient: MqttAndroidClient): SubscribeFragment {
            val fragment = SubscribeFragment()
            this.mqttAndroidClient = mqttAndroidClient
            return fragment
        }

        /**
         * This is the method to enable/disable the subscribe/unsubscribe button
         * @param isConnected is to get the state of the connection of the client
         * @see ConnectFragment.onDestroy
         */
        fun passIsConnectedToSubscribe(isConnected: Boolean): SubscribeFragment {
            val fragment = SubscribeFragment()
            this.isConnected = isConnected
            return fragment
        }

        /**
         * This is the method to get messages to show from the other fragments
         * @param topicList is the array of topics come outside
         * @param messageList is the array of messages come outside
         * @see PublishFragment.onDestroy
         */
        fun passArrayToSubscribe(topicList: ArrayList<String>, messageList: ArrayList<String>): SubscribeFragment{
            val fragment = SubscribeFragment()
            this.topicArray = topicList
            this.messageArray = messageList
            return fragment
        }

        private var topicArray = ArrayList<String>()
        private var messageArray = ArrayList<String>()
        private var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(null, null, null)
        private var isConnected = false
        private var isSubscribed = false
        private var savedState: Bundle? = null
        const val TAG = "SubscribeFragment"
        const val STATE_TEXT = "Subscribe text"
        const val SUCCESS_TEXT_UNSUBSCRIBE = "You have unsubscribed"
        const val FAILURE_TEXT_UNSUBSCRIBE = "Unsubscription went wrong, please try again"
        const val MESSAGE_TO_SAVE = "message"
        const val TOPIC_TO_SAVE = "topic"
    }

    private lateinit var messageText: TextView
    private lateinit var subscribeTopic: EditText
    private lateinit var subscribeError: TextView
    private lateinit var subscribeTopicForUnsubscribe: String
    private var checkedOption: Int = 0 //Default value of qos
    private lateinit var newText: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_subscribe, container, false)

        subscribeError = root.findViewById(R.id.tv_connect_error_message)

        val chipGroup = root.findViewById<ChipGroup>(R.id.cg_subscribe)

        messageText = root.findViewById(R.id.et_subscribe_messages)
        subscribeTopic = root.findViewById(R.id.et_subscribe_topic)

        chipGroup.setOnCheckedChangeListener { _, checkedId: Int ->
            val chip: Chip? = root.findViewById(checkedId)
            val qos = chip?.text.toString().toInt()
            checkedOption = qos

            Log.d(TAG, "Checked option passed with value $checkedOption")
        }

        if(savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(MESSAGE_TO_SAVE)
        }
        if (savedState != null) {
            val savedTopic = savedState!!.getString(TOPIC_TO_SAVE)
            messageText.text = savedState?.getCharSequence(MESSAGE_TO_SAVE)
            subscribeTopic.setText(savedTopic)
            subscribeTopicForUnsubscribe = savedTopic!!
        }
        savedState = null

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(savedInstanceState != null) {
            newText = savedInstanceState.getString(STATE_TEXT)!!
        }

        val subscribeButton = view.findViewById<Button>(R.id.btn_subscribe_subscribe)
        val unsubscribeButton = view.findViewById<Button>(R.id.btn_subscribe_unsubscribe)

        subscribeButton.isEnabled = isConnected && !isSubscribed
        unsubscribeButton.isEnabled = isSubscribed

        subscribeButton.setOnClickListener {
            isSubscribed = true
            subscribe()
            checkingSubscription(subscribeButton, unsubscribeButton)
        }
        unsubscribeButton.setOnClickListener {
            isSubscribed = false
            unsubscribe()
            checkingSubscription(subscribeButton, unsubscribeButton)
        }

        if (!topicArray.isNullOrEmpty() && !messageArray.isNullOrEmpty()){
            for(i in topicArray.indices) {
                update(messageArray[i], topicArray[i])
            }
        }

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

    override fun onDestroy() {
        super.onDestroy()
        PublishFragment.passIsSubscribedToPublish(isSubscribed)
        PublishFragment.passMQTTAndroidClientToPublish(mqttAndroidClient)
        ConnectFragment.passMqttAndroidClientToConnect(mqttAndroidClient)
        savedState = saveState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(MESSAGE_TO_SAVE, if (savedState != null) savedState else saveState())
    }

    /**
     * This is the method to pass the data we want to be saved.
     */
    private fun saveState(): Bundle? {
        val state = Bundle()
        state.putCharSequence(MESSAGE_TO_SAVE, messageText.text.toString())
        state.putString(TOPIC_TO_SAVE, subscribeTopic.text.toString())
        return state
    }

    /**
     * This is the function makes buttons to be enabled or disabled depending on clicking on them
     * @param buttonSubscribe is to provide subscribe button to the method
     * @param buttonUnsubscribe is to provide unsubscribed button to the method
     */
    private fun checkingSubscription(buttonSubscribe: Button, buttonUnsubscribe: Button) {
        when (isSubscribed) {

            true -> {
                buttonSubscribe.isEnabled = false
                buttonUnsubscribe.isEnabled = true
            }

            false -> {
                buttonSubscribe.isEnabled = true
                buttonUnsubscribe.isEnabled = false
            }
        }
    }

    /**
     * This is the method overrides the interface method update.
     * It receives incoming messages and topics and resets et_subscribe_messages EditView with new messages
     * @param message receives incoming messages
     * @param topic receives incoming topic
     *
     */
    private fun update(message: String, topic: String) {

        val text = et_subscribe_messages.text.toString()

        newText = if (text.isEmpty()) {
            """
Topic: $topic
Message: $message
            """
        } else {
            """$text
                
Topic: $topic 
Message: $message
            """
        }

        et_subscribe_messages.setText(newText)
        et_subscribe_messages.setSelection(et_subscribe_messages.text.length)
    }

    /**
     * This is the method to subscribe the user to the the certain topic, using the tools provided by MQTT
     *
     */
    private fun subscribe(){

        try {
            Log.d(TAG, "Checked option in subscribe method is $checkedOption")

            val subscribeTopic = subscribeTopic.text.toString()
            subscribeTopicForUnsubscribe = subscribeTopic
            if (subscribeTopic.isEmpty()) {
                topicIsEmpty()
                isSubscribed = false
            } else {
                subscribeTopicForUnsubscribe = subscribeTopic
                mqttAndroidClient.subscribe(subscribeTopic,
                    checkedOption, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Toast.makeText(context, "You have subscribed to topic", Toast.LENGTH_SHORT).show()
                            subscribeError.isVisible = false
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                            isConnected = false
                        }

                    })
            }
        } catch (e: Exception){}
    }

    /**
     * This is the method to unsubscribe the user of the the certain topic, using the tools provided by MQTT
     */
    private fun unsubscribe() {
        try {
            val unsubToken = mqttAndroidClient.unsubscribe(subscribeTopicForUnsubscribe)
            if (subscribeTopicForUnsubscribe.isEmpty()) {
                topicIsEmpty()
                isSubscribed = true
            }
            else {
                et_subscribe_messages.text.clear()
                unsubToken.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Toast.makeText(context, SUCCESS_TEXT_UNSUBSCRIBE, Toast.LENGTH_SHORT).show()
                        subscribeError.isVisible = false
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Toast.makeText(context, FAILURE_TEXT_UNSUBSCRIBE, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: MqttException) {}
    }

    private fun topicIsEmpty() {
        subscribeError.isVisible = true
        subscribeError.setTextColor(ContextCompat.getColor(context!!, R.color.cinnabar))
        subscribeError.text = resources.getString(R.string.subscribe_error_text)
    }
}
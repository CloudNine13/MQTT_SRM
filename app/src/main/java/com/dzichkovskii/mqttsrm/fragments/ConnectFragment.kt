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
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dzichkovskii.mqttsrm.R
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class ConnectFragment : Fragment(){
    companion object{

        private var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(null, null, null)

        fun passMqttAndroidClientToConnect(mqttAndroidClient: MqttAndroidClient): ConnectFragment {
            val fragment = ConnectFragment()
            this.mqttAndroidClient = mqttAndroidClient
            return fragment
        }

        const val SUCCESS_TEXT_CONNECT = "Connection established successfully"
        const val FAILURE_TEXT_CONNECT = "Connection wasn't established. Error happened."
        const val SUCCESS_TEXT_DISCONNECT = "You have disconnected"
        const val FAILURE_TEXT_DISCONNECT = "Disconnect went wrong, please try again"
        const val BLANK_TEXT = "Your inputs cannot be empty. Please, write the correct address or ID."
        const val CONNECTION_FAILURE = "Something went wrong. Probably you have no internet. Try later"
        const val TAG = "ConnectFragment"
        const val SET_GET_TEXT = "SetGet"
        private var savedState: Bundle? = null
    }
    private lateinit var statusOfConnection: String
    private var isConnected: Boolean = false
    private var isSessionClean: Boolean = false
    private val mqttConnectOptions = MqttConnectOptions()
    private lateinit var error: TextView
    private lateinit var address: EditText
    private lateinit var port: EditText
    private lateinit var id: EditText
    private lateinit var username: EditText
    private lateinit var statusTextView: TextView
    private lateinit var inputAddress: EditText
    private lateinit var inputPort: EditText
    private lateinit var inputId: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_connect, container, false)
        
        address = root.findViewById(R.id.et_connect_broker_address_input)
        port = root.findViewById(R.id.et_connect_broker_port_input)
        id = root.findViewById(R.id.et_connect_client_id_input)
        username = root.findViewById(R.id.et_connect_client_username_input)
        statusTextView = root.findViewById(R.id.tv_connect_status)
        inputAddress = root.findViewById(R.id.et_connect_broker_address_input)
        inputPort = root.findViewById(R.id.et_connect_broker_port_input)
        inputId = root.findViewById(R.id.et_connect_client_id_input)
        error = root.findViewById(R.id.tv_connect_error_message)
        statusOfConnection = activity?.getString(R.string.connect_status_disconnected)!!

        val cleanSessionSwitch = root.findViewById<Switch>(R.id.sw_connect_clean_session)

        val connectButton = root.findViewById<Button>(R.id.btn_connect_connect)
        val disconnectButton = root.findViewById<Button>(R.id.btn_connect_disconnect)

        if(savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(SET_GET_TEXT)
        }
        if (savedState != null) {
            isConnected = savedState?.getBoolean(SET_GET_TEXT)!!
            isSessionClean = savedState?.getBoolean("isSessionClean")!!
            address.setText(savedState!!.getString("address"))
            port.setText(savedState!!.getString("port"))
            id.setText(savedState!!.getString("ID"))
            username.setText(savedState!!.getString("username"))
            statusTextView.text = savedState!!.getString("statusString")

        }
        savedState = null
        cleanSessionSwitch.isChecked = isSessionClean

        connectButton.isEnabled = !isConnected
        disconnectButton.isEnabled = isConnected



        connectButton.setOnClickListener {
            connect(context, view)
            checkingConnection(connectButton, disconnectButton)
        }

        disconnectButton.setOnClickListener{
            disconnect()
            isConnected = false
            checkingConnection(connectButton, disconnectButton)
        }

        mqttConnectOptions.isCleanSession = false

        cleanSessionSwitch.setOnCheckedChangeListener { _, isChecked ->
            mqttConnectOptions.isCleanSession = isChecked
            isSessionClean = isChecked
        }

        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        SubscribeFragment.passIsConnectedToSubscribe(isConnected)
        savedState = saveState()
    }

    /**
     * This is the method to save instance activity
     */
    private fun saveState(): Bundle? {
        val state = Bundle()
        state.putBoolean(SET_GET_TEXT, isConnected)
        state.putBoolean("isSessionClean", isSessionClean)
        state.putString("address", address.text.toString())
        state.putString("port", port.text.toString())
        state.putString("ID", id.text.toString())
        state.putString("username", username.text.toString())
        state.putString("statusString", statusOfConnection)
        return state
    }

    /**
     * This is the method to connect the user to the broker, using the tools provided by MQTT
     *
     * @param context is used to pass context to Mqtt
     * @param view is used to be able to work with views (EditView, TextView y etc.)
     *
     */
    private fun connect(context: Context?,
                        view: View?) {

        //Making the string the user needs to put more friendly
        val addressStringSimplification = "tcp://" + inputAddress.text.toString() +
               ":" + inputPort.text.toString()

        val addressStatus = inputAddress.text.toString() + ":" + inputPort.text.toString()

        val usernameEditText = view?.findViewById<EditText>(R.id.et_connect_client_username_input)
        val passwordEditText = view?.findViewById<EditText>(R.id.et_connect_client_password_input)

        mqttConnectOptions.isAutomaticReconnect = true
        
        if (usernameEditText?.text.toString().isNotEmpty()){
            mqttConnectOptions.userName = usernameEditText!!.text.toString()
        }

        if (passwordEditText?.text.toString().isNotEmpty()){
            val password = passwordEditText!!.text.toString()
            val charArray = password.toCharArray()
            mqttConnectOptions.password = charArray
        }
            mqttAndroidClient = MqttAndroidClient(context?.applicationContext, addressStringSimplification, inputId.text.toString())

        if(addressStringSimplification.isNotEmpty() && addressStringSimplification != "tcp://:" && inputId.text.toString().isNotEmpty()) {
            SubscribeFragment.passMqttAndroidClientToSubscribe(mqttAndroidClient)
            PublishFragment.passMQTTAndroidClientToPublish(mqttAndroidClient)

            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.addToBackStack(null)
            transaction?.commit()
        }

        if (inputAddress.isEmpty() || inputId.isEmpty()
            || inputPort.isEmpty() || addressStringSimplification == "tcp://:"){
            displayErrorMessage(BLANK_TEXT, this)
            isConnected = false
        }
        else {

            try {
                val token = mqttAndroidClient.connect(mqttConnectOptions)
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {

                        val disconnectedBufferOptions = DisconnectedBufferOptions()
                        disconnectedBufferOptions.isBufferEnabled = true
                        disconnectedBufferOptions.bufferSize = 100
                        disconnectedBufferOptions.isPersistBuffer = false
                        disconnectedBufferOptions.isDeleteOldestMessages = false
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)

                        statusOfConnection = resources.getString(R.string.connect_status_connected, addressStatus)
                        view?.findViewById<TextView>(R.id.tv_connect_status)?.text = statusOfConnection

                        Log.d(TAG, "Connection is successful")

                        context?.toast(SUCCESS_TEXT_CONNECT)
                        isConnected = true
                        hideKeyboard()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {

                        Log.d(TAG, "Connection didn't established")
                        isConnected = false
                        context?.toast(FAILURE_TEXT_CONNECT)
                        displayErrorMessage(FAILURE_TEXT_CONNECT, this@ConnectFragment)
                    }
                }
            } catch (e: MqttException) {

                Log.d(TAG, "Exception caught")

                displayErrorMessage(CONNECTION_FAILURE, this)
            }
        }

        error.visibility = View.INVISIBLE
    }

    /**
     * This is the function to disconnect user from MQTT, using tools provided by MQTT
     */
    private fun disconnect() {
        try {

            val disconnectToken = mqttAndroidClient.disconnect()
            disconnectToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    context?.toast(SUCCESS_TEXT_DISCONNECT)
                    statusOfConnection = resources.getString(R.string.connect_status_disconnected)
                    view?.findViewById<TextView>(R.id.tv_connect_status)?.text = statusOfConnection
                }
                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    context?.toast(FAILURE_TEXT_DISCONNECT)
                    isConnected = true
                }
            }
        } catch (e: MqttException) {}
    }

    /**
     * This extension function makes strings look less ugly.
     */
    private fun EditText.isEmpty() = this.text.toString().isEmpty()

    /**
     * This extension function makes Toast looks less ugly
     */
    private fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT)
            = Toast.makeText(this, text, duration).show()

    /**
     * This is the function makes buttons to be enabled or disabled depending on clicking on them
     * @param buttonConnect is to provide connect button to the method
     * @param buttonDisconnect is to provide disconnect button to the method
     */
    private fun checkingConnection(buttonConnect: Button, buttonDisconnect: Button) {
        when (isConnected) {
            true -> {
                buttonConnect.isEnabled = false
                buttonDisconnect.isEnabled = true
            }
            false -> {
                buttonConnect.isEnabled = true
                buttonDisconnect.isEnabled = false
            }
        }
    }

    /**
     * This is the method to show an errors if user didn't use port, id or broker's address.
     *
     * @param fragment is used to pass the param to the method hideKeyboard()
     *
     */
    private fun displayErrorMessage(errorMessage: String, fragment: Fragment){
        error.text = errorMessage
        error.setTextColor(ContextCompat.getColor(context!!, R.color.cinnabar))
        error.visibility = View.VISIBLE
        fragment.hideKeyboard()
    }

    /**
     * Two methods to hide the keyboard after the result is shown
     */
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
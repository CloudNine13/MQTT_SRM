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
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dzichkovskii.mqttsrm.R
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class ConnectFragment : Fragment(){
    companion object{
        const val SUCCESS_TEXT = "Connection established successfully"
        const val FAILURE_TEXT = "Connection wasn't established. Error happened."
        const val BLANK_TEXT = "Your inputs cannot be empty. Please, write the correct address or ID."
        const val CONNECTION_FAILURE = "Something went wrong. Probably you have no internet. Try later"
        const val TAG = "ConnectFragment"
    }
    private lateinit var mqttAndroidClient: MqttAndroidClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_connect, container, false)

//        [TEST VALUES]
//        val testClientId = MqttClient.generateClientId()
//        val testAddress = "tcp://broker.hivemq.com:1883"

        root.findViewById<Button>(R.id.btn_connect).setOnClickListener {
            connect(context, view)

        }

        return root
    }

    /**
     * This is the method to connect the user to the broker
     *
     * @param context is used to pass context to Mqtt
     * @param view is used to be able to work with views (EditView, TextView y etc.)
     *
     */
    private fun connect(context: Context?,
                        view: View?) {
        //val inputAddress = view?.findViewById(R.id.tv_broker_address_input) as EditText
        //val inputId = view.findViewById(R.id.tv_client_id_input) as EditText
        //val inputPort = view.findViewById(R.id.tv_broker_port_input) as EditText

        //Making the string the user needs to put more friendly
        //val addressStringSimplification = "tcp://" + inputAddress.text.toString() +
        //       ":" + inputPort.text.toString()

//        [TEST VALUES]
        val addressStringSimplification = "tcp://broker.hivemq.com:1883"
        val testClientId = MqttClient.generateClientId()

        mqttAndroidClient = MqttAndroidClient(context?.applicationContext, addressStringSimplification, testClientId/*inputId.text.toString()*/)

//        if(!addressStringSimplification.isBlank() && addressStringSimplification != "tcp://:" && !inputId.text.toString().isBlank()) {
            SubscribeFragment.passMQTTAndroidClientToSubscribe(mqttAndroidClient)

            PublishFragment.passMQTTAndroidClientToPublish(mqttAndroidClient)

            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            transaction?.replace(R.id.fragment_container, someFragment)
            transaction?.addToBackStack(null)
            transaction?.commit()
//        }

//        if (inputAddress.isBlank() && inputId.isBlank()
//            && inputPort.isBlank() && addressStringSimplification == "tcp://:"){
//           displayErrorMessage(BLANK_TEXT, view, this)
//        }
//        else {

            try {
                val token = mqttAndroidClient.connect()
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {

                        Log.d(TAG, "Connection is successful")

                        Toast.makeText(context, SUCCESS_TEXT, Toast.LENGTH_SHORT).show()
                        hideKeyboard()
                        return
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {

                        Log.d(TAG, "Connection didn't established")

                        Toast.makeText(context, FAILURE_TEXT, Toast.LENGTH_SHORT).show()
                        displayErrorMessage(FAILURE_TEXT, view, this@ConnectFragment)
                        return
                    }
                }
            } catch (e: MqttException) {

                Log.d(TAG, "Exception caught")

                displayErrorMessage(CONNECTION_FAILURE, view, this)
            }
//    }
//        tv_error.visibility = View.INVISIBLE
    }

    /**
     * This extension function makes strings look less ugly.
     */
    private fun EditText.isBlank() = this.text.toString().isBlank()
//}

/**
 * This is the method to show an errors if user didn't use port, id or broker's address.
 *
 * @param errorString is used to pass the error string to the method. It's not necessary to put it
 * outside the method but I decided to follow the OOP rules. Also en catch we've got another message
 * to be passed to this method, so it would be more maintainable to drag the strings outside
 * @param view is used to find text view "tv_error" inside the method. Outside it's not working.
 * @param fragment is used to pass the param to the method hideKeyboard()
 *
 */
fun displayErrorMessage(errorString: String, view: View?, fragment: Fragment){
    val errorTextView = view?.rootView?.findViewById(R.id.tv_error) as TextView
    errorTextView.text = errorString
    errorTextView.setTextColor(ContextCompat.getColor(context!!, R.color.cinnabar))
    errorTextView.visibility = View.VISIBLE
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
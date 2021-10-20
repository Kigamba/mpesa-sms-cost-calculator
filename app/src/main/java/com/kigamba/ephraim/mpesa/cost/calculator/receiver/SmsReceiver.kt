package com.kigamba.ephraim.mpesa.cost.calculator.receiver

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.widget.Toast

import android.os.Build
import androidx.annotation.RequiresApi
import timber.log.Timber


/**
 * Created by Kigamba (nek.eam@gmail.com) on 20-October-2021
 */
class SmsReceiver : BroadcastReceiver() {

  val TAG = SmsReceiver::class.java.simpleName
  val pdu_type = "pdus"


  override fun onReceive(context: Context?, intent: Intent?) {
    val bundle = intent!!.extras
    var msgs: Array<SmsMessage?>
    var strMessage = ""
    val format = bundle!!.getString("format")
    val pdus = bundle[pdu_type] as Array<Any>?

    if (pdus != null) {
      // Fill the msgs array.
      msgs = arrayOfNulls(pdus.size)
      for (i in 0 until msgs.size) {
        // Check Android version and use appropriate createFromPdu.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          // If Android version M or newer:
          msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray, format)
        } else {
          // If Android version L or older:
          msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
        }
        // Build the message to show.
        strMessage += "SMS from " + msgs[i]?.getOriginatingAddress()
        strMessage += " :${msgs[i]?.getMessageBody()}"
        // Log and display the SMS message.
        Timber.d(TAG, "onReceive: $strMessage")
        Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show()
      }
    }
  }


}
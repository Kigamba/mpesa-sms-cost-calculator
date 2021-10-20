package com.kigamba.ephraim.mpesa.cost.calculator

import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kigamba.ephraim.mpesa.cost.calculator.adapter.CustomAdapter
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.HashMap
import timber.log.Timber

class MainActivity : AppCompatActivity() {

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Timber.plant(Timber.DebugTree())
    setContentView(R.layout.activity_main)


    checkPermissions()
  }

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onResume() {
    super.onResume()

    val durations = HashMap<String, Pair<Date, Date>>()
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val startOfDay = calendar.time
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val endOfDay = calendar.time
    calendar.add(Calendar.DAY_OF_MONTH, -1)

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = calendar.time
    calendar.add(Calendar.MONTH, 1)
    val endOfMonth = calendar.time
    calendar.add(Calendar.MONTH, -1)


    //calendar.set(Calendar.MONTH, 0)
    calendar.set(Calendar.DAY_OF_YEAR, 1)
    val startOfYear = calendar.time
    calendar.add(Calendar.YEAR, 1)
    val endOfYear = calendar.time
    calendar.add(Calendar.YEAR, -1)

    durations["This year"] = Pair(startOfYear, endOfYear)
    durations["This month"] = Pair(startOfMonth, endOfMonth)
    durations["Today"] = Pair(startOfDay, endOfDay)

    val costs = getCosts()

    val totalCostsDuration = calculateSummaries(durations, costs)

    displayDurations(totalCostsDuration)
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun checkPermissions() {
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    val requestPermissionLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()
      ) { isGranted: Boolean ->
        if (isGranted) {
          // Permission is granted. Continue the action or workflow in your
          // app.
        } else {
          // Explain to the user that the feature is unavailable because the
          // features requires a permission that the user has denied. At the
          // same time, respect the user's decision. Don't link to system
          // settings in an effort to convince the user to change their
          // decision.
        }
      }

    when {
      ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.READ_SMS
      ) == PackageManager.PERMISSION_GRANTED -> {
        // You can use the API that requires the permission.
      }
      //shouldShowRequestPermissionRationale(android.Manifest.permission.READ_SMS) -> {
      // In an educational UI, explain to the user why your app requires this
      // permission for a specific feature to behave as expected. In this UI,
      // include a "cancel" or "no thanks" button that allows the user to
      // continue using your app without granting the permission.
      //showInContextUI()
    //}
      else -> {
        // You can directly ask for the permission.
        // The registered ActivityResultCallback gets the result of this request.
        requestPermissionLauncher.launch(
          android.Manifest.permission.READ_SMS)
      }
    }
  }

  fun calculateSummaries(duration: HashMap<String, Pair<Date, Date>>, costs: HashMap<String, Float>) : HashMap<String, Float> {
    val summaries = HashMap<String, Float>()

    duration.forEach {
      summaries[it.key] = 0F
    }

    costs.forEach {
      val costDate = it.key
      val cost = it.value

      duration.forEach {
        if (costDate.isWithin(it.value.first, it.value.second)) {
          summaries[it.key] = summaries[it.key]?.plus(cost) ?: 0F
        }
      }
    }

    return summaries
  }

  fun String.isWithin(date1: Date, date2: Date) : Boolean {
    val time = this.toLong()
    return time >= date1.time && time < date2.time
  }

  fun displayDurations(totalCostDurations: HashMap<String, Float>) {
    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
    recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    recyclerView.adapter = CustomAdapter(totalCostDurations)
  }

  // Read mpesa messages and calculate the costs
  // Group them by dates and enable performing reports by month, day, week
  fun getCosts() : HashMap<String, Float> {

    // public static final String INBOX = "content://sms/inbox";
    // public static final String SENT = "content://sms/sent";
    // public static final String DRAFT = "content://sms/draft";
        // public static final String INBOX = "content://sms/inbox";
    // public static final String SENT = "content://sms/sent";
    // public static final String DRAFT = "content://sms/draft";
    val mpesaTransactionCostMap = HashMap<String, Float>()
    val cursor: Cursor? =
      contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)

    if (cursor != null && cursor.moveToFirst()) { // must check the result to prevent exception
      do {
        // If safaricom mpesa message get the message text and transaction cost
        //if (safaricom )
        if (cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS))?.contains("MPESA", true) == true) {
          var msgData = ""
          for (idx in 0 until cursor.getColumnCount()) {
            msgData += " " + cursor.getColumnName(idx).toString() + ":" + cursor.getString(idx)
          }
          // use msgData
          Timber.i(msgData)

          val msg = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
          if (msg != null) {
            val transactionCostPrefix = "Transaction cost, Ksh"
            val transactionCostIndex = msg.indexOf(transactionCostPrefix)

            if (transactionCostIndex < 0) {
              continue
            }

            val transactionCostEndIndex = msg.indexOf(".", transactionCostIndex + transactionCostPrefix.length).let { msg.indexOf(".", it + 1) }

            val transactionCostString = msg.subSequence(transactionCostIndex + transactionCostPrefix.length, transactionCostEndIndex)
              .toString()

            Timber.i("Transaction Cost String $transactionCostString")

            val date = cursor.getString(cursor.getColumnIndex(Telephony.Sms.DATE))
            Timber.i("Date : $date")
            mpesaTransactionCostMap.put(date, transactionCostString.toFloat())

          }
        }

      } while (cursor.moveToNext())
    } else {
      // empty box, no SMS
    }

    return mpesaTransactionCostMap
  }
}
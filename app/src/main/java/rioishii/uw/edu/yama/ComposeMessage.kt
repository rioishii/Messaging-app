package rioishii.uw.edu.yama

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.PendingIntent
import android.content.Context
import android.telephony.SmsManager
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import android.content.IntentFilter
import android.content.BroadcastReceiver
import kotlinx.android.synthetic.main.activity_compose_message.*

class ComposeMessage : AppCompatActivity() {
    private var number: String? = null
    private val SENT: String = "SMS_Sent"
    private lateinit var smsReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose_message)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sendSMSPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        val receiveSMSPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
        if (sendSMSPermission != PackageManager.PERMISSION_GRANTED && receiveSMSPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS),
                1
            )
        } else {
            selectContact.setOnClickListener {
                selectContact()
            }
            send.setOnClickListener { v ->
                if (number != null) {
                    val message = msgText.text.toString()
                    val sms = SmsManager.getDefault()

                    val pendingIntent = PendingIntent.getBroadcast(this, 0, Intent(SENT), 0)
                    sms.sendTextMessage(number, null, message, pendingIntent, null)
                    
                    number = null
                    phoneNum.text = "To:"
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    msgText.setText("")
                }
            }

            if (intent.getStringExtra(SMSManager.REPLY) != null) {
                number = intent.getStringExtra(SMSManager.REPLY)
                phoneNum.text = "To: $number"
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(intent.getIntExtra(SMSManager.NOTIFICATION, 0))
        }
    }

    private fun selectContact() {
        val intent = Intent(Intent.ACTION_PICK).apply{
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == Activity.RESULT_OK) {
            val contactUri = data!!.data
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            contentResolver.query(contactUri!!, projection, null, null, null).use { cursor ->
                if (cursor!!.moveToFirst()) {
                    number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phoneNum.text = "To: $number"
                }
            }
        }
    }

    companion object {
        const val REQUEST_SELECT_PHONE_NUMBER = 1
    }

    override fun onResume() {
        smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (resultCode) {
                    Activity.RESULT_OK ->
                        Toast.makeText(context, "SMS sent!", Toast.LENGTH_SHORT).show()

                    SmsManager.RESULT_ERROR_GENERIC_FAILURE ->
                        Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()

                    SmsManager.RESULT_ERROR_NO_SERVICE ->
                        Toast.makeText(context, "No Service!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        registerReceiver(smsReceiver, IntentFilter(SENT))
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(smsReceiver)
        super.onPause()
    }
}

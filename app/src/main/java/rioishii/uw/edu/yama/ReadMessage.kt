package rioishii.uw.edu.yama

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.message_list.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ReadMessage : AppCompatActivity() {
    private var messages = ArrayList<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.app_name)

        recyclerView.adapter = MessageAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        fab.setOnClickListener {
            val intent = Intent(this, ComposeMessage::class.java)
            startActivity(intent)
        }
        
        val readSMSPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        if (readSMSPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS), 1)
        } else {
            getSMSMessages()
        }
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(intent.getIntExtra(SMSManager.NOTIFICATION, 0))
    }
    
    private fun getSMSMessages() {
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val cursor = contentResolver.query(uri,
            arrayOf(Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE),
            null, null, null)
        messages.clear()
        while (cursor!!.moveToNext()) {
            val address = cursor.getString(cursor.getColumnIndex("address"))
            val body = cursor.getString(cursor.getColumnIndex("body"))
            val date = cursor.getLong(cursor.getColumnIndex("date"))

            val newDate = Date(date)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm aaa", Locale.US)
            val dateString = dateFormat.format(newDate)

            val message = Message(address, body, dateString)
            messages.add(message)
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }
    
    class MessageAdapter(private val data: ArrayList<Message>) :
        RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val authorView: TextView = view.author
            val bodyView: TextView = view.body
            val dateView: TextView = view.date
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.message_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            holder.authorView.text = item.author
            holder.bodyView.text = item.body
            holder.dateView.text = item.dateTime

        }

        override fun getItemCount() = data.size
    }
    
    data class Message(
        val author: String,
        val body: String,
        val dateTime: String
    )
}
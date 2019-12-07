package rioishii.uw.edu.yama

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.provider.Telephony.Sms.Intents.getMessagesFromIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder

class SMSManager : BroadcastReceiver() {
    companion object {
        const val REPLY = "reply"
        const val NOTIFICATION = "notification"
        var notificationId = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            val messages = getMessagesFromIntent(intent)
            for (i in messages.indices) {
                val phoneNum = messages[i].displayOriginatingAddress
                val message = messages[i].displayMessageBody
                showNotification(context, phoneNum, message)
            }
        }
    }

    private fun showNotification(context: Context, author: String, msg: String) {
        val builder = NotificationCompat.Builder(context, "yama")
            .setSmallIcon(R.drawable.ic_sms)
            .setContentTitle(author)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setAutoCancel(true)
        
        val intent = Intent(context, ReadMessage::class.java)
        intent.putExtra(NOTIFICATION, notificationId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        builder.setContentIntent(pendingIntent)
        builder.addAction(0, "View", pendingIntent)

        val replyIntent = Intent(context, ComposeMessage::class.java)
        replyIntent.putExtra(REPLY, author)
        replyIntent.putExtra(NOTIFICATION, notificationId)
        replyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val replyPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(replyIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        builder.addAction(0, "Reply", replyPendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("yama","yama_channel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId++, builder.build())
    }
}
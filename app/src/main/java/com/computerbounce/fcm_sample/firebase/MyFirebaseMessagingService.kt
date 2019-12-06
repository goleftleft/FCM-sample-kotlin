package com.computerbounce.fcm_sample.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.computerbounce.fcm_sample.R
import com.computerbounce.fcm_sample.view.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FIREBASEMESSAGESERVICE"


    /**
     *  message type : notification, data
     *  i think the difference between
     *  ----------------------------------------------------------------------
     *  |                          |    notification    |        data        |
     *  ----------------------------------------------------------------------
     *  | foreground push vibrator |         O          |          O         |
     *  |                          |                    |  (builder option)  |
     *  ----------------------------------------------------------------------
     *  | background push vibrator |         X          |          O         |
     *  |                          |                    |  (builder option)  |
     *  ----------------------------------------------------------------------
     *  | badge count              |         O          |          X         |
     *  |                          |                    |  (use function)    |
     *  ----------------------------------------------------------------------
     *  | message stack            |         O          |          X         |
     *  |---------------------------------------------------------------------
     *
     */


    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // message info : https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        sendNotification(remoteMessage)

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


    // get Token for personal push
    override fun onNewToken(token: String) {
        Log.d(TAG, "Firebase token: $token")
        sendRegistrationToServer(token)
    }

    //send token to app server
    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
        //val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        //WorkManager.getInstance().beginWith(work).enqueue()
        // [END dispatch_job]
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }


    private fun sendNotification(remoteMessage: RemoteMessage) {

        //remoteMessage - notification, data
        val notification = remoteMessage.notification
        val data = remoteMessage.data
        val title = notification?.title ?: data["title"]
        val messaage = notification?.body ?: data["message"]

        //set intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("Noti", messaage)
        }

        //set option
        val CHANNEL_ID = "NotiID"
        val CHANNEL_NAME = "NotiName"
        val description = "Noti description"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // >=oreo need channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.enableLights(true)
            channel.enableVibration(true)
            //channel.vibrationPattern = longArrayOf(100,200,100,200)
            channel.description = description
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            channel.importance = NotificationManager.IMPORTANCE_HIGH
            channel.lightColor = Color.BLUE
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0 , intent,
            PendingIntent.FLAG_ONE_SHOT)


        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.common_full_open_on_phone)
            .setContentTitle("")
            .setContentText("")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setVibrate(longArrayOf(100,200,100,200))
            //.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_LIGHTS)
            .setDefaults( Notification.DEFAULT_VIBRATE)
            //.setLights(Color.BLUE, 1, 1)
            .setContentIntent(pendingIntent)

        notificationManager.notify(0, notificationBuilder.build())
    }


    //set app badge count
    private fun UpdateBadge(count: Int) {
        val intent = Intent("android.intent.action.BADGE_COUNT_UPDATE")
        intent.putExtra("badge_count_package_name", packageName)
        intent.putExtra("badge_count_class_name",javaClass)
        intent.putExtra("badge_count", count)
        sendBroadcast(intent)
    }


}
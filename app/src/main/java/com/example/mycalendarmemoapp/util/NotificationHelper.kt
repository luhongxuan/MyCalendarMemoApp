package com.example.mycalendarmemoapp.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mycalendarmemoapp.R // 你的圖示資源
import com.example.mycalendarmemoapp.MainActivity // 點擊通知時要開啟的 Activity
import com.example.mycalendarmemoapp.receiver.AlarmReceiver // 下面會提到的 BroadcastReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_ID = "memo_reminder_channel"
    private const val CHANNEL_NAME = "備忘錄提醒"
    private const val CHANNEL_DESCRIPTION = "用於備忘錄 App 的定時提醒"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 可以設為 HIGH 讓提醒更明顯
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(context: Context, memoId: Int, title: String, dateTime: LocalDateTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MEMO_ID, memoId)
            putExtra(AlarmReceiver.EXTRA_MEMO_TITLE, title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            memoId, // 使用 memoId 作為 requestCode，方便取消
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // 檢查是否可以排程精確鬧鐘
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // 引導使用者去設定開啟精確鬧鐘的權限
                // 或者使用 setWindow/set inexact alarms
                // 這裡為了簡化，我們假設權限已允許或使用非精確鬧鐘
                alarmManager.setAndAllowWhileIdle( // 或者 setWindow
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelNotification(context: Context, memoId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            memoId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE 表示如果不存在則不建立
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel() // 取消 PendingIntent 本身
        }
        // 同時也取消已顯示的通知 (如果有的話)
        NotificationManagerCompat.from(context).cancel(memoId)
    }


    fun showNotification(context: Context, memoId: Int, title: String) {
        // 權限檢查應該在呼叫此方法之前或 ViewModel 中完成
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 在實際應用中，這裡應該要有權限請求的邏輯，或確保權限已授予
            // 為簡化起見，此處假設權限已授予
            println("POST_NOTIFICATIONS permission not granted.")
            return
        }

        // 點擊通知後開啟 MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // 你可以傳遞額外資訊，例如要跳轉到哪個備忘錄
            // putExtra("memo_id_to_open", memoId)
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            memoId + 1000, // 不同的 requestCode
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            //.setSmallIcon(R.drawable.ic_notification_icon) // **請替換成你自己的通知圖示**
            .setContentTitle("備忘錄提醒")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 在鎖定畫面上顯示
            .setAutoCancel(true) // 點擊後自動移除通知
            .setContentIntent(pendingContentIntent) // 設定點擊行為

        NotificationManagerCompat.from(context).notify(memoId, builder.build())
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12 及以下版本不需要執行時權限
        }
    }
}
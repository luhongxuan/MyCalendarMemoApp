// 在 app/src/main/java/com/example/mycalendarmemoapp/receiver/AlarmReceiver.kt
package com.example.mycalendarmemoapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mycalendarmemoapp.util.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_MEMO_ID = "extra_memo_id"
        const val EXTRA_MEMO_TITLE = "extra_memo_title"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val memoId = intent.getIntExtra(EXTRA_MEMO_ID, -1)
        val memoTitle = intent.getStringExtra(EXTRA_MEMO_TITLE)

        if (memoId != -1 && memoTitle != null) {
            NotificationHelper.showNotification(context, memoId, memoTitle)
        }
    }
}
package com.example.mycalendarmemoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mycalendarmemoapp.ui.navigation.NavGraph
import com.example.mycalendarmemoapp.ui.theme.MyCalendarMemoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCalendarMemoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph() // 載入我們的導航圖
                }
            }
        }
        // 你可以在這裡或 Application 類別中建立通知頻道
        // NotificationHelper.createNotificationChannel(this)
    }
}
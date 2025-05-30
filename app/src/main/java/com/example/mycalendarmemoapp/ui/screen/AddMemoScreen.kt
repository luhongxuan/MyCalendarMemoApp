package com.example.mycalendarmemoapp.ui.screen

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mycalendarmemoapp.ui.MemoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import android.util.Log
import android.widget.Toast
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemoScreen(navController: NavController, viewModel: MemoViewModel) {
    val context = LocalContext.current
    var time by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }

    val currentDate = LocalDate.now()
    val formattedDate = currentDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

    // 時間選擇器
    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        // 如果 time 字串已經有值，嘗試解析它來設定初始時間
        val initialHour: Int
        val initialMinute: Int
        if (time.matches(Regex("\\d{2}:\\d{2}"))) {
            val parts = time.split(":")
            initialHour = parts[0].toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
            initialMinute = parts[1].toIntOrNull() ?: calendar.get(Calendar.MINUTE)
        } else {
            initialHour = calendar.get(Calendar.HOUR_OF_DAY)
            initialMinute = calendar.get(Calendar.MINUTE)
        }

        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute -> // OnTimeSetListener: 當用戶點擊 "OK"
                time = String.format("%02d:%02d", hourOfDay, minute)
                timeError = false
                // showTimePicker = false // 這裡可以不用重複設定，因為 onDismissListener 會處理
                Log.d("AddMemoScreen", "Time set: $time")
            },
            initialHour,
            initialMinute,
            true // 24 小時制
        )

        timePickerDialog.setOnDismissListener {
            showTimePicker = false // 無論是確定、取消或點擊外部，都將狀態設回 false
            Log.d("AddMemoScreen", "TimePickerDialog dismissed. showTimePicker = $showTimePicker")
        }

        LaunchedEffect(Unit) { // 確保 dialog.show() 只被調用一次當 showTimePicker 變為 true
            timePickerDialog.show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("新增備忘錄") }) // 新增頁面標題 [cite: 3]
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "日期: $formattedDate") // 日期是今天的時間(不可改) [cite: 3]

            OutlinedTextField(
                value = time,
                onValueChange = { /* 不直接修改，透過選擇器修改 */ },
                label = { Text("時間 (HH:mm)") },
                readOnly = true, // 透過點擊按鈕開啟選擇器
                isError = timeError,
                trailingIcon = {
                    Button(onClick = { showTimePicker = true }) {
                        Text("選擇時間")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (timeError) {
                Text("請注意時間的格式以及時間是否早於現在", color = MaterialTheme.colorScheme.error)
            }


            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = it.isBlank()
                },
                label = { Text("標題 / 說明*") },
                isError = titleError,
                modifier = Modifier.fillMaxWidth()
            )
            if (titleError) {
                Text("標題不可為空", color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("地點 (可選)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    titleError = title.isBlank()
                    timeError = (!isValidTimeFormat(time) || !isCorrectTime(time))// 檢查時間格式 [cite: 3]

                    if (!titleError && !timeError) { // 儲存時要檢查標題是否有內容 & 時間正確 [cite: 3]
                        viewModel.addMemo(time, title, location)
                        navController.popBackStack() // 回到首頁
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("儲存")
            }
        }
    }
}

// 檢查時間格式是否為 HH:mm
fun isValidTimeFormat(time: String): Boolean {
    return try {
        val parts = time.split(":")
        if (parts.size != 2) return false
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        hour in 0..23 && minute in 0..59
    } catch (e: Exception) {
        false
    }
}

fun isCorrectTime(time: String): Boolean {
    return try {
        val currentLocalTime = LocalTime.now()
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        Log.d("AddMemoScreen", String.format("%02d %02d %02d %02d", currentLocalTime.hour, currentLocalTime.minute, hour, minute))
        hour > currentLocalTime.hour || (hour == currentLocalTime.hour && minute > currentLocalTime.minute)
    } catch(e: Exception){
        false
    }
}
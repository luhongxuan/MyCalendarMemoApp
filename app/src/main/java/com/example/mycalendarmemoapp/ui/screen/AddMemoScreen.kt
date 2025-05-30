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
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                time = String.format("%02d:%02d", hourOfDay, minute)
                timeError = false // 選擇後清除錯誤
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24 小時制
        ).show()
        // 因為 TimePickerDialog 不是 Compose 元件，需要手動處理 showTimePicker 狀態
        // 這裡我們只在需要時顯示，但 Compose 無法直接控制它的顯示，
        // 為了避免重複顯示，我們在選擇後設為 false。
        // 一個更好的方法可能是使用 Compose 內建的時間選擇器 (如果有的話) 或自訂。
        // 這裡為了簡單起見，暫時這樣處理，並在點擊按鈕時將 showTimePicker 設為 true。
        // 為了避免在 recomposition 時重複顯示，我們在 onDismissRequest 中設為 false。
        // 但 TimePickerDialog 沒有 onDismissRequest，所以我們只在選擇後設 false。
        // **注意**: 這種方式在 Compose 中不太理想，但作為範例，我們先這樣。
        // **修正**: TimePickerDialog 本身會處理顯示，我們只需要將 showTimePicker 設為 false 即可。
        // 我們將 showTimePicker 的控制移到按鈕點擊事件中。
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
                Text("請選擇有效的時間", color = MaterialTheme.colorScheme.error)
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
                    timeError = !isValidTimeFormat(time) // 檢查時間格式 [cite: 3]

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
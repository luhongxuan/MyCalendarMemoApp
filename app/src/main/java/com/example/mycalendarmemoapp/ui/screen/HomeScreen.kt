package com.example.mycalendarmemoapp.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mycalendarmemoapp.data.Memo
import com.example.mycalendarmemoapp.ui.MemoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

const val FAB_TEST_TAG = "AddMemoFab" // 為了測試而設定的 Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MemoViewModel) {
    val context = LocalContext.current
    val currentDate by viewModel.currentDate.collectAsState()
    val memos by viewModel.memosForCurrentDate.observeAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf<Memo?>(null) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        var hasNotificationPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasNotificationPermission = isGranted
                if (!isGranted) {
                    // 使用者拒絕了權限，你可以顯示一個提示訊息說明為什麼需要此權限
                    // 例如使用一個 Snackbar 或 Dialog
                    println("通知權限被拒絕")
                }
            }
        )

        LaunchedEffect(key1 = true) { // 只在 Composable 首次進入時執行一次
            if (!hasNotificationPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${
                            currentDate.format(
                                DateTimeFormatter.ofLocalizedDate(
                                    FormatStyle.LONG
                                )
                            )
                        } 備忘錄"
                    ) // 顯示「當天日期」備忘錄 [cite: 2]
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_memo") },
                modifier = Modifier.testTag(FAB_TEST_TAG) // 設定 Test Tag [cite: 3]
            ) {
                Icon(Icons.Filled.Add, contentDescription = "新增備忘錄")
            } // 新增備忘錄項目的浮動按鈕 [cite: 2]
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (memos.isEmpty()) {
                Text(
                    text = "今天沒有備忘錄。",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) { // 當日備忘錄清單 [cite: 2]
                    items(memos) { memo ->
                        MemoItem(
                            memo = memo,
                            onDeleteClick = { showDeleteDialog = it },
                            onLocationClick = { location ->
                                openMap(context, location) // 點擊地點開啟地圖 [cite: 2]
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    // 刪除確認對話框 [cite: 2]
    showDeleteDialog?.let { memoToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("確認刪除") },
            text = { Text("您確定要刪除 '${memoToDelete.title}' 這筆備忘錄嗎？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMemo(memoToDelete)
                        showDeleteDialog = null
                    }
                ) {
                    Text("刪除")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun MemoItem(memo: Memo, onDeleteClick: (Memo) -> Unit, onLocationClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${memo.time} ${memo.title}", // 顯示時間與要進行工作 [cite: 2]
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            memo.location?.let { location ->
                Text(
                    text = location,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onLocationClick(location) } // 如果有地點的話，點擊可以顯示該地點地圖 [cite: 2]
                )
            }
        }
        IconButton(onClick = { onDeleteClick(memo) }) {
            Icon(Icons.Filled.Delete, contentDescription = "刪除備忘錄") // 有刪除按鈕 [cite: 2]
        }
    }
}

// 開啟地圖的 Intent [cite: 6]
fun openMap(context: Context, location: String) {
    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps") // 嘗試優先開啟 Google Maps
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        // 如果沒有 Google Maps，嘗試用任何能處理 geo 的 App 開啟
        val genericMapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        if (genericMapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(genericMapIntent)
        } else {
            // 可以在這裡顯示錯誤訊息
            println("找不到可以開啟地圖的應用程式")
        }
    }
}
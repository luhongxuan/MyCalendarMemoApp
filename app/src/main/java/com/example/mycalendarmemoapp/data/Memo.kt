package com.example.mycalendarmemoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memos")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long, // 儲存日期 (時間戳)
    val time: String, // 儲存時間 (HH:mm)
    val title: String, // 備忘錄標題/說明 [cite: 2]
    val location: String? // 地點 (可選) [cite: 2]
)
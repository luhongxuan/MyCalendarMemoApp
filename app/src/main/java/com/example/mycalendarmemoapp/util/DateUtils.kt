package com.example.mycalendarmemoapp.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DateUtils {

    // 將 LocalDate 轉換為儲存到資料庫的 Long (Epoch Milliseconds)
    fun localDateToEpochMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    // 將資料庫中的 Long (Epoch Milliseconds) 轉換回 LocalDate
    fun epochMillisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    // 格式化 LocalDate 為使用者可讀的字串 (例如："2025年6月20日")
    fun formatLocalDate(date: LocalDate): String {
        // 你可以根據需要調整 FormatStyle 或使用自訂模式
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.TAIWAN)
        return date.format(formatter)
    }

    // 格式化時間字串 (HH:mm) - 雖然輸入本身就是 HH:mm，但如果需要轉換可以加入
    // fun formatTime(time: LocalTime): String {
    //     val formatter = DateTimeFormatter.ofPattern("HH:mm")
    //     return time.format(formatter)
    // }

    // 將 LocalDate 和時間字串 (HH:mm) 合併為 LocalDateTime，用於 AlarmManager
    fun combineDateAndTime(date: LocalDate, timeString: String): LocalDateTime? {
        return try {
            val timeParts = timeString.split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                if (hour in 0..23 && minute in 0..59) {
                    date.atTime(hour, minute)
                } else {
                    null // 時間格式或範圍不正確
                }
            } else {
                null // 時間格式不正確
            }
        } catch (e: NumberFormatException) {
            null // 無法解析時間
        }
    }

    // 獲取當前日期的 Epoch Milliseconds
    fun getCurrentDateEpochMillis(): Long {
        return localDateToEpochMillis(LocalDate.now())
    }

    // 獲取當前日期的 LocalDate
    fun getCurrentLocalDate(): LocalDate {
        return LocalDate.now()
    }
}
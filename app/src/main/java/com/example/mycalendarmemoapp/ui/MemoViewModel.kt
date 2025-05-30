package com.example.mycalendarmemoapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.mycalendarmemoapp.data.AppDatabase
import com.example.mycalendarmemoapp.data.Memo
import com.example.mycalendarmemoapp.repository.MemoRepository
import com.example.mycalendarmemoapp.util.DateUtils
import com.example.mycalendarmemoapp.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import android.util.Log

class MemoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MemoRepository
    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    val memosForCurrentDate: LiveData<List<Memo>>

    init {
        val memoDao = AppDatabase.getDatabase(application).memoDao()
        repository = MemoRepository(memoDao)

        // 使用 LiveData 來觀察資料庫變化
        memosForCurrentDate = currentDate.asLiveData().switchMap { date -> // 直接在 LiveData 物件上調用 .switchMap
            repository.getMemosForDate(
                date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ).asLiveData()
        }
    }

    fun addMemo(time: String, title: String, location: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val memoDate = _currentDate.value // 獲取當前日期 (即備忘錄日期)
            val reminderDateTime: LocalDateTime? = DateUtils.combineDateAndTime(memoDate, time)

            val memo = Memo(
                date = _currentDate.value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                time = time,
                title = title,
                location = location?.takeIf { it.isNotBlank() }
            )
            val newMemoId: Long = repository.insertMemo(memo)
            // 可以在這裡加入設定提醒通知的邏輯 [cite: 3]
            if (newMemoId > 0) { // 確認儲存成功
                // 3. 如果時間有效且在未來，則排程通知
                if (reminderDateTime != null && reminderDateTime.isAfter(LocalDateTime.now())) {
                    NotificationHelper.scheduleNotification(
                        getApplication(), // Context
                        newMemoId.toInt(),    // Memo ID 作為通知 ID 和 PendingIntent Request Code
                        title,                // 備忘錄標題
                        reminderDateTime      // 排程的日期和時間
                    )
                    Log.d("MemoViewModel", "Notification scheduled for memo ID: $newMemoId at $reminderDateTime")
                } else if (reminderDateTime != null) {
                    Log.d("MemoViewModel", "Reminder time $reminderDateTime is in the past. Not scheduling.")
                } else {
                    Log.e("MemoViewModel", "Invalid time format for scheduling: $time")
                }
            } else {
                Log.e("MemoViewModel", "Failed to save memo.")
            }
        }


    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMemo(memo)
        }
    }

    // 你也可以加入更新日期的方法，如果 App 需要顯示其他日期的話
    // fun changeDate(newDate: LocalDate) {
    //     _currentDate.value = newDate
    // }
}

// 需要一個 ViewModel Factory 來建立 MemoViewModel
class MemoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
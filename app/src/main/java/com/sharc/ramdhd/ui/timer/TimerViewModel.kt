package com.sharc.ramdhd.ui.timer

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Locale

class TimerViewModel : ViewModel() {
    private val _timerText = MutableLiveData("00:00:00")
    val timerText: LiveData<String> = _timerText

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _timerFinished = MutableLiveData(false)
    val timerFinished: LiveData<Boolean> = _timerFinished

    private val _canStart = MutableLiveData(false)
    val canStart: LiveData<Boolean> = _canStart

    private val _resetPickers = MutableLiveData(false)
    val resetPickers: LiveData<Boolean> = _resetPickers

    private var hours: Int = 0
    private var minutes: Int = 0
    private var seconds: Int = 0

    private var originalTimeMillis: Long = 0
    private var timerService: TimerService? = null

    fun setTimerService(service: TimerService) {
        timerService = service
        observeTimerState(service)
    }

    private fun observeTimerState(service: TimerService) {
        viewModelScope.launch {
            service.timerState.collect { state ->
                when (state) {
                    is TimerState.Running -> {
                        _isRunning.value = true
                        updateTimerText(state.remainingMillis)
                    }
                    is TimerState.Stopped -> {
                        _isRunning.value = false
                        _canStart.value = state.remainingMillis > 0
                        updateTimerText(state.remainingMillis)
                    }
                    is TimerState.Finished -> {
                        _isRunning.value = false
                        _timerFinished.value = true
                        resetToOriginalTime()
                    }
                    TimerState.Idle -> {
                        _isRunning.value = false
                    }
                }
            }
        }
    }

    fun setHours(value: Int) {
        hours = value
        updateOriginalTime()
    }

    fun setMinutes(value: Int) {
        minutes = value
        updateOriginalTime()
    }

    fun setSeconds(value: Int) {
        seconds = value
        updateOriginalTime()
    }

    private fun updateOriginalTime() {
        originalTimeMillis = ((hours * 3600L) + (minutes * 60L) + seconds) * 1000L
        updateTimerText(originalTimeMillis)
        _canStart.value = originalTimeMillis > 0
    }

    fun startTimer() {
        if (_isRunning.value == true) return
        timerService?.startTimer(originalTimeMillis)
    }

    fun stopTimer() {
        timerService?.stopTimer()
    }

    fun resetTimer() {
        timerService?.resetTimer()
        hours = 0
        minutes = 0
        seconds = 0
        updateOriginalTime()
        _resetPickers.value = true
    }

    private fun resetToOriginalTime() {
        updateTimerText(originalTimeMillis)
        _canStart.value = originalTimeMillis > 0
    }

    private fun updateTimerText(millis: Long) {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        _timerText.value = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun onTimerFinishedHandled() {
        _timerFinished.value = false
    }

    fun onResetPickersHandled() {
        _resetPickers.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
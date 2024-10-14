package com.sharc.ramdhd.ui.timer

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    private var countDownTimer: CountDownTimer? = null
    private var remainingTimeMillis: Long = 0
    private var originalTimeMillis: Long = 0

    private var hours: Int = 0
    private var minutes: Int = 0
    private var seconds: Int = 0

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
        remainingTimeMillis = originalTimeMillis
        updateTimerText()
        _canStart.value = originalTimeMillis > 0
    }

    fun startTimer() {
        if (_isRunning.value == true || remainingTimeMillis <= 0) return

        countDownTimer = object : CountDownTimer(remainingTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                _isRunning.value = false
                _timerFinished.value = true
                resetToOriginalTime()
            }
        }.start()

        _isRunning.value = true
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _canStart.value = remainingTimeMillis > 0
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        hours = 0
        minutes = 0
        seconds = 0
        updateOriginalTime()
        _resetPickers.value = true
    }

    private fun resetToOriginalTime() {
        remainingTimeMillis = originalTimeMillis
        updateTimerText()
        _canStart.value = originalTimeMillis > 0
    }

    private fun updateTimerText() {
        val totalSeconds = remainingTimeMillis / 1000
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
        countDownTimer?.cancel()
    }
}
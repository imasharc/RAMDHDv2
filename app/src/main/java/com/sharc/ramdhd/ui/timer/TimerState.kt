package com.sharc.ramdhd.ui.timer

// TimerState.kt
sealed class TimerState {
    object Idle : TimerState()
    data class Running(val remainingMillis: Long) : TimerState()
    data class Stopped(val remainingMillis: Long) : TimerState()
    object Finished : TimerState()
}
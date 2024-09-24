package com.hcyacg.utils

import kotlin.math.floor

class DateUtil {
    companion object {
        fun getTime(time: Long): String {
            val days: Double = (time / 1000 / 60 / 60 / 24).toDouble()
            val daysRound = floor(days)
            val hours = time / 1000 / 60 / 60 - 24 * daysRound
            val hoursRound = floor(hours)
            val minutes = time / 1000 / 60 - 24 * 60 * daysRound - 60 * hoursRound
            val minutesRound = floor(minutes)
            val seconds = time / 1000 - 24 * 60 * 60 * daysRound - 60 * 60 * hoursRound - 60 * minutesRound
            val secondsRound = floor(seconds)
            val timeRound = "${hoursRound.toString().split(".")[0]}:${minutesRound.toString().split(".")[0]}:${secondsRound.toString().split(".")[0]}"
            return timeRound
        }
    }

}
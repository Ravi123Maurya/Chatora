package com.ravi.chatora.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime(timeMillis: Long, formatPattern: String = "h:mm a"): String {

    val instant: Instant? = Instant.ofEpochMilli(timeMillis)
    val formatter = DateTimeFormatter.ofPattern(formatPattern)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return formatter.format(localDateTime)

}
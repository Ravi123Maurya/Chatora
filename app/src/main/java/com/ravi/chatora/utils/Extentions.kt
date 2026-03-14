package com.ravi.chatora.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType


var toast: Toast? = null
fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT){
    toast?.cancel()
    toast = Toast.makeText(this, text, duration)
    toast?.show()
}

fun Modifier.hapticFeedback(hapticFeedbackType: HapticFeedbackType) {

}
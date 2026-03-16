package com.ravi.chatora.utils

import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


var toast: Toast? = null
fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT){
    toast?.cancel()
    toast = Toast.makeText(this, text, duration)
    toast?.show()
}

fun Modifier.hapticFeedback(hapticFeedbackType: HapticFeedbackType) {

}

fun Context.copyToClip(clipboard: Clipboard, text: String, scope: CoroutineScope){
    val clipData = ClipData.newPlainText(
        "Chatora message",
        text
    )
    scope.launch {
        clipboard.setClipEntry(ClipEntry(clipData))
    }
}
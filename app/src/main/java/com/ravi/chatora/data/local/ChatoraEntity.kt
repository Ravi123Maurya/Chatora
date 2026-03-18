package com.ravi.chatora.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.utils.DatabaseConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread


@Entity(tableName = "chatora")
data class ChatoraEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message: String,
    val isUser: Boolean,
    val timeStamp: String
)




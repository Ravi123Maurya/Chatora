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


// Chatora <-> ChatoraEntity Mappers
fun ChatoraEntity.toChatora(): Chatora {
    return Chatora(
        id = id,
        message = message,
        isUser = isUser,
        timeStamp = timeStamp
    )
}

fun Chatora.toChatoraEntity(): ChatoraEntity {
    return ChatoraEntity(
//        id = id,
        message = message,
        isUser = isUser,
        timeStamp = timeStamp
    )
}

fun main() = runBlocking{
    thread {
        println("Work A : Thread: ${Thread.currentThread().name}")
        Thread.sleep(1000)
        println("Work B : Thread: ${Thread.currentThread().name}")
    }

     CoroutineScope(CoroutineScope(Dispatchers.Default).coroutineContext).launch {
        println("Work C : Thread: ${Thread.currentThread().name}")
        delay(1000)
        println("Work D : Thread: ${Thread.currentThread().name}")
    }
    return@runBlocking

}
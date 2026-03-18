package com.ravi.chatora.data.mappers

import com.ravi.chatora.data.local.ChatoraEntity
import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.domain.models.ChatoraHistory

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



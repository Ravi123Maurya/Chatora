package com.ravi.chatora.domain.models



data class Chatora(
    val id: Int = 0,
    val message: String,
    val isUser: Boolean,
    val timeStamp: String
){

}
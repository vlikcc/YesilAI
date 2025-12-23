package com.yesilai.app.data.model

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class MessageSender {
        USER,
        BOT
    }
}

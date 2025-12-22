package com.yesilai.app.data.api

import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(
    val message: String,
    val timestamp: String,
    val user: String = "mobile_user",
    val sessionId: String
)

data class ChatResponse(
    val output: String? = null,
    val message: String? = null,
    val reply: String? = null,
    val response: String? = null,
    val text: String? = null
)

interface ChatApiService {
    @POST("webhook/6a98f1ab-9f5b-43c2-89f0-d878d21358e0")
    suspend fun sendMessage(@Body request: ChatRequest): List<ChatResponse>
}

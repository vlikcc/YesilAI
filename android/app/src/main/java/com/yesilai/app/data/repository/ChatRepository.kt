package com.yesilai.app.data.repository

import com.yesilai.app.data.api.ChatApiService
import com.yesilai.app.data.api.ChatRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ChatRepository {
    private val apiService: ChatApiService
    
    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://n8n.izmirmem.cloud/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ChatApiService::class.java)
    }
    
    suspend fun sendMessage(message: String, sessionId: String): Result<String> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val request = ChatRequest(
                message = message,
                timestamp = dateFormat.format(Date()),
                sessionId = sessionId
            )
            
            val response = apiService.sendMessage(request)
            
            if (response.isNotEmpty()) {
                val firstResponse = response[0]
                val responseText = firstResponse.output 
                    ?: firstResponse.message 
                    ?: firstResponse.reply 
                    ?: firstResponse.response 
                    ?: firstResponse.text 
                    ?: "Webhook bağlantısı başarılı ama yanıt formatı tanınmıyor."
                Result.success(responseText)
            } else {
                Result.success("Webhook'tan boş yanıt alındı.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

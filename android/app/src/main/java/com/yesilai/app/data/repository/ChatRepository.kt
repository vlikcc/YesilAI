package com.yesilai.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yesilai.app.data.api.ChatApiService
import com.yesilai.app.data.api.ChatRequest
import com.yesilai.app.data.model.ChatMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ChatRepository {
    private val apiService: ChatApiService
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val userId: String?
        get() = auth.currentUser?.uid
    
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
    
    suspend fun saveMessage(message: ChatMessage) {
        val uid = userId ?: return
        
        val messageData = hashMapOf(
            "text" to message.text,
            "sender" to message.sender.name,
            "timestamp" to message.timestamp
        )
        
        try {
            db.collection("users").document(uid)
                .collection("messages")
                .add(messageData)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun loadChatHistory(): List<ChatMessage> {
        val uid = userId ?: return emptyList()
        
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(100)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                val text = doc.getString("text") ?: return@mapNotNull null
                val senderName = doc.getString("sender") ?: return@mapNotNull null
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                
                val sender = try {
                    ChatMessage.MessageSender.valueOf(senderName)
                } catch (e: Exception) {
                    ChatMessage.MessageSender.BOT
                }
                
                ChatMessage(
                    id = timestamp,
                    text = text,
                    sender = sender,
                    timestamp = timestamp
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun clearChatHistory() {
        val uid = userId ?: return
        
        try {
            val snapshot = db.collection("users").document(uid)
                .collection("messages")
                .get()
                .await()
            
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

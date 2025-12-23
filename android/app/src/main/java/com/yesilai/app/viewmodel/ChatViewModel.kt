package com.yesilai.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yesilai.app.data.model.ChatMessage
import com.yesilai.app.data.repository.ChatRepository
import com.yesilai.app.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingHistory: Boolean = true,
    val inputText: String = ""
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository()
    private val sessionIdKey = stringPreferencesKey("session_id")
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var sessionId: String = ""
    
    init {
        viewModelScope.launch {
            sessionId = getOrCreateSessionId()
            loadChatHistory()
        }
    }
    
    private suspend fun loadChatHistory() {
        _uiState.value = _uiState.value.copy(isLoadingHistory = true)
        
        val history = repository.loadChatHistory()
        
        if (history.isEmpty()) {
            // Add welcome message
            val welcomeMessage = ChatMessage(
                id = 1,
                text = "Merhaba! Bağımlılıkla mücadele yolculuğunda sana destek olmak için buradayım. Nelerden bahsetmek istersin?",
                sender = ChatMessage.MessageSender.BOT
            )
            _uiState.value = _uiState.value.copy(
                messages = listOf(welcomeMessage),
                isLoadingHistory = false
            )
            repository.saveMessage(welcomeMessage)
        } else {
            _uiState.value = _uiState.value.copy(
                messages = history,
                isLoadingHistory = false
            )
        }
    }
    
    private suspend fun getOrCreateSessionId(): String {
        val context = getApplication<Application>()
        val storedId = context.dataStore.data.map { preferences ->
            preferences[sessionIdKey]
        }.first()
        
        return storedId ?: run {
            val newId = "session_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(9)}"
            context.dataStore.edit { preferences ->
                preferences[sessionIdKey] = newId
            }
            newId
        }
    }
    
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }
    
    fun sendMessage() {
        val currentMessage = _uiState.value.inputText.trim()
        if (currentMessage.isEmpty() || _uiState.value.isLoading) return
        
        viewModelScope.launch {
            // Add user message
            val userMessage = ChatMessage(
                id = System.currentTimeMillis(),
                text = currentMessage,
                sender = ChatMessage.MessageSender.USER
            )
            
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMessage,
                inputText = "",
                isLoading = true
            )
            
            // Save user message to Firestore
            repository.saveMessage(userMessage)
            
            // Send to API
            val result = repository.sendMessage(currentMessage, sessionId)
            
            val botResponseText = result.getOrElse { error ->
                when {
                    error.message?.contains("timeout", ignoreCase = true) == true ->
                        "⚠️ İstek zaman aşımına uğradı. Sunucu yanıt vermekte yavaş olabilir. Lütfen tekrar deneyin."
                    error.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                        "⚠️ Sunucuya ulaşılamıyor. İnternet bağlantınızı kontrol edin."
                    else ->
                        "⚠️ Bir sorun oluştu: ${error.message}"
                }
            }
            
            val botMessage = ChatMessage(
                id = System.currentTimeMillis() + 1,
                text = botResponseText,
                sender = ChatMessage.MessageSender.BOT
            )
            
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + botMessage,
                isLoading = false
            )
            
            // Save bot message to Firestore
            repository.saveMessage(botMessage)
        }
    }
    
    fun testWebhook() {
        viewModelScope.launch {
            val testMessage = "Test mesajı - ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
            
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.sendMessage(testMessage, sessionId)
            
            val responseText = result.getOrElse { "Test hatası: ${it.message}" }
            
            val testResponseMessage = ChatMessage(
                id = System.currentTimeMillis(),
                text = "🧪 Webhook Test: $responseText",
                sender = ChatMessage.MessageSender.BOT
            )
            
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + testResponseMessage,
                isLoading = false
            )
            
            // Save test message to Firestore
            repository.saveMessage(testResponseMessage)
        }
    }
    
    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            
            val welcomeMessage = ChatMessage(
                id = 1,
                text = "Merhaba! Bağımlılıkla mücadele yolculuğunda sana destek olmak için buradayım. Nelerden bahsetmek istersin?",
                sender = ChatMessage.MessageSender.BOT
            )
            
            _uiState.value = _uiState.value.copy(messages = listOf(welcomeMessage))
            repository.saveMessage(welcomeMessage)
        }
    }
}

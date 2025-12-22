package com.yesilai.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentUserEmail = MutableStateFlow(auth.currentUser?.email ?: "")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    companion object {
        // Web client ID from google-services.json
        private const val WEB_CLIENT_ID = "360765057146-8fdftkhgemqnbuc3s5oikbr77kra8op7.apps.googleusercontent.com"
    }
    
    // Register new user
    suspend fun register(email: String, password: String, confirmPassword: String): Boolean {
        _errorMessage.value = ""
        
        // Validation
        if (email.isBlank()) {
            _errorMessage.value = "E-posta adresi gereklidir"
            return false
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            _errorMessage.value = "Geçerli bir e-posta adresi giriniz"
            return false
        }
        
        if (password.isBlank()) {
            _errorMessage.value = "Şifre gereklidir"
            return false
        }
        
        if (password.length < 6) {
            _errorMessage.value = "Şifre en az 6 karakter olmalıdır"
            return false
        }
        
        if (password != confirmPassword) {
            _errorMessage.value = "Şifreler eşleşmiyor"
            return false
        }
        
        _isLoading.value = true
        
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            _currentUserEmail.value = email
            _isLoggedIn.value = true
            _isLoading.value = false
            true
        } catch (e: FirebaseAuthWeakPasswordException) {
            _errorMessage.value = "Şifre çok zayıf"
            _isLoading.value = false
            false
        } catch (e: FirebaseAuthUserCollisionException) {
            _errorMessage.value = "Bu e-posta adresi zaten kayıtlı"
            _isLoading.value = false
            false
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            _errorMessage.value = "Geçersiz e-posta adresi"
            _isLoading.value = false
            false
        } catch (e: Exception) {
            _errorMessage.value = "Kayıt başarısız: ${e.localizedMessage}"
            _isLoading.value = false
            false
        }
    }
    
    // Login user
    suspend fun login(email: String, password: String): Boolean {
        _errorMessage.value = ""
        
        if (email.isBlank()) {
            _errorMessage.value = "E-posta adresi gereklidir"
            return false
        }
        
        if (password.isBlank()) {
            _errorMessage.value = "Şifre gereklidir"
            return false
        }
        
        _isLoading.value = true
        
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            _currentUserEmail.value = auth.currentUser?.email ?: email
            _isLoggedIn.value = true
            _isLoading.value = false
            true
        } catch (e: FirebaseAuthInvalidUserException) {
            _errorMessage.value = "Bu e-posta adresi kayıtlı değil"
            _isLoading.value = false
            false
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            _errorMessage.value = "Şifre yanlış"
            _isLoading.value = false
            false
        } catch (e: Exception) {
            _errorMessage.value = "Giriş başarısız: ${e.localizedMessage}"
            _isLoading.value = false
            false
        }
    }
    
    // Google Sign-In
    suspend fun signInWithGoogle(context: Context): Boolean {
        _errorMessage.value = ""
        _isLoading.value = true
        
        return try {
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            val result = credentialManager.getCredential(context, request)
            
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(firebaseCredential).await()
                        
                        _currentUserEmail.value = authResult.user?.email ?: ""
                        _isLoggedIn.value = true
                        _isLoading.value = false
                        true
                    } else {
                        _errorMessage.value = "Beklenmeyen kimlik bilgisi türü"
                        _isLoading.value = false
                        false
                    }
                }
                else -> {
                    _errorMessage.value = "Beklenmeyen kimlik bilgisi türü"
                    _isLoading.value = false
                    false
                }
            }
        } catch (e: GetCredentialCancellationException) {
            _isLoading.value = false
            false
        } catch (e: GetCredentialException) {
            _errorMessage.value = "Google ile giriş başarısız: ${e.message}"
            _isLoading.value = false
            false
        } catch (e: Exception) {
            _errorMessage.value = "Google ile giriş başarısız: ${e.localizedMessage}"
            _isLoading.value = false
            false
        }
    }
    
    // Logout user
    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _currentUserEmail.value = ""
    }
    
    fun clearError() {
        _errorMessage.value = ""
    }
}

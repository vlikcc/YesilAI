package com.yesilai.app.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val age: Int? = null,
    val gender: Gender? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    enum class Gender(val displayName: String, val rawValue: String) {
        MALE("Erkek", "erkek"),
        FEMALE("Kadın", "kadın"),
        OTHER("Diğer", "diğer"),
        PREFER_NOT_TO_SAY("Belirtmek İstemiyorum", "belirtmek_istemiyorum")
    }
    
    val fullName: String
        get() = if (firstName.isEmpty() && lastName.isEmpty()) email 
                else "$firstName $lastName".trim()
    
    val isProfileComplete: Boolean
        get() = firstName.isNotEmpty() && lastName.isNotEmpty()
}

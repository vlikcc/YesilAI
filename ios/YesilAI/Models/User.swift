import Foundation

struct User: Codable, Identifiable {
    var id: String
    var email: String
    var firstName: String
    var lastName: String
    var age: Int?
    var gender: Gender?
    var createdAt: Date
    var updatedAt: Date
    
    enum Gender: String, Codable, CaseIterable {
        case male = "erkek"
        case female = "kadın"
        case preferNotToSay = "belirtmek_istemiyorum"
        
        var displayName: String {
            switch self {
            case .male: return "Erkek"
            case .female: return "Kadın"
            case .preferNotToSay: return "Belirtmek İstemiyorum"
            }
        }
    }
    
    init(
        id: String,
        email: String,
        firstName: String = "",
        lastName: String = "",
        age: Int? = nil,
        gender: Gender? = nil,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.email = email
        self.firstName = firstName
        self.lastName = lastName
        self.age = age
        self.gender = gender
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
    
    var fullName: String {
        if firstName.isEmpty && lastName.isEmpty {
            return email
        }
        return "\(firstName) \(lastName)".trimmingCharacters(in: .whitespaces)
    }
    
    var isProfileComplete: Bool {
        return !firstName.isEmpty && !lastName.isEmpty
    }
}

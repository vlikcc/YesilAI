import Foundation

class SessionManager {
    static let shared = SessionManager()
    private let sessionIdKey = "yesilai_session_id"
    
    private init() {}
    
    var sessionId: String {
        if let storedId = UserDefaults.standard.string(forKey: sessionIdKey) {
            return storedId
        }
        
        let newId = "session_\(Int(Date().timeIntervalSince1970 * 1000))_\(UUID().uuidString.prefix(9))"
        UserDefaults.standard.set(newId, forKey: sessionIdKey)
        return newId
    }
}

import Foundation

class ChatAPIService {
    static let shared = ChatAPIService()
    
    private let webhookURL = "https://n8n.izmirmem.cloud/webhook/6a98f1ab-9f5b-43c2-89f0-d878d21358e0"
    private let sessionManager = SessionManager.shared
    
    private init() {}
    
    func sendMessage(_ message: String) async throws -> String {
        guard let url = URL(string: webhookURL) else {
            throw ChatError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.timeoutInterval = 60
        
        let dateFormatter = ISO8601DateFormatter()
        let body: [String: Any] = [
            "message": message,
            "timestamp": dateFormatter.string(from: Date()),
            "user": "mobile_user",
            "sessionId": sessionManager.sessionId
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw ChatError.invalidResponse
        }
        
        guard httpResponse.statusCode < 500 else {
            throw ChatError.serverError(httpResponse.statusCode)
        }
        
        // Try to parse as array first
        if let responseArray = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]],
           let firstResponse = responseArray.first {
            return extractResponseText(from: firstResponse)
        }
        
        // Try to parse as object
        if let responseObject = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            return extractResponseText(from: responseObject)
        }
        
        return "Webhook'tan boş yanıt alındı."
    }
    
    private func extractResponseText(from response: [String: Any]) -> String {
        return response["output"] as? String
            ?? response["message"] as? String
            ?? response["reply"] as? String
            ?? response["response"] as? String
            ?? response["text"] as? String
            ?? "Webhook bağlantısı başarılı ama yanıt formatı tanınmıyor."
    }
}

enum ChatError: LocalizedError {
    case invalidURL
    case invalidResponse
    case serverError(Int)
    case networkError(String)
    
    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "⚠️ Webhook URL'si geçersiz."
        case .invalidResponse:
            return "⚠️ Sunucudan geçersiz yanıt alındı."
        case .serverError(let code):
            return "⚠️ Sunucu hatası (\(code))."
        case .networkError(let message):
            return "⚠️ Ağ hatası: \(message)"
        }
    }
}

import Foundation
import SwiftUI

@MainActor
class ChatViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = [
        ChatMessage(
            id: 1,
            text: "Merhaba! Bağımlılıkla mücadele yolculuğunda sana destek olmak için buradayım. Nelerden bahsetmek istersin?",
            sender: .bot
        )
    ]
    @Published var inputText: String = ""
    @Published var isLoading: Bool = false
    
    private let apiService = ChatAPIService.shared
    
    func sendMessage() {
        let messageText = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !messageText.isEmpty, !isLoading else { return }
        
        // Add user message
        let userMessage = ChatMessage(
            id: Int64(Date().timeIntervalSince1970 * 1000),
            text: messageText,
            sender: .user
        )
        messages.append(userMessage)
        inputText = ""
        isLoading = true
        
        Task {
            do {
                let response = try await apiService.sendMessage(messageText)
                let botMessage = ChatMessage(
                    id: Int64(Date().timeIntervalSince1970 * 1000) + 1,
                    text: response,
                    sender: .bot
                )
                messages.append(botMessage)
            } catch {
                let errorMessage = ChatMessage(
                    id: Int64(Date().timeIntervalSince1970 * 1000) + 1,
                    text: error.localizedDescription,
                    sender: .bot
                )
                messages.append(errorMessage)
            }
            isLoading = false
        }
    }
    
    func testWebhook() {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "HH:mm:ss"
        let testMessage = "Test mesajı - \(dateFormatter.string(from: Date()))"
        
        isLoading = true
        
        Task {
            do {
                let response = try await apiService.sendMessage(testMessage)
                let testResponseMessage = ChatMessage(
                    id: Int64(Date().timeIntervalSince1970 * 1000),
                    text: "🧪 Webhook Test: \(response)",
                    sender: .bot
                )
                messages.append(testResponseMessage)
            } catch {
                let errorMessage = ChatMessage(
                    id: Int64(Date().timeIntervalSince1970 * 1000),
                    text: "🧪 Test hatası: \(error.localizedDescription)",
                    sender: .bot
                )
                messages.append(errorMessage)
            }
            isLoading = false
        }
    }
}

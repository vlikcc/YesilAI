import Foundation
import SwiftUI
import FirebaseFirestore
import FirebaseAuth

@MainActor
class ChatViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    @Published var inputText: String = ""
    @Published var isLoading: Bool = false
    @Published var isLoadingHistory: Bool = false
    
    private let apiService = ChatAPIService.shared
    private let db = Firestore.firestore()
    
    private var userId: String? {
        Auth.auth().currentUser?.uid
    }
    
    init() {
        loadChatHistory()
    }
    
    func loadChatHistory() {
        guard let userId = userId else {
            addWelcomeMessage()
            return
        }
        
        isLoadingHistory = true
        
        db.collection("users").document(userId).collection("messages")
            .order(by: "timestamp", descending: false)
            .limit(to: 100)
            .getDocuments { [weak self] snapshot, error in
                guard let self = self else { return }
                
                Task { @MainActor in
                    self.isLoadingHistory = false
                    
                    if let error = error {
                        print("Error loading chat history: \(error.localizedDescription)")
                        self.addWelcomeMessage()
                        return
                    }
                    
                    guard let documents = snapshot?.documents, !documents.isEmpty else {
                        self.addWelcomeMessage()
                        return
                    }
                    
                    self.messages = documents.compactMap { doc -> ChatMessage? in
                        let data = doc.data()
                        guard let text = data["text"] as? String,
                              let senderRaw = data["sender"] as? String,
                              let sender = ChatMessage.MessageSender(rawValue: senderRaw),
                              let timestamp = data["timestamp"] as? Timestamp else {
                            return nil
                        }
                        
                        return ChatMessage(
                            id: Int64(timestamp.dateValue().timeIntervalSince1970 * 1000),
                            text: text,
                            sender: sender
                        )
                    }
                    
                    if self.messages.isEmpty {
                        self.addWelcomeMessage()
                    }
                }
            }
    }
    
    private func addWelcomeMessage() {
        let welcomeMessage = ChatMessage(
            id: 1,
            text: "Merhaba! Bağımlılıkla mücadele yolculuğunda sana destek olmak için buradayım. Nelerden bahsetmek istersin?",
            sender: .bot
        )
        messages = [welcomeMessage]
        saveMessage(welcomeMessage)
    }
    
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
        saveMessage(userMessage)
        
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
                saveMessage(botMessage)
            } catch {
                let errorMessage = ChatMessage(
                    id: Int64(Date().timeIntervalSince1970 * 1000) + 1,
                    text: error.localizedDescription,
                    sender: .bot
                )
                messages.append(errorMessage)
                saveMessage(errorMessage)
            }
            isLoading = false
        }
    }
    
    private func saveMessage(_ message: ChatMessage) {
        guard let userId = userId else { return }
        
        let messageData: [String: Any] = [
            "text": message.text,
            "sender": message.sender.rawValue,
            "timestamp": FieldValue.serverTimestamp()
        ]
        
        db.collection("users").document(userId).collection("messages")
            .addDocument(data: messageData) { error in
                if let error = error {
                    print("Error saving message: \(error.localizedDescription)")
                }
            }
    }
    
    func clearChatHistory() {
        guard let userId = userId else { return }
        
        // Delete all messages from Firestore
        db.collection("users").document(userId).collection("messages")
            .getDocuments { [weak self] snapshot, error in
                guard let documents = snapshot?.documents else { return }
                
                let batch = self?.db.batch()
                documents.forEach { batch?.deleteDocument($0.reference) }
                
                batch?.commit { _ in
                    Task { @MainActor in
                        self?.messages = []
                        self?.addWelcomeMessage()
                    }
                }
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
                saveMessage(testResponseMessage)
            } catch {
                let errorMessage = ChatMessage(
                    id: Int64(Date().timeIntervalSince1970 * 1000),
                    text: "🧪 Test hatası: \(error.localizedDescription)",
                    sender: .bot
                )
                messages.append(errorMessage)
                saveMessage(errorMessage)
            }
            isLoading = false
        }
    }
}

import Foundation

struct ChatMessage: Identifiable, Equatable {
    let id: Int64
    let text: String
    let sender: MessageSender
    
    enum MessageSender: String {
        case user
        case bot
    }
}

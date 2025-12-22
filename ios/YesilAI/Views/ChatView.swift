import SwiftUI

struct ChatView: View {
    @ObservedObject var navigationManager: NavigationManager
    @StateObject private var viewModel = ChatViewModel()
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("YeşilAI")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(YesilTheme.textPrimary)
            }
            .frame(maxWidth: .infinity)
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(YesilTheme.background)
            .overlay(
                Rectangle()
                    .fill(YesilTheme.border)
                    .frame(height: 1),
                alignment: .bottom
            )
            
            // Messages List
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(viewModel.messages) { message in
                            MessageBubbleView(message: message)
                                .id(message.id)
                        }
                    }
                    .padding(16)
                }
                .onChange(of: viewModel.messages.count) { _ in
                    if let lastMessage = viewModel.messages.last {
                        withAnimation {
                            proxy.scrollTo(lastMessage.id, anchor: .bottom)
                        }
                    }
                }
            }
            
            // Loading indicator
            if viewModel.isLoading {
                HStack {
                    Image("YesilAI")
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 32, height: 32)
                        .clipShape(Circle())
                    
                    ProgressView()
                        .tint(YesilTheme.primary)
                    
                    Text("YeşilAI yazıyor...")
                        .font(.system(size: 14))
                        .foregroundColor(YesilTheme.textSecondary)
                        .padding(.leading, 8)
                    
                    Spacer()
                }
                .padding(16)
            }
            
            // Message Input
            HStack(spacing: 12) {
                // Input Field
                TextField("Mesaj yazınız...", text: $viewModel.inputText, axis: .vertical)
                    .lineLimit(1...4)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(Color(hex: "f3f4f6"))
                    .cornerRadius(25)
                
                // Send Button
                Button(action: {
                    viewModel.sendMessage()
                }) {
                    Text("➤")
                        .font(.system(size: 24))
                        .foregroundColor(.white)
                        .frame(width: 48, height: 48)
                        .background(YesilTheme.primary)
                        .clipShape(Circle())
                }
                .disabled(viewModel.isLoading)
            }
            .padding(16)
            .background(Color.white)
            .overlay(
                Rectangle()
                    .fill(YesilTheme.border)
                    .frame(height: 1),
                alignment: .top
            )
        }
        .background(YesilTheme.background)
    }
}

struct MessageBubbleView: View {
    let message: ChatMessage
    
    var isBot: Bool {
        message.sender == .bot
    }
    
    var body: some View {
        HStack(alignment: .bottom, spacing: 8) {
            if isBot {
                Image("YesilAI")
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: 32, height: 32)
                    .clipShape(Circle())
            }
            
            if !isBot {
                Spacer()
            }
            
            Text(message.text)
                .font(.system(size: 14))
                .foregroundColor(isBot ? YesilTheme.textPrimary : .white)
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(isBot ? YesilTheme.botBubble : YesilTheme.primary)
                .clipShape(
                    RoundedRectangle(
                        cornerRadius: 16,
                        style: .continuous
                    )
                )
                .frame(maxWidth: 280, alignment: isBot ? .leading : .trailing)
            
            if isBot {
                Spacer()
            }
        }
        .frame(maxWidth: .infinity, alignment: isBot ? .leading : .trailing)
    }
}

#Preview {
    ChatView(navigationManager: NavigationManager())
}

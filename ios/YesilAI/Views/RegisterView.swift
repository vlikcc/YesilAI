import SwiftUI

struct RegisterView: View {
    @ObservedObject var navigationManager: NavigationManager
    @StateObject private var authManager = AuthManager.shared
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer()
                    .frame(height: 60)
                
                // Logo
                Image("yesil-ai-koyu")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 80, height: 80)
                    .padding(.bottom, 24)
                
                // Title
                Text("YeşilAI'ya Kayıt Ol")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(YesilTheme.primary)
                    .multilineTextAlignment(.center)
                
                // Subtitle
                Text("Bağımlılıkla mücadele yolculuğunda sana destek olmak için buradayız.")
                    .font(.system(size: 16))
                    .foregroundColor(YesilTheme.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
                    .padding(.top, 8)
                    .padding(.bottom, 32)
                
                // Error Message
                if !authManager.errorMessage.isEmpty {
                    Text(authManager.errorMessage)
                        .font(.system(size: 14))
                        .foregroundColor(YesilTheme.error)
                        .multilineTextAlignment(.center)
                        .padding(.bottom, 16)
                }
                
                // Form Fields
                VStack(spacing: 16) {
                    // Email Input
                    TextField("E-posta Adresi", text: $email)
                        .textFieldStyle(YesilTextFieldStyle())
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                        .autocorrectionDisabled()
                        .disabled(authManager.isLoading)
                    
                    // Password Input
                    SecureField("Şifre", text: $password)
                        .textFieldStyle(YesilTextFieldStyle())
                        .disabled(authManager.isLoading)
                    
                    // Confirm Password Input
                    SecureField("Şifre Tekrar", text: $confirmPassword)
                        .textFieldStyle(YesilTextFieldStyle())
                        .disabled(authManager.isLoading)
                }
                
                // Register Button
                Button(action: {
                    Task {
                        if await authManager.register(email: email, password: password, confirmPassword: confirmPassword) {
                            navigationManager.navigateToMain()
                        }
                    }
                }) {
                    HStack {
                        if authManager.isLoading {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Text("Kayıt Ol")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(YesilTheme.primary)
                    .cornerRadius(25)
                    .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
                }
                .padding(.top, 24)
                .disabled(authManager.isLoading)
                
                Spacer()
                    .frame(height: 32)
                
                // Login Link
                Button(action: {
                    navigationManager.navigateToLogin()
                }) {
                    Text("Zaten hesabın var mı? Giriş yap")
                        .font(.system(size: 14))
                        .foregroundColor(YesilTheme.primary)
                        .underline()
                }
                .disabled(authManager.isLoading)
                
                Spacer()
            }
            .padding(.horizontal, 24)
        }
        .background(YesilTheme.background)
    }
}

#Preview {
    RegisterView(navigationManager: NavigationManager())
}

import SwiftUI
import FirebaseFirestore
import FirebaseAuth

struct LoginView: View {
    @ObservedObject var navigationManager: NavigationManager
    @StateObject private var authManager = AuthManager.shared
    @State private var email = ""
    @State private var password = ""
    
    private let db = Firestore.firestore()
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Banner Image Section
                ZStack {
                    Image("yesil-ai-afis")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(height: 200)
                    
                    Color.black.opacity(0.1)
                }
                .frame(height: 200)
                .background(Color(hex: "f8f9fa"))
                
                // Main Content Card
                VStack(spacing: 0) {
                    // Logo
                    Image("yesil-ai-koyu")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 80, height: 80)
                        .padding(.bottom, 24)
                    
                    // Welcome Text
                    Text("Yeşilay'a Hoş Geldiniz")
                        .font(.system(size: 30, weight: .bold))
                        .foregroundColor(YesilTheme.textPrimary)
                        .multilineTextAlignment(.center)
                    
                    Text("Bağımlılıkla mücadelede yalnız değilsin.")
                        .font(.system(size: 16))
                        .foregroundColor(YesilTheme.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 8)
                    
                    // Error Message
                    if !authManager.errorMessage.isEmpty {
                        Text(authManager.errorMessage)
                            .font(.system(size: 14))
                            .foregroundColor(YesilTheme.error)
                            .multilineTextAlignment(.center)
                            .padding(.top, 16)
                    }
                    
                    // Email Input
                    TextField("", text: $email, prompt: Text("E-posta Adresi").foregroundColor(YesilTheme.textPlaceholder))
                        .textFieldStyle(YesilTextFieldStyle())
                        .padding(.top, 32)
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                        .autocorrectionDisabled()
                        .disabled(authManager.isLoading)
                    
                    // Password Input
                    SecureField("", text: $password, prompt: Text("Şifre").foregroundColor(YesilTheme.textPlaceholder))
                        .textFieldStyle(YesilTextFieldStyle())
                        .padding(.top, 16)
                        .disabled(authManager.isLoading)
                    
                    // Login Button
                    Button(action: {
                        Task {
                            if await authManager.login(email: email, password: password) {
                                navigationManager.navigateToMain()
                            }
                        }
                    }) {
                        HStack {
                            if authManager.isLoading {
                                ProgressView()
                                    .tint(.white)
                            } else {
                                Text("Giriş Yap")
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
                    
                    // Divider with "Veya"
                    HStack {
                        Rectangle()
                            .fill(YesilTheme.border)
                            .frame(height: 1)
                        
                        Text("Veya")
                            .font(.system(size: 14))
                            .foregroundColor(YesilTheme.textSecondary)
                            .padding(.horizontal, 8)
                        
                        Rectangle()
                            .fill(YesilTheme.border)
                            .frame(height: 1)
                    }
                    .padding(.vertical, 24)
                    
                    // Social Login Buttons
                    VStack(spacing: 12) {
                        // Google Sign In Button
                        Button(action: {
                            Task {
                                if await authManager.signInWithGoogle() {
                                    await checkProfileAndNavigate()
                                }
                            }
                        }) {
                            HStack(spacing: 12) {
                                Image(systemName: "g.circle.fill")
                                    .font(.system(size: 20))
                                    .foregroundColor(.red)
                                Text("Google ile Giriş Yap")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(YesilTheme.textPrimary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.white)
                            .cornerRadius(25)
                            .overlay(
                                RoundedRectangle(cornerRadius: 25)
                                    .stroke(YesilTheme.border, lineWidth: 1)
                            )
                        }
                        .disabled(authManager.isLoading)
                        
                        // Apple Sign In Button
                        Button(action: {
                            Task {
                                if await authManager.signInWithApple() {
                                    await checkProfileAndNavigate()
                                }
                            }
                        }) {
                            HStack(spacing: 12) {
                                Image(systemName: "apple.logo")
                                    .font(.system(size: 20))
                                    .foregroundColor(.white)
                                Text("Apple ile Giriş Yap")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(.white)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.black)
                            .cornerRadius(25)
                        }
                        .disabled(authManager.isLoading)
                    }
                    .padding(.bottom, 16)
                    
                    // Register Link
                    Button(action: {
                        navigationManager.navigateToRegister()
                    }) {
                        Text("Hesabın yok mu? Kayıt ol")
                            .font(.system(size: 14))
                            .foregroundColor(YesilTheme.primary)
                            .underline()
                    }
                    .disabled(authManager.isLoading)
                }
                .padding(24)
                .background(Color.white)
                .cornerRadius(24, corners: [.topLeft, .topRight])
                .offset(y: -32)
            }
        }
        .background(YesilTheme.background)
        .ignoresSafeArea(.all, edges: .bottom)
    }
    
    private func checkProfileAndNavigate() async {
        guard let userId = Auth.auth().currentUser?.uid else {
            navigationManager.navigateToMain()
            return
        }
        
        do {
            let document = try await db.collection("users").document(userId).getDocument()
            
            if document.exists,
               let data = document.data(),
               let firstName = data["firstName"] as? String,
               let lastName = data["lastName"] as? String,
               !firstName.isEmpty,
               !lastName.isEmpty {
                // Profile is complete, go to main
                navigationManager.navigateToMain()
            } else {
                // Profile is incomplete, go to profile completion
                navigationManager.navigateToProfileCompletion()
            }
        } catch {
            // On error, redirect to profile completion to be safe
            navigationManager.navigateToProfileCompletion()
        }
    }
}

// Custom TextField Style
struct YesilTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(YesilTheme.background)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(YesilTheme.border, lineWidth: 1)
            )
    }
}

// Corner radius extension for specific corners
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}

#Preview {
    LoginView(navigationManager: NavigationManager())
}

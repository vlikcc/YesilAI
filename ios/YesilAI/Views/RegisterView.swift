import SwiftUI
import FirebaseFirestore
import FirebaseAuth

struct RegisterView: View {
    @ObservedObject var navigationManager: NavigationManager
    @StateObject private var authManager = AuthManager.shared
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var firstName = ""
    @State private var lastName = ""
    @State private var age = ""
    @State private var selectedGender: User.Gender?
    
    private let db = Firestore.firestore()
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer()
                    .frame(height: 40)
                
                // Logo
                Image("yesil-ai-koyu")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 80, height: 80)
                    .padding(.bottom, 16)
                
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
                    .padding(.bottom, 24)
                
                // Error Message
                if !authManager.errorMessage.isEmpty {
                    Text(authManager.errorMessage)
                        .font(.system(size: 14))
                        .foregroundColor(YesilTheme.error)
                        .multilineTextAlignment(.center)
                        .padding(.bottom, 16)
                }
                
                // Form Fields
                VStack(spacing: 12) {
                    // Name Fields Row
                    HStack(spacing: 12) {
                        TextField("", text: $firstName, prompt: Text("İsim").foregroundColor(YesilTheme.textPlaceholder))
                            .textFieldStyle(YesilTextFieldStyle())
                            .autocapitalization(.words)
                            .disabled(authManager.isLoading)
                        
                        TextField("", text: $lastName, prompt: Text("Soyisim").foregroundColor(YesilTheme.textPlaceholder))
                            .textFieldStyle(YesilTextFieldStyle())
                            .autocapitalization(.words)
                            .disabled(authManager.isLoading)
                    }
                    
                    // Age and Gender Row
                    HStack(spacing: 12) {
                        TextField("", text: $age, prompt: Text("Yaş").foregroundColor(YesilTheme.textPlaceholder))
                            .textFieldStyle(YesilTextFieldStyle())
                            .keyboardType(.numberPad)
                            .disabled(authManager.isLoading)
                            .frame(width: 80)
                        
                        // Gender Picker
                        Menu {
                            ForEach(User.Gender.allCases, id: \.self) { gender in
                                Button(action: { selectedGender = gender }) {
                                    Text(gender.displayName)
                                }
                            }
                        } label: {
                            HStack {
                                Text(selectedGender?.displayName ?? "Cinsiyet")
                                    .foregroundColor(selectedGender == nil ? YesilTheme.textPlaceholder : YesilTheme.textPrimary)
                                Spacer()
                                Image(systemName: "chevron.down")
                                    .foregroundColor(YesilTheme.textSecondary)
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 12)
                            .background(YesilTheme.background)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(YesilTheme.border, lineWidth: 1)
                            )
                        }
                        .disabled(authManager.isLoading)
                    }
                    
                    // Email Input
                    TextField("", text: $email, prompt: Text("E-posta Adresi").foregroundColor(YesilTheme.textPlaceholder))
                        .textFieldStyle(YesilTextFieldStyle())
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                        .autocorrectionDisabled()
                        .disabled(authManager.isLoading)
                    
                    // Password Input
                    SecureField("", text: $password, prompt: Text("Şifre").foregroundColor(YesilTheme.textPlaceholder))
                        .textFieldStyle(YesilTextFieldStyle())
                        .disabled(authManager.isLoading)
                    
                    // Confirm Password Input
                    SecureField("", text: $confirmPassword, prompt: Text("Şifre Tekrar").foregroundColor(YesilTheme.textPlaceholder))
                        .textFieldStyle(YesilTextFieldStyle())
                        .disabled(authManager.isLoading)
                }
                
                // Register Button
                Button(action: {
                    Task {
                        if await authManager.register(email: email, password: password, confirmPassword: confirmPassword) {
                            // Save user profile to Firestore
                            if let userId = Auth.auth().currentUser?.uid {
                                var userData: [String: Any] = [
                                    "email": email,
                                    "firstName": firstName,
                                    "lastName": lastName,
                                    "createdAt": FieldValue.serverTimestamp(),
                                    "updatedAt": FieldValue.serverTimestamp()
                                ]
                                
                                if let ageInt = Int(age) {
                                    userData["age"] = ageInt
                                }
                                
                                if let gender = selectedGender {
                                    userData["gender"] = gender.rawValue
                                }
                                
                                try? await db.collection("users").document(userId).setData(userData)
                            }
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
                    .frame(height: 24)
                
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

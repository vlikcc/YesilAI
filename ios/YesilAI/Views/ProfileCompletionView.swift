import SwiftUI
import FirebaseFirestore
import FirebaseAuth

struct ProfileCompletionView: View {
    @ObservedObject var navigationManager: NavigationManager
    @State private var firstName = ""
    @State private var lastName = ""
    @State private var age = ""
    @State private var selectedGender: User.Gender?
    @State private var isLoading = false
    @State private var errorMessage = ""
    
    private let db = Firestore.firestore()
    
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
                Text("Profilini Tamamla")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(YesilTheme.primary)
                    .multilineTextAlignment(.center)
                
                // Subtitle
                Text("Seni daha iyi tanımamız için birkaç bilgiye ihtiyacımız var.")
                    .font(.system(size: 16))
                    .foregroundColor(YesilTheme.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
                    .padding(.top, 8)
                    .padding(.bottom, 32)
                
                // Error Message
                if !errorMessage.isEmpty {
                    Text(errorMessage)
                        .font(.system(size: 14))
                        .foregroundColor(YesilTheme.error)
                        .multilineTextAlignment(.center)
                        .padding(.bottom, 16)
                }
                
                // Form Fields
                VStack(spacing: 16) {
                    // First Name
                    TextField("", text: $firstName, prompt: Text("İsim").foregroundColor(YesilTheme.textPlaceholder))
                        .textFieldStyle(YesilTextFieldStyle())
                        .foregroundColor(YesilTheme.textPrimary)
                        .autocapitalization(.words)
                        .disabled(isLoading)
                    
                    // Last Name
                    TextField("", text: $lastName, prompt: Text("Soyisim").foregroundColor(YesilTheme.textPlaceholder))
                        .textFieldStyle(YesilTextFieldStyle())
                        .foregroundColor(YesilTheme.textPrimary)
                        .autocapitalization(.words)
                        .disabled(isLoading)
                    
                    // Age
                    TextField("", text: $age, prompt: Text("Yaş").foregroundColor(YesilTheme.textPlaceholder))
                        .textFieldStyle(YesilTextFieldStyle())
                        .foregroundColor(YesilTheme.textPrimary)
                        .keyboardType(.numberPad)
                        .disabled(isLoading)
                    
                    // Gender Picker
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Cinsiyet")
                            .font(.system(size: 14))
                            .foregroundColor(YesilTheme.textSecondary)
                        
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                            ForEach(User.Gender.allCases, id: \.self) { gender in
                                Button(action: {
                                    selectedGender = gender
                                }) {
                                    Text(gender.displayName)
                                        .font(.system(size: 14, weight: .medium))
                                        .foregroundColor(selectedGender == gender ? .white : YesilTheme.textPrimary)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 12)
                                        .background(selectedGender == gender ? YesilTheme.primary : Color.white)
                                        .cornerRadius(10)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(selectedGender == gender ? YesilTheme.primary : YesilTheme.border, lineWidth: 1)
                                        )
                                }
                                .disabled(isLoading)
                            }
                        }
                    }
                }
                
                // Save Button
                Button(action: saveProfile) {
                    HStack {
                        if isLoading {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Text("Devam Et")
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
                .disabled(isLoading)
                
                // Skip Button
                Button(action: {
                    navigationManager.navigateToMain()
                }) {
                    Text("Daha sonra tamamla")
                        .font(.system(size: 14))
                        .foregroundColor(YesilTheme.textSecondary)
                        .underline()
                }
                .padding(.top, 16)
                .disabled(isLoading)
                
                Spacer()
            }
            .padding(.horizontal, 24)
        }
        .background(YesilTheme.background)
    }
    
    private func saveProfile() {
        guard let userId = Auth.auth().currentUser?.uid else {
            errorMessage = "Kullanıcı bulunamadı"
            return
        }
        
        errorMessage = ""
        
        // Validate
        guard !firstName.isEmpty else {
            errorMessage = "İsim gereklidir"
            return
        }
        
        guard !lastName.isEmpty else {
            errorMessage = "Soyisim gereklidir"
            return
        }
        
        isLoading = true
        
        var userData: [String: Any] = [
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
        
        db.collection("users").document(userId).setData(userData, merge: true) { error in
            isLoading = false
            
            if let error = error {
                errorMessage = "Kayıt hatası: \(error.localizedDescription)"
            } else {
                navigationManager.navigateToMain()
            }
        }
    }
}

#Preview {
    ProfileCompletionView(navigationManager: NavigationManager())
}

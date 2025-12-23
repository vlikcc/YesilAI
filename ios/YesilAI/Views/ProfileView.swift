import SwiftUI
import FirebaseAuth
import FirebaseFirestore

struct ProfileView: View {
    @ObservedObject var navigationManager: NavigationManager
    @StateObject private var authManager = AuthManager.shared
    @State private var showingLogoutAlert = false
    @State private var userData: [String: Any] = [:]
    @State private var isLoading = true
    
    private let db = Firestore.firestore()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Profile Header
                    VStack(spacing: 16) {
                        // Avatar
                        ZStack {
                            Circle()
                                .fill(YesilTheme.primary.opacity(0.2))
                                .frame(width: 100, height: 100)
                            
                            Image(systemName: "person.fill")
                                .font(.system(size: 48))
                                .foregroundColor(YesilTheme.primary)
                        }
                        
                        // User Name
                        Text(fullName)
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(YesilTheme.textPrimary)
                        
                        // User Email
                        Text(authManager.currentUserEmail)
                            .font(.system(size: 14))
                            .foregroundColor(YesilTheme.textSecondary)
                    }
                    .padding(.top, 20)
                    
                    if isLoading {
                        ProgressView()
                            .padding()
                    } else {
                        // Profile Info Section
                        VStack(spacing: 0) {
                            ProfileMenuItem(
                                icon: "person.fill",
                                title: "Ad Soyad",
                                value: fullName
                            )
                            
                            Divider()
                                .padding(.leading, 56)
                            
                            ProfileMenuItem(
                                icon: "envelope.fill",
                                title: "E-posta",
                                value: authManager.currentUserEmail
                            )
                            
                            if let age = userData["age"] as? Int {
                                Divider()
                                    .padding(.leading, 56)
                                
                                ProfileMenuItem(
                                    icon: "number",
                                    title: "Yaş",
                                    value: "\(age)"
                                )
                            }
                            
                            if let genderRaw = userData["gender"] as? String {
                                Divider()
                                    .padding(.leading, 56)
                                
                                ProfileMenuItem(
                                    icon: "person.2.fill",
                                    title: "Cinsiyet",
                                    value: genderDisplayName(genderRaw)
                                )
                            }
                            
                            Divider()
                                .padding(.leading, 56)
                            
                            ProfileMenuItem(
                                icon: "calendar",
                                title: "Kayıt Tarihi",
                                value: formattedRegistrationDate
                            )
                        }
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 2)
                        .padding(.horizontal, 16)
                    }
                    
                    // Account Actions Section
                    VStack(spacing: 0) {
                        Button(action: {
                            showingLogoutAlert = true
                        }) {
                            HStack(spacing: 16) {
                                Image(systemName: "rectangle.portrait.and.arrow.forward")
                                    .font(.system(size: 20))
                                    .foregroundColor(YesilTheme.error)
                                    .frame(width: 24)
                                
                                Text("Çıkış Yap")
                                    .font(.system(size: 16))
                                    .foregroundColor(YesilTheme.error)
                                
                                Spacer()
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                        }
                    }
                    .background(Color.white)
                    .cornerRadius(12)
                    .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 2)
                    .padding(.horizontal, 16)
                    
                    Spacer()
                }
            }
            .background(YesilTheme.background)
            .navigationTitle("Profil")
            .navigationBarTitleDisplayMode(.large)
            .toolbarColorScheme(.light, for: .navigationBar)
            .toolbarBackground(YesilTheme.background, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
            .onAppear {
                loadUserData()
            }
            .alert("Çıkış Yap", isPresented: $showingLogoutAlert) {
                Button("İptal", role: .cancel) { }
                Button("Çıkış Yap", role: .destructive) {
                    authManager.logout()
                    navigationManager.navigateToLogin()
                }
            } message: {
                Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?")
            }
        }
        .accentColor(YesilTheme.primary)
    }
    
    private var fullName: String {
        let firstName = userData["firstName"] as? String ?? ""
        let lastName = userData["lastName"] as? String ?? ""
        let name = "\(firstName) \(lastName)".trimmingCharacters(in: .whitespaces)
        return name.isEmpty ? authManager.currentUserEmail : name
    }
    
    private var formattedRegistrationDate: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd MMMM yyyy"
        formatter.locale = Locale(identifier: "tr_TR")
        
        // Try Firestore Timestamp first
        if let timestamp = userData["createdAt"] as? Timestamp {
            return formatter.string(from: timestamp.dateValue())
        }
        
        // Fallback to Firebase Auth creation date
        if let creationDate = Auth.auth().currentUser?.metadata.creationDate {
            return formatter.string(from: creationDate)
        }
        
        return "Bilinmiyor"
    }
    
    private func genderDisplayName(_ rawValue: String) -> String {
        switch rawValue {
        case "erkek": return "Erkek"
        case "kadın": return "Kadın"
        case "belirtmek_istemiyorum": return "Belirtmek İstemiyorum"
        default: return rawValue
        }
    }
    
    private func loadUserData() {
        guard let userId = Auth.auth().currentUser?.uid else {
            isLoading = false
            return
        }
        
        db.collection("users").document(userId).getDocument { snapshot, error in
            isLoading = false
            
            if let data = snapshot?.data() {
                userData = data
            }
        }
    }
}

struct ProfileMenuItem: View {
    let icon: String
    let title: String
    let value: String
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(YesilTheme.primary)
                .frame(width: 24)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.system(size: 14))
                    .foregroundColor(YesilTheme.textSecondary)
                
                Text(value)
                    .font(.system(size: 16))
                    .foregroundColor(YesilTheme.textPrimary)
            }
            
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
}

#Preview {
    ProfileView(navigationManager: NavigationManager())
}

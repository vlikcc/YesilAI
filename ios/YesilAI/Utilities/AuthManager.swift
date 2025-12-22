import Foundation
import SwiftUI
import FirebaseCore
import FirebaseAuth
import GoogleSignIn
import AuthenticationServices
import CryptoKit

@MainActor
class AuthManager: NSObject, ObservableObject {
    static let shared = AuthManager()
    
    @Published var isLoggedIn: Bool = false
    @Published var currentUserEmail: String = ""
    @Published var errorMessage: String = ""
    @Published var isLoading: Bool = false
    
    // For Apple Sign In
    private var currentNonce: String?
    private var appleSignInContinuation: CheckedContinuation<Bool, Never>?
    
    private override init() {
        super.init()
        // Check if user is already logged in
        if let user = Auth.auth().currentUser {
            isLoggedIn = true
            currentUserEmail = user.email ?? ""
        }
    }
    
    // MARK: - Email/Password Authentication
    
    // Register new user
    func register(email: String, password: String, confirmPassword: String) async -> Bool {
        errorMessage = ""
        
        // Validation
        guard !email.isEmpty else {
            errorMessage = "E-posta adresi gereklidir"
            return false
        }
        
        guard email.contains("@") && email.contains(".") else {
            errorMessage = "Geçerli bir e-posta adresi giriniz"
            return false
        }
        
        guard !password.isEmpty else {
            errorMessage = "Şifre gereklidir"
            return false
        }
        
        guard password.count >= 6 else {
            errorMessage = "Şifre en az 6 karakter olmalıdır"
            return false
        }
        
        guard password == confirmPassword else {
            errorMessage = "Şifreler eşleşmiyor"
            return false
        }
        
        isLoading = true
        
        do {
            let result = try await Auth.auth().createUser(withEmail: email, password: password)
            currentUserEmail = result.user.email ?? email
            isLoggedIn = true
            isLoading = false
            return true
        } catch let error as NSError {
            isLoading = false
            switch error.code {
            case AuthErrorCode.emailAlreadyInUse.rawValue:
                errorMessage = "Bu e-posta adresi zaten kayıtlı"
            case AuthErrorCode.invalidEmail.rawValue:
                errorMessage = "Geçersiz e-posta adresi"
            case AuthErrorCode.weakPassword.rawValue:
                errorMessage = "Şifre çok zayıf"
            default:
                errorMessage = "Kayıt başarısız: \(error.localizedDescription)"
            }
            return false
        }
    }
    
    // Login user
    func login(email: String, password: String) async -> Bool {
        errorMessage = ""
        
        guard !email.isEmpty else {
            errorMessage = "E-posta adresi gereklidir"
            return false
        }
        
        guard !password.isEmpty else {
            errorMessage = "Şifre gereklidir"
            return false
        }
        
        isLoading = true
        
        do {
            let result = try await Auth.auth().signIn(withEmail: email, password: password)
            currentUserEmail = result.user.email ?? email
            isLoggedIn = true
            isLoading = false
            return true
        } catch let error as NSError {
            isLoading = false
            switch error.code {
            case AuthErrorCode.userNotFound.rawValue:
                errorMessage = "Bu e-posta adresi kayıtlı değil"
            case AuthErrorCode.wrongPassword.rawValue:
                errorMessage = "Şifre yanlış"
            case AuthErrorCode.invalidEmail.rawValue:
                errorMessage = "Geçersiz e-posta adresi"
            case AuthErrorCode.invalidCredential.rawValue:
                errorMessage = "E-posta veya şifre yanlış"
            default:
                errorMessage = "Giriş başarısız: \(error.localizedDescription)"
            }
            return false
        }
    }
    
    // Logout user
    func logout() {
        do {
            try Auth.auth().signOut()
            isLoggedIn = false
            currentUserEmail = ""
        } catch {
            print("Logout error: \(error.localizedDescription)")
        }
    }
    
    // MARK: - Google Sign In
    
    func signInWithGoogle() async -> Bool {
        errorMessage = ""
        isLoading = true
        
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            errorMessage = "Firebase yapılandırması bulunamadı"
            isLoading = false
            return false
        }
        
        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config
        
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootViewController = windowScene.windows.first?.rootViewController else {
            errorMessage = "Uygulama penceresi bulunamadı"
            isLoading = false
            return false
        }
        
        do {
            let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController)
            
            guard let idToken = result.user.idToken?.tokenString else {
                errorMessage = "Google kimlik doğrulama hatası"
                isLoading = false
                return false
            }
            
            let credential = GoogleAuthProvider.credential(
                withIDToken: idToken,
                accessToken: result.user.accessToken.tokenString
            )
            
            let authResult = try await Auth.auth().signIn(with: credential)
            currentUserEmail = authResult.user.email ?? ""
            isLoggedIn = true
            isLoading = false
            return true
            
        } catch {
            isLoading = false
            errorMessage = "Google ile giriş başarısız: \(error.localizedDescription)"
            return false
        }
    }
    
    // MARK: - Apple Sign In
    
    func signInWithApple() async -> Bool {
        errorMessage = ""
        isLoading = true
        
        let nonce = randomNonceString()
        currentNonce = nonce
        
        let appleIDProvider = ASAuthorizationAppleIDProvider()
        let request = appleIDProvider.createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = sha256(nonce)
        
        let authorizationController = ASAuthorizationController(authorizationRequests: [request])
        authorizationController.delegate = self
        
        return await withCheckedContinuation { continuation in
            appleSignInContinuation = continuation
            authorizationController.performRequests()
        }
    }
    
    // Generate random nonce for Apple Sign In
    private func randomNonceString(length: Int = 32) -> String {
        precondition(length > 0)
        var randomBytes = [UInt8](repeating: 0, count: length)
        let errorCode = SecRandomCopyBytes(kSecRandomDefault, randomBytes.count, &randomBytes)
        if errorCode != errSecSuccess {
            fatalError("Unable to generate nonce. SecRandomCopyBytes failed with OSStatus \(errorCode)")
        }
        
        let charset: [Character] = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        let nonce = randomBytes.map { byte in
            charset[Int(byte) % charset.count]
        }
        return String(nonce)
    }
    
    // SHA256 hash for nonce
    private func sha256(_ input: String) -> String {
        let inputData = Data(input.utf8)
        let hashedData = SHA256.hash(data: inputData)
        let hashString = hashedData.compactMap {
            String(format: "%02x", $0)
        }.joined()
        return hashString
    }
}

// MARK: - ASAuthorizationControllerDelegate
extension AuthManager: ASAuthorizationControllerDelegate {
    nonisolated func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        Task { @MainActor in
            guard let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
                  let nonce = currentNonce,
                  let appleIDToken = appleIDCredential.identityToken,
                  let idTokenString = String(data: appleIDToken, encoding: .utf8) else {
                errorMessage = "Apple kimlik bilgileri alınamadı"
                isLoading = false
                appleSignInContinuation?.resume(returning: false)
                appleSignInContinuation = nil
                return
            }
            
            let credential = OAuthProvider.appleCredential(
                withIDToken: idTokenString,
                rawNonce: nonce,
                fullName: appleIDCredential.fullName
            )
            
            do {
                let authResult = try await Auth.auth().signIn(with: credential)
                currentUserEmail = authResult.user.email ?? ""
                isLoggedIn = true
                isLoading = false
                appleSignInContinuation?.resume(returning: true)
            } catch {
                errorMessage = "Apple ile giriş başarısız: \(error.localizedDescription)"
                isLoading = false
                appleSignInContinuation?.resume(returning: false)
            }
            appleSignInContinuation = nil
        }
    }
    
    nonisolated func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        Task { @MainActor in
            let nsError = error as NSError
            if nsError.code != ASAuthorizationError.canceled.rawValue {
                errorMessage = "Apple ile giriş başarısız: \(error.localizedDescription)"
            }
            isLoading = false
            appleSignInContinuation?.resume(returning: false)
            appleSignInContinuation = nil
        }
    }
}

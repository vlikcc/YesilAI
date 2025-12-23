import SwiftUI
import FirebaseAuth

struct ContentView: View {
    @StateObject private var navigationManager = NavigationManager()
    
    var body: some View {
        Group {
            switch navigationManager.currentScreen {
            case .login:
                LoginView(navigationManager: navigationManager)
            case .register:
                RegisterView(navigationManager: navigationManager)
            case .main:
                MainTabView(navigationManager: navigationManager)
            case .profileCompletion:
                ProfileCompletionView(navigationManager: navigationManager)
            }
        }
        .animation(.easeInOut, value: navigationManager.currentScreen)
    }
}

class NavigationManager: ObservableObject {
    enum Screen {
        case login
        case register
        case main
        case profileCompletion
    }
    
    @Published var currentScreen: Screen = .login
    
    init() {
        // Check if user is already logged in
        if Auth.auth().currentUser != nil {
            currentScreen = .main
        }
    }
    
    func navigateToLogin() {
        currentScreen = .login
    }
    
    func navigateToRegister() {
        currentScreen = .register
    }
    
    func navigateToMain() {
        currentScreen = .main
    }
    
    func navigateToProfileCompletion() {
        currentScreen = .profileCompletion
    }
}

#Preview {
    ContentView()
}

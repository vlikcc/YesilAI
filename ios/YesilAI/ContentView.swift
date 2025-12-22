import SwiftUI

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
    }
    
    @Published var currentScreen: Screen = .login
    
    func navigateToLogin() {
        currentScreen = .login
    }
    
    func navigateToRegister() {
        currentScreen = .register
    }
    
    func navigateToMain() {
        currentScreen = .main
    }
}

#Preview {
    ContentView()
}

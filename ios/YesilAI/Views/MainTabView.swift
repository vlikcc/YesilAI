import SwiftUI

struct MainTabView: View {
    @ObservedObject var navigationManager: NavigationManager
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            ChatView(navigationManager: navigationManager)
                .tabItem {
                    Text(selectedTab == 0 ? "💬" : "💭")
                    Text("Sohbet")
                }
                .tag(0)
            
            ProfileView()
                .tabItem {
                    Text(selectedTab == 1 ? "👤" : "👥")
                    Text("Profil")
                }
                .tag(1)
            
            SettingsView()
                .tabItem {
                    Text(selectedTab == 2 ? "⚙️" : "🔧")
                    Text("Ayarlar")
                }
                .tag(2)
        }
        .accentColor(YesilTheme.primary)
    }
}

#Preview {
    MainTabView(navigationManager: NavigationManager())
}

import SwiftUI

struct MainTabView: View {
    @ObservedObject var navigationManager: NavigationManager
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            ChatView(navigationManager: navigationManager)
                .tabItem {
                    Image(systemName: selectedTab == 0 ? "message.fill" : "message")
                        .font(.system(size: 24))
                    Text("Sohbet")
                }
                .tag(0)
            
            ProfileView(navigationManager: navigationManager)
                .tabItem {
                    Image(systemName: selectedTab == 1 ? "person.fill" : "person")
                        .font(.system(size: 24))
                    Text("Profil")
                }
                .tag(1)
            
            SettingsView(navigationManager: navigationManager)
                .tabItem {
                    Image(systemName: selectedTab == 2 ? "gearshape.fill" : "gearshape")
                        .font(.system(size: 24))
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

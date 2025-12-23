import SwiftUI

struct SettingsView: View {
    @ObservedObject var navigationManager: NavigationManager
    @AppStorage("notificationsEnabled") private var notificationsEnabled = true
    @AppStorage("darkModeEnabled") private var darkModeEnabled = false
    
    var body: some View {
        NavigationView {
            List {
                // Notifications Section
                Section {
                    Toggle(isOn: $notificationsEnabled) {
                        HStack(spacing: 12) {
                            Image(systemName: "bell.fill")
                                .foregroundColor(YesilTheme.primary)
                                .frame(width: 24)
                            Text("Bildirimler")
                                .foregroundColor(YesilTheme.textPrimary)
                        }
                    }
                    .tint(YesilTheme.primary)
                    .listRowBackground(Color.white)
                } header: {
                    Text("Bildirimler")
                        .foregroundColor(YesilTheme.textSecondary)
                }
                
                // Appearance Section
                Section {
                    Toggle(isOn: $darkModeEnabled) {
                        HStack(spacing: 12) {
                            Image(systemName: "moon.fill")
                                .foregroundColor(YesilTheme.primary)
                                .frame(width: 24)
                            Text("Karanlık Mod")
                                .foregroundColor(YesilTheme.textPrimary)
                        }
                    }
                    .tint(YesilTheme.primary)
                    .listRowBackground(Color.white)
                } header: {
                    Text("Görünüm")
                        .foregroundColor(YesilTheme.textSecondary)
                }
                
                // About Section
                Section {
                    HStack(spacing: 12) {
                        Image(systemName: "info.circle.fill")
                            .foregroundColor(YesilTheme.primary)
                            .frame(width: 24)
                        Text("Sürüm")
                            .foregroundColor(YesilTheme.textPrimary)
                        Spacer()
                        Text("1.0.0")
                            .foregroundColor(YesilTheme.textSecondary)
                    }
                    .listRowBackground(Color.white)
                    
                    Link(destination: URL(string: "https://yesilai.com/privacy")!) {
                        HStack(spacing: 12) {
                            Image(systemName: "lock.shield.fill")
                                .foregroundColor(YesilTheme.primary)
                                .frame(width: 24)
                            Text("Gizlilik Politikası")
                                .foregroundColor(YesilTheme.textPrimary)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.system(size: 14))
                                .foregroundColor(YesilTheme.textSecondary)
                        }
                    }
                    .listRowBackground(Color.white)
                    
                    Link(destination: URL(string: "https://yesilai.com/terms")!) {
                        HStack(spacing: 12) {
                            Image(systemName: "doc.text.fill")
                                .foregroundColor(YesilTheme.primary)
                                .frame(width: 24)
                            Text("Kullanım Koşulları")
                                .foregroundColor(YesilTheme.textPrimary)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.system(size: 14))
                                .foregroundColor(YesilTheme.textSecondary)
                        }
                    }
                    .listRowBackground(Color.white)
                } header: {
                    Text("Hakkında")
                        .foregroundColor(YesilTheme.textSecondary)
                }
                
                // Support Section
                Section {
                    Link(destination: URL(string: "mailto:destek@yesilai.com")!) {
                        HStack(spacing: 12) {
                            Image(systemName: "envelope.fill")
                                .foregroundColor(YesilTheme.primary)
                                .frame(width: 24)
                            Text("Destek")
                                .foregroundColor(YesilTheme.textPrimary)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.system(size: 14))
                                .foregroundColor(YesilTheme.textSecondary)
                        }
                    }
                    .listRowBackground(Color.white)
                } header: {
                    Text("Yardım")
                        .foregroundColor(YesilTheme.textSecondary)
                }
            }
            .listStyle(.insetGrouped)
            .scrollContentBackground(.hidden)
            .background(YesilTheme.background)
            .navigationTitle("Ayarlar")
            .navigationBarTitleDisplayMode(.large)
            .toolbarColorScheme(.light, for: .navigationBar)
            .toolbarBackground(YesilTheme.background, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
        }
        .accentColor(YesilTheme.primary)
        .preferredColorScheme(.light)
    }
}

#Preview {
    SettingsView(navigationManager: NavigationManager())
}

import SwiftUI

struct SettingsView: View {
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            
            Text("⚙️")
                .font(.system(size: 24))
                .padding(.bottom, 10)
            
            Text("Ayarlar")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(YesilTheme.textPrimary)
                .padding(.bottom, 8)
            
            Text("Uygulama ayarları ve tercihleri\nyakında eklenecek")
                .font(.system(size: 16))
                .foregroundColor(YesilTheme.textSecondary)
                .multilineTextAlignment(.center)
            
            Spacer()
        }
        .padding(20)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(YesilTheme.background)
    }
}

#Preview {
    SettingsView()
}

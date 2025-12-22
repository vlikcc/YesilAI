import SwiftUI

struct ProfileView: View {
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            
            Text("👤")
                .font(.system(size: 24))
                .padding(.bottom, 10)
            
            Text("Profil")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(YesilTheme.textPrimary)
                .padding(.bottom, 8)
            
            Text("Kullanıcı profili ve ayarları\nyakında eklenecek")
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
    ProfileView()
}

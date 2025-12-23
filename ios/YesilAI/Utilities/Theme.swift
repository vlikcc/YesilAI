import SwiftUI

// MARK: - Theme Colors
struct YesilTheme {
    static let primary = Color(hex: "10b981")
    static let primaryDark = Color(hex: "059669")
    static let background = Color(hex: "f9fafb")
    static let surface = Color.white
    static let textPrimary = Color(hex: "1f2937")
    static let textSecondary = Color(hex: "6b7280")
    static let textPlaceholder = Color(hex: "9ca3af")
    static let botBubble = Color(hex: "d1fae5")
    static let border = Color(hex: "e5e7eb")
    static let error = Color(hex: "dc2626")
    static let googleRed = Color(hex: "ea4335")
    static let microsoftBlue = Color(hex: "0078d4")
}

// MARK: - Color Extension
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

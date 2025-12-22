// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "YesilAI",
    platforms: [
        .iOS(.v17)
    ],
    dependencies: [
        .package(url: "https://github.com/firebase/firebase-ios-sdk.git", from: "10.19.0")
    ],
    targets: [
        .target(
            name: "YesilAI",
            dependencies: [
                .product(name: "FirebaseAuth", package: "firebase-ios-sdk")
            ]
        )
    ]
)

// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "PaginautSwiftUI",
    platforms: [
        .iOS(.v16),
        .macOS(.v13),
    ],
    products: [
        .library(
            name: "PaginautCore",
            targets: ["PaginautCoreWrapper"]
        ),
        .library(
            name: "PaginautSwiftUI",
            targets: ["PaginautSwiftUI"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "PaginautCore",
            // Local development path (relative to Package.swift):
            path: "../paginaut-core/build/XCFrameworks/release/PaginautCore.xcframework"
            // For distribution, replace with:
            // url: "https://github.com/aenadgrleey/paginaut/releases/download/v1.0.0/PaginautCore.xcframework.zip",
            // checksum: "<sha256>"
        ),
        .target(
            name: "PaginautCoreWrapper",
            dependencies: ["PaginautCore"],
            path: "Sources/PaginautCore"
        ),
        .target(
            name: "PaginautSwiftUI",
            dependencies: ["PaginautCoreWrapper"],
            path: "Sources/PaginautSwiftUI"
        ),
    ]
)

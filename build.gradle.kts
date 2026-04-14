plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.skie) apply false
}

allprojects {
    group = property("paginaut.group") as String
    version = property("paginaut.version") as String
}

val swiftUiFrameworkModule = providers.gradleProperty("paginaut.swift.module")
    .orElse("PaginautCore")
val swiftUiOutputDir = providers.gradleProperty("paginaut.swift.outputDir").orNull

tasks.register<GenerateSwiftUiSourcesTask>("generateSwiftUiSources") {
    group = "swift"
    description = "Generates Paginaut SwiftUI sources for a Kotlin framework module."

    sourceDir.set(layout.projectDirectory.dir("paginaut-swiftui/Sources/PaginautSwiftUI"))
    frameworkModuleName.set(swiftUiFrameworkModule)
    if (swiftUiOutputDir != null) {
        outputDir.set(layout.projectDirectory.dir(swiftUiOutputDir))
    } else {
        outputDir.set(layout.buildDirectory.dir("generated/swiftui/PaginautSwiftUI"))
    }
}

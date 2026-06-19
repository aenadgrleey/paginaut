import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.skie)
}

kotlin {
    val buildXcframework = providers.gradleProperty("paginaut.build.xcframework")
        .map { it.toBooleanStrictOrNull() ?: true }
        .getOrElse(true)
    val xcf = if (buildXcframework) XCFramework("Shared") else null

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.paginautCore)
            xcf?.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.paginautCore)
        }
    }
}

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.skie)
    `maven-publish`
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name = "Paginaut Core"
            description = "Kotlin Multiplatform pagination library"
            url = "https://github.com/aenadgrleey/paginaut"
            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "aenadgrleey"
                    name = "aenadgrleey"
                }
            }
            scm {
                url = "https://github.com/aenadgrleey/paginaut"
            }
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.aenadgrleey.paginaut.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    js {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    val xcf = XCFramework("PaginautCore")

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "PaginautCore"
            isStatic = true
            xcf.add(this)
        }
    }

    macosX64()
    macosArm64()

    linuxX64()
    linuxArm64()

    mingwX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

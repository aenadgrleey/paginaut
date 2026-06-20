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
    val buildXcframework = providers.gradleProperty("paginaut.build.xcframework")
        .map { it.toBooleanStrictOrNull() ?: true }
        .getOrElse(true)
    val xcf = if (buildXcframework) XCFramework("PaginautCore") else null

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

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "PaginautCore"
            isStatic = true
            xcf?.add(this)
        }
    }

    macosX64()
    macosArm64()

    linuxX64()
    linuxArm64()

    mingwX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        // Don't publish sources/metadata jars for native targets. The KGP
        // auto-generates them, and the .module file lists them with proper
        // `-sources` / `-metadata` classifiers. But JitPack (our only
        // distribution channel) regenerates .module files on upload, dropping
        // the classifiers, so the published .module ends up pointing at
        // filenames like `paginaut-core-iosarm64-v0.3.x.jar` that 404
        // (JitPack actually uploaded `-sources.jar` / `-metadata.jar`).
        //
        // Dropping the variants means there's nothing for JitPack to
        // mis-attribute; IDE consumers fall back to the .pom, which lists
        // the klib as the main artifact. Sources jars aren't load-bearing for
        // pagination library consumers — they only matter for source-level
        // debugging inside an IDE, and even then `.jar` source attachments
        // are cosmetic.
        // Two things disable sources/metadata jars for native targets:
        //   1. `withSourcesJar(false)` on the publication drops the sources
        //      variant from the .module.
        //   2. Disabling the per-target `*MetadataElements` task drops the
        //      metadata jar from the publication and the corresponding
        //      variant from the .module. There is no `withMetadataJar(false)`
        //      counterpart to `withSourcesJar(false)`.
        // After both, the .module for each native variant references only
        // the klib — the only artifact JitPack's `.module` regeneration
        // can't get wrong.
        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
            mavenPublication {
                withSourcesJar(false)
            }
            tasks.matching {
                it.name.endsWith("MetadataElements") && !it.name.startsWith("metadata")
            }.configureEach {
                this.setEnabled(false)
            }
        }
    }
}

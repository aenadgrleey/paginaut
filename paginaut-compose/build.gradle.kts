import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidMultiplatformLibrary)
    `maven-publish`
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name = "Paginaut Compose"
            description = "Compose Multiplatform pagination UI components"
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
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    androidLibrary {
        namespace = "com.aenadgrleey.paginaut.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm("desktop")

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.paginautCore)
            @Suppress("DEPRECATION")
            api(compose.runtime)
            @Suppress("DEPRECATION")
            api(compose.foundation)
            @Suppress("DEPRECATION")
            api(compose.material3)
            @Suppress("DEPRECATION")
            api(compose.ui)
            @Suppress("DEPRECATION")
            api(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // Don't publish sources/metadata jars for native targets. See the
        // equivalent comment in paginaut-core/build.gradle.kts for the full
        // rationale. In short: JitPack regenerates .module files on upload
        // and drops the `-sources` / `-metadata` classifiers, so the
        // regenerated .module points at filenames that 404. We drop both
        // variants from the publication so there's nothing for JitPack to
        // mis-attribute. Consumers fall back to the .pom and use the klib
        // directly.
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

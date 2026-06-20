import java.io.File
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
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

// Patch the .module files produced for native targets so JitPack can serve
// them without 404s. The KGP emits a .module whose sources/metadata variants
// reference `.jar` filenames without `-sources` / `-metadata` classifiers.
// JitPack then *also* rewrites the .module on upload, dropping the
// classifiers in the process — leaving the published .module pointing at
// filenames that don't exist (404).
//
// Concretely: the local publish produces
//   paginaut-core-iosarm64-{version}-sources.jar
//   paginaut-core-iosarm64-{version}-metadata.jar
//   paginaut-core-iosarm64-{version}.klib
// but the .module published on JitPack names them as
//   paginaut-core-iosarm64-{version}.jar   (twice)
// which 404. We rewrite the .module in-place to point each variant at the
// actually-existing classified file. Runs as part of `publishToMavenLocal`
// so JitPack picks up the patched file when uploading.
val patchPaginautNativeModule by tasks.registering {
    group = "publishing"
    description = "Patches .module files for native targets in the local Maven repo to use classified file names."
    // The doLast block references JsonSlurper/JsonOutput from the Gradle
    // classpath and walks the local Maven repo; the script-level patchModuleFile
    // function can't be serialized for the configuration cache, so exclude.
    notCompatibleWithConfigurationCache("Patch task reads local Maven repo; not safe to cache.")

    doLast {
        val m2Root = File(System.getProperty("maven.repo.local")
            ?: "${System.getProperty("user.home")}/.m2/repository")
        val groupDir = File(m2Root, "com/aenadgrleey/paginaut")
        if (!groupDir.exists()) return@doLast
        val slurper = JsonSlurper()
        groupDir.walkTopDown().forEach { f ->
            if (f.isFile && f.name.endsWith(".module")) {
                patchModuleFile(f, slurper)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun patchModuleFile(moduleFile: File, slurper: JsonSlurper) {
    val root = slurper.parse(moduleFile) as? MutableMap<String, Any?> ?: return
    val variants = root["variants"] as? List<*> ?: return
    var changed = false
    variants.forEach { variant ->
        val variantMap = variant as? MutableMap<String, Any?> ?: return@forEach
        val attrs = variantMap["attributes"] as? MutableMap<String, String> ?: return@forEach
        if (attrs["org.jetbrains.kotlin.platform.type"] != "native") return@forEach
        val classifier = when {
            attrs["org.gradle.docstype"] == "sources" -> "sources"
            attrs["org.gradle.usage"] == "kotlin-metadata" -> "metadata"
            else -> null
        } ?: return@forEach
        val files = variantMap["files"] as? List<*> ?: return@forEach
        files.forEach { fileNode ->
            val fileMap = fileNode as? MutableMap<String, Any?> ?: return@forEach
            val name = fileMap["name"] as? String ?: return@forEach
            if (name.endsWith(".jar") && !name.endsWith("-$classifier.jar")) {
                val corrected = name.removeSuffix(".jar") + "-$classifier.jar"
                fileMap["name"] = corrected
                fileMap["url"] = corrected
                changed = true
            }
        }
    }
    if (changed) {
        moduleFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(root)))
    }
}

subprojects {
    // Run the patch *after* the publish, not before. JitPack invokes
    // `./gradlew publishToMavenLocal`; if patch runs first, the local Maven
    // repo is empty and there's nothing to fix. By making the patch
    // finalize the publish, we guarantee the .module files are in place when
    // the patch walks the repo.
    tasks.matching { it.name == "publishToMavenLocal" }.configureEach {
        finalizedBy(patchPaginautNativeModule)
    }
}

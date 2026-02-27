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

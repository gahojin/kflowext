plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlinx.kover) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.maven.publish) apply false
}

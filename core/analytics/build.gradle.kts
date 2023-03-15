plugins {
    id("appcoins.android.library")
}

android {
    namespace = "com.appcoins.wallet.core.analytics"
    defaultConfig {
        buildConfigField(
            "String", "RAKAM_API_KEY", project.property("RAKAM_API_KEY").toString())
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(project(":legacy:commons"))
    implementation(project(":core:utils:properties"))
    implementation(libs.jackson.annotation)
    implementation(libs.bundles.analytics)
    implementation(libs.bundles.network)
    implementation(libs.androidx.multidex)
    implementation(libs.google.play.services)
}
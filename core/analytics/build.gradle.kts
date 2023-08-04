plugins {
    id("appcoins.android.library")
}

android {
    namespace = "com.appcoins.wallet.core.analytics"
}

dependencies {
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(project(":ui:common"))
    implementation(project(":core:utils:jvm-common"))
    implementation(project(":core:utils:properties"))
    implementation(project(":core:network:analytics"))
    implementation(libs.jackson.annotation)
    implementation(libs.bundles.analytics)
    implementation(libs.bundles.network)
    implementation(libs.androidx.multidex)
    implementation(libs.google.play.services)
}
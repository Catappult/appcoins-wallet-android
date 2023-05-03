plugins {
    id("appcoins.android.library")
}
android {
    namespace = "com.appcoins.wallet.feature.rewards"
}

dependencies {
    implementation(libs.androidx.navigation.ui)
    implementation(libs.bundles.rx)
    implementation(libs.rx.rxlifecyle)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.result)
}
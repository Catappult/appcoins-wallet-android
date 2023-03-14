plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.shared_preferences"
}

dependencies {
  implementation(libs.bundles.rx)
  implementation(libs.androidx.security.crypto)
  implementation(libs.bundles.network)
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
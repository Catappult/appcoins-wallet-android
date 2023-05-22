plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.airdrop"
}

dependencies {
  implementation(libs.bundles.network)
  implementation(libs.jetbrains.annotations)
  testImplementation(libs.bundles.testing)
  implementation(project(":core:network:airdrop"))
}